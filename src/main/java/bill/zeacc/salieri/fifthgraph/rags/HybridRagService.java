package bill.zeacc.salieri.fifthgraph.rags ;

import java.io.File ;
import java.io.IOException ;
import java.io.UncheckedIOException ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.sql.JDBCType ;
import java.sql.PreparedStatement ;
import java.sql.SQLException ;
import java.util.ArrayList ;
import java.util.Comparator ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.LinkedHashMap ;
import java.util.List ;
import java.util.Map ;
import java.util.Set ;
import java.util.SortedSet ;
import java.util.TreeSet ;
import java.util.UUID ;
import java.util.concurrent.atomic.AtomicInteger ;

import org.postgresql.util.PGobject ;
import org.springframework.ai.document.ContentFormatter ;
import org.springframework.ai.document.DefaultContentFormatter ;
import org.springframework.ai.document.Document ;
import org.springframework.ai.document.MetadataMode ;
import org.springframework.ai.embedding.BatchingStrategy ;
import org.springframework.ai.embedding.EmbeddingModel ;
import org.springframework.ai.embedding.EmbeddingOptions ;
import org.springframework.ai.embedding.TokenCountBatchingStrategy ;
import org.springframework.ai.vectorstore.SearchRequest ;
import org.springframework.ai.vectorstore.VectorStore ;
import org.springframework.ai.vectorstore.filter.Filter.Expression ;
import org.springframework.core.io.FileSystemResource ;
import org.springframework.core.io.InputStreamSource ;
import org.springframework.jdbc.core.BatchPreparedStatementSetter ;
import org.springframework.jdbc.core.JdbcTemplate ;
import org.springframework.jdbc.core.SqlTypeValue ;
import org.springframework.jdbc.core.StatementCreatorUtils ;
import org.springframework.stereotype.Service ;

import com.fasterxml.jackson.core.JsonProcessingException ;
import com.fasterxml.jackson.databind.ObjectMapper ;
import com.knuddels.jtokkit.api.EncodingType ;
import com.pgvector.PGvector ;

import dev.langchain4j.data.segment.TextSegment ;
import lombok.Data ;

@Service
public class HybridRagService implements RagPullerService {

	private enum HybridResultGroup implements RagPullerService.ResultGroup {
		HYBRID_RESULTS, NEIGHBOR_EXPANSION ;

		public String namespace ( ) {
			return "hybrid_rag_pull_results" ;
		}
	}

	private final JdbcTemplate jdbc ;
//	private final VectorStore vectorStorf ; // Vector store is limited in three major ways.
										// Search operator is limited at time of setup, which is really a
										// strange design decision that is baked into Spring AI's VectorStore
										// and even its own SearchRequest object.  I cannot change the operator at runtime.
										// Furthermore, I'm stuck with a single table underlying table structure
										// and I cannot do anything fancy to categorize or tenant my data or
										// keep separate document collections.  I am also limited to
										// a single vector index to search, with its own distinctive embedding model.
										// Next iteration, let's blow this up and redo with just JdbcTemplate
										// and EmbeddingModel.
	private EmbeddingModel embeddingModel ;
	private ObjectMapper om ;
	private TextChunker chunker ;
	private SegmentMapper mapper ;
	private final int batchSize = 200 ;

	private static final Map <DocumentSearchOptions.Operator, String> TEMPLATES = new HashMap <> ( ) ;

	static {
		// Needs distance customized per operator, topK and similarity threshold
		// Also need to sub in items from the select clause dynamically
		TEMPLATES.put ( DocumentSearchOptions.Operator.COSINE, "SELECT %s, embedding <=> ? AS distance FROM %s WHERE embedding <=> ? < ? %s %s ORDER BY distance LIMIT ? " ) ;
		TEMPLATES.put ( DocumentSearchOptions.Operator.L2, "SELECT %s, embedding <-> ? AS distance FROM %s WHERE embedding <-> ? < ? %s %s ORDER BY distance LIMIT ? " ) ;
		TEMPLATES.put ( DocumentSearchOptions.Operator.IP, "SELECT %s, (1 + (embedding <#> ?)) AS distance FROM %s WHERE (1 + (embedding <#> ?)) < ? %s %s ORDER BY distance LIMIT ? " ) ;
	}

	private static final Map <String, CorpusParser> PARSERS = Map.of(
		"application/pdf", new PdfBoxCorpusParser(),
		"text/plain", new TextCorpusParser()
	);

	public HybridRagService ( JdbcTemplate jdbc, VectorStore vectorStore, EmbeddingModel embeddingModel, ObjectMapper om ) {
		this.jdbc = jdbc ;
//		this.vectorStore = vectorStore ;
		this.embeddingModel = embeddingModel ;
		this.om = om ;
		this.chunker = new TextSplitterChunker ( 800, 120 ) ;                 // paragraph → sentence fallback
		this.mapper = new HybridizingSegmentMapper ( true /* deterministicIds */ ) ;
	}

	public void ingestPath ( Path root, String contentType ) {
		ingestPath ( root, RagIngestionContext.builder ( ).contentType ( contentType ).provenance ( root.toUri ( ).toString ( ) ).build ( ) ) ;
	}

	public void ingestPath ( Path root, RagIngestionContext xctx ) {
		xctx.setCollection ( xctx.getCollection ( ) == null ? "default" : xctx.getCollection ( ) ) ;
		xctx.setTenant ( xctx.getTenant ( ) == null ? "default" : xctx.getTenant ( ) ) ;
		xctx.setContentType ( xctx.getContentType ( ) == null ? "application/pdf" : xctx.getContentType ( ) ) ;
		String contentType = xctx.getContentType ( ) ;
		List <File> files = collectFiles ( root ) ;
		List <InputStreamSource> sources = files.stream ( ).map ( f -> bakeInputStreamSource ( f, contentType ) ).toList ( ) ;
		List <String> documentIds = files.stream ( ).map ( f -> UUID.randomUUID ( ).toString ( ) ).toList ( ) ;
		ingest ( sources, documentIds, xctx ) ;
	}

	private InputStreamSource bakeInputStreamSource ( File f, String contentType ) {
		InputStreamSource iss = new FileSystemResource ( f ) ;
		Map <String, Object> meta = new HashMap <> ( ) ;
		meta.put ( "fileName", f.getName ( ) ) ;
		meta.put ( "filePath", f.getAbsolutePath ( ) ) ;
		meta.put ( "fileSize", f.length ( ) ) ;
		meta.put ( "lastModified", f.lastModified ( ) ) ;
		meta.put ( "extension", f.getName ( ).contains ( "." ) ? f.getName ( ).substring ( f.getName ( ).lastIndexOf ( '.' ) + 1 ) : "" ) ;
		meta.put ( "content-type", contentType ) ;
		return new ProxyInputStreamSource ( iss, meta ) ;
	}

	private List <File> collectFiles ( Path root ) {
		try {
			List <File> files = new ArrayList <> ( ) ;
			Files.walk ( root ).filter ( Files::isRegularFile ).forEach ( p -> files.add ( p.toFile ( ) ) ) ;
			return files ;
		} catch ( IOException e ) {
			throw new UncheckedIOException ( e ) ;
		}
	}

	public void ingest ( List <InputStreamSource> sources, List <String> documentIds, RagIngestionContext xctx ) {
		CorpusParser parser = getParser ( xctx.getContentType ( ) ) ;
		RagIngestionTreeContext ctx = new RagIngestionTreeContext ( ) ;
		ctx.setBatchRoot ( xctx.getProvenance ( ) ) ;
		ctx.setProperty ( HybridizableRagIngestionKey.SESSION_ID, xctx.getSessionId ( ) ) ;
		ctx.setProperty ( HybridizableRagIngestionKey.REQUEST_ID, xctx.getRequestId ( ) == null ? UUID.randomUUID ( ).toString ( ) : xctx.getRequestId ( ) ) ;
		ctx.setProperty ( HybridizableRagIngestionKey.BATCH_ROOT, xctx.getProvenance ( ) ) ;
		ctx.setProperty ( HybridizableRagIngestionKey.TENANT, xctx.getTenant ( ) ) ;
		ctx.setProperty ( HybridizableRagIngestionKey.COLLECTION, xctx.getCollection ( ) ) ;
		ctx.setProperty ( HybridizableRagIngestionKey.CONTENT_TYPE, xctx.getContentType ( ) ) ;
		ctx.setProperty ( HybridizableRagIngestionKey.BATCH_ID, xctx.getBatchId ( ) == null ? UUID.randomUUID ( ).toString ( ) : xctx.getBatchId ( ) ) ;
		ctx.setProperty ( HybridizableRagIngestionKey.MAX_INPUT_TOKEN_COUNT, xctx.getMaxInputTokenCount ( ) <= 0 ? 2000 : xctx.getMaxInputTokenCount ( ) ) ;
		ctx.setProperty ( HybridizableRagIngestionKey.RESERVE_PERCENTAGE, xctx.getReservePercentage ( ) <= 0d ? 0.2d : xctx.getReservePercentage ( ) ) ;
		List <Document> batch = new ArrayList <> ( sources.size ( ) ) ;            // Spring AI Document
		Iterator <String> docIdIter = documentIds.iterator ( ) ;
		for ( InputStreamSource source : sources ) {
			String docId = docIdIter.hasNext ( ) ? docIdIter.next ( ) : UUID.randomUUID ( ).toString ( ) ;
			Map <String, Object> meta = ( source instanceof ProxyInputStreamSource pis ) ? pis.getMetadata ( ) : new HashMap <> ( ) ;
			ctx.setCurrentFile ( source, meta ) ;
			ctx.setProperty ( HybridizableRagIngestionKey.FILE_ID, docId ) ;
			ctx.setProperty ( HybridizableRagIngestionKey.META, meta ) ;
			if ( ! parser.supports ( xctx.getContentType ( ), ctx ) ) {
				throw new IllegalArgumentException ( "No parser available for content type " + xctx.getContentType ( ) ) ;
			}
			try {
				List <dev.langchain4j.data.document.Document> innerDocs = parser.parse ( source, ctx ) ;
				for ( dev.langchain4j.data.document.Document doc : innerDocs ) {
					ctx.setCurrentDocument ( doc ) ;
					ctx.setProperty ( HybridizableRagIngestionKey.DOC_ID, docId ) ;
					ctx.setProperty ( HybridizableRagIngestionKey.DOC_POSITION, new AtomicInteger ( 0 ) ) ;
					List <TextSegment> segments = chunker.split ( doc, ctx ) ;           // LangChain4j TextSegment
					for ( TextSegment seg : segments ) {
						ctx.setCurrentSegment ( seg ) ;
						batch.add ( mapper.toSpringDoc ( seg, ctx ) ) ;               // Spring AI Document
						if ( batch.size ( ) >= batchSize ) {
							persistToStore ( batch, ctx ) ;                                // persists to PG + embeddings
							batch.clear ( ) ;
						}
						ctx.setCurrentSegment ( null ) ;
					}
					ctx.setCurrentDocument ( null ) ;
				}

				ctx.setCurrentFile ( null, null ) ;
				if ( ! batch.isEmpty ( ) ) {
					persistToStore ( batch, ctx ) ;
				}
			} catch ( IOException e ) {
				throw new UncheckedIOException ( e ) ;
			}
		}
	}

	/*
	private void insertOrUpdateBatch(List<Document> batch, List<Document> documents, List<float[]> embeddings) {
		String sql = "INSERT INTO " + getFullyQualifiedTableName()
				+ " (id, content, metadata, embedding) VALUES (?, ?, ?::jsonb, ?) " + "ON CONFLICT (id) DO "
				+ "UPDATE SET content = ? , metadata = ?::jsonb , embedding = ? ";

	 */

	/*
	create table flexirag (
	    id uuid primary key default gen_random_uuid(),
	    content text not null,
	    metadata jsonb,
	    embedding vector(1536) not null,
	    fts tsvector generated always as (to_tsvector('english', content)) stored,
	    tenant varchar,
	    batch_id uuid,
	    collection varchar,
	    doc_id uuid,
	    session_id uuid,
	    request_id uuid,
	    created_at timestamptz default current_timestamp,
	    constraint unique_chunk_per_doc unique (doc_id, tenant, collection, batch_id
	  ) ;
	  create index idx_flexirag_fts on flexirag using gin(fts) ;
	  create index idx_flexirag_content on flexirag using gin(to_tsvector('english', content)) ;
	  create index idx_flexirag_metadata on flexirag using gin(metadata) ;
	  create index idx_flexirag_embedding on flexirag using hnsw (embedding vector_cosine_ops) with (lists = 100) ;
	  create index idx_flexirag_tenant on flexirag (tenant) ;
	  create index idx_flexirag_collection on flexirag (collection) ;
	  create index idx_flexirag_doc_id on flexirag (doc_id) ;
	  create index idx_flexirag_batch_id on flexirag (batch_id) ;
	  create index idx_flexirag_session_id on flexirag (session_id) ;
	  create index idx_flexirag_request_id on flexirag (request_id) ;
	 */
	private void persistToStore ( List <Document> batch, RagIngestionTreeContext ctx ) {
		String tableName = identifyTable ( ctx ) ;
		List <float [ ]> embeddings = embed ( batch, ctx ) ;
		List <List <Document>> docBatches = batchDocuments ( batch, ctx ) ;
		List <List <float [ ]>> embeddingBatches = batchEmbeddings ( embeddings, ctx, docBatches ) ;

		// insert into flexirag (id, content, metadata, embedding, tenant, batch_id, collection, doc_id, session_id, request_id) values (?, ?, ?, ?, ?, ?, ?)

		String sqlTemplate = """
				insert into %s (content, metadata, embedding %s) values (?, ?::jsonb, ? %s)
				""" ;

		Map <String, SqlType> additionalColumns = getAdditionalColumns ( ctx ) ;
		Map <String, Object> additionalValues = getAdditionalValues ( ctx, additionalColumns ) ;
		String colNames = String.join ( ", ", additionalColumns.keySet ( ) ) ;
		String additionalPlaceholders = additionalColumns.keySet ( ).stream ( ).map ( k -> ", ?" ).reduce ( ", ", String::concat ) ;
		String sql = sqlTemplate.formatted ( tableName, colNames, additionalPlaceholders ) ;

		int batchNum = 0 ;
		for ( List <Document> currentBatch : docBatches ) {
			List <float [ ]> currentEmbeddings = embeddingBatches.get ( batchNum++ ) ;
			BatchPreparedStatementSetter prep = new BatchPreparedStatementSetter ( ) {
	
				@Override
				public void setValues ( PreparedStatement ps, int batchIndex ) throws SQLException {
					int i = 1 ;
					Document doc = currentBatch.get ( batchIndex ) ;
					// ID will be autogen; start with content
					StatementCreatorUtils.setParameterValue ( ps, i++, SqlTypeValue.TYPE_UNKNOWN, tableName, doc.getText ( ) ) ;
					StatementCreatorUtils.setParameterValue ( ps, i++, SqlTypeValue.TYPE_UNKNOWN, tableName, toJson ( doc.getMetadata ( ) ) ) ;
					StatementCreatorUtils.setParameterValue ( ps, i++, SqlTypeValue.TYPE_UNKNOWN, tableName, new PGvector ( currentEmbeddings.get ( batchIndex ) ) ) ;
					// additional columns
					for ( Map.Entry <String, SqlType> entry : additionalColumns.entrySet ( ) ) {
						String col = entry.getKey ( ) ;
						int sqlType = entry.getValue ( ).vendorTypeNumber ( ) ;
						String typeName = entry.getValue ( ).typeName ( ) ;
						Object val = additionalValues.get ( col ) ;
						StatementCreatorUtils.setParameterValue ( ps, i++, sqlType, typeName, val ) ;
					}
				}

				@Override
				public int getBatchSize ( ) {
					return currentBatch.size ( ) ;
				}
			} ;
			jdbc.batchUpdate ( sql, prep ) ;
		}
	}

	private Map <String, Object> getAdditionalValues ( RagIngestionTreeContext ctx, Map <String, SqlType> additionalColumns ) {
		Map <String, Object> retVal = new LinkedHashMap <> ( ) ;
		String collection = ctx.getProperty ( HybridizableRagIngestionKey.COLLECTION, String.class ) ;
		String tenant = ctx.getProperty ( HybridizableRagIngestionKey.TENANT, String.class ) ;
		String sessionId = ctx.getProperty ( HybridizableRagIngestionKey.SESSION_ID, String.class ) ;
		String batchId = ctx.getProperty ( HybridizableRagIngestionKey.BATCH_ID, String.class ) ;
		String requestId = ctx.getProperty ( HybridizableRagIngestionKey.REQUEST_ID, String.class ) ;

		Map <String, String> t = new HashMap <> ( ) ;
		t.put ( "collection", collection ) ;
		t.put ( "tenant", tenant ) ;
		t.put ( "session_id", sessionId ) ;
		t.put ( "batch_id", batchId ) ;
		t.put ( "request_id", requestId ) ;

		for ( String col : additionalColumns.keySet ( ) ) {
			retVal.put ( col, t.get ( col ) ) ;
		}
		return retVal ;
	}

	private List <List <float [ ]>> batchEmbeddings ( List <float [ ]> embeddings, RagIngestionTreeContext ctx, List <List <Document>> docBatches ) {
		List <List <float [ ]>> retVal = new ArrayList <> ( ) ;
		// mismatach here
		return retVal ;
	}

	private String toJson ( Map <String, Object> metadata ) {
		try {
			return om.writeValueAsString ( metadata ) ;
		} catch ( JsonProcessingException e ) {
			throw new RuntimeException ( e ) ;
		}
	}

	private List <float [ ]> embed ( List <Document> batch, RagIngestionTreeContext ctx ) {
		EmbeddingOptions opts = buildEmbeddingOptions ( ctx ) ;
		BatchingStrategy bs = getBatchingStrategy ( ctx ) ;
		return this.embeddingModel.embed ( batch, opts, bs ) ;
	}

	private record SqlType ( Integer vendorTypeNumber, String typeName ) { ; }

	private Map <String, SqlType> getAdditionalColumns ( RagIngestionTreeContext ctx ) {
		Map <String, SqlType> retVal = new LinkedHashMap <> ( ) ; // preserve order
		retVal.put ( "tenant", new SqlType ( JDBCType.VARCHAR.getVendorTypeNumber ( ), "varchar" ) ) ;
		retVal.put ( "batch_id", new SqlType ( SqlTypeValue.TYPE_UNKNOWN, "UUID" ) ) ;
		retVal.put ( "collection", new SqlType ( SqlTypeValue.TYPE_UNKNOWN, "varchar" ) ) ;
		retVal.put ( "doc_id", new SqlType ( SqlTypeValue.TYPE_UNKNOWN, "UUID" ) ) ;
		retVal.put ( "session_id", new SqlType ( SqlTypeValue.TYPE_UNKNOWN, "UUID" ) ) ;
		retVal.put ( "request_id", new SqlType ( SqlTypeValue.TYPE_UNKNOWN, "UUID" ) ) ;
		return retVal ;
	}

	private List<List<Document>> batchDocuments(List<Document> documents, RagIngestionTreeContext ctx) {
		List<List<Document>> batches = new ArrayList<>();
		for (int i = 0; i < documents.size(); i += ctx.getMaxDocumentBatchSize ( ) ) {
			batches.add(documents.subList(i, Math.min(i + ctx.getMaxDocumentBatchSize ( ), documents.size())));
		}
		return batches;
	}

	/*
	public TokenCountBatchingStrategy(EncodingType encodingType, int maxInputTokenCount, double reservePercentage,
			ContentFormatter contentFormatter, MetadataMode metadataMode) {
	 */
	private BatchingStrategy getBatchingStrategy ( RagIngestionTreeContext ctx ) {
		EncodingType et = EncodingType.O200K_BASE ;
		int maxInputTokenCount = ctx.getProperty ( HybridizableRagIngestionKey.MAX_INPUT_TOKEN_COUNT, Integer.class ) ;
		double reservePercentage = ctx.getProperty ( HybridizableRagIngestionKey.RESERVE_PERCENTAGE, Double.class ) ;
		ContentFormatter cf = DefaultContentFormatter.defaultConfig ( ) ;
		MetadataMode mm = MetadataMode.ALL ;
		return new TokenCountBatchingStrategy ( et, maxInputTokenCount, reservePercentage, cf, mm ) ;
	}

	private EmbeddingOptions buildEmbeddingOptions ( RagIngestionTreeContext ctx ) {
		// TODO Auto-generated method stub
		return null ;
	}

	private String identifyTable ( RagIngestionTreeContext ctx ) {
		return "flexirag" ;
	}

	private CorpusParser getParser ( String contentType ) {
		CorpusParser parser = PARSERS.get ( contentType ) ;
		if ( parser == null ) {
			throw new IllegalArgumentException ( "No parser available for content type " + contentType ) ;
		}
		return parser ;
	}

	/* Follow on question slightly out of left field.  If I wanted to create an ontology of facts in Neo4J that operate on
	   the same principles and even structure of RDF triples, how could I use vector technology to help me with search queries?
	   What would I create my indexes on?
	   Also, in Neo4J could I create multiple vector interfaces for the same piece of data, maybe indexed in different ways for different purposes?  Would there be
	   any advantage in that?  I'm assuming there would be tradeoffs in terms of storage and performance, but maybe there would be some benefit in terms of searchability or flexibility.
	*/
	// TODO: Session id
	public List <Document> search ( String query, DocumentSearchOptions options ) {
		options = options == null ? DocumentSearchOptions.builder ( ).build ( ) : options ; // null implies just take all the defaults
		SearchContext ctx = new SearchContext ( query, options ) ;

		// Order in which we do the searches doesn't matter a whit.  Each set of 
		// results is sequestered separately and we'll consolidate at the end.  Options will determine precedence.

		// 1) Full-text search: find chunks whose fts vector matches the query
		if ( options.doFullTextSearch ( ) ) {
			doFullTextSearch ( ctx, options ) ;
		}

		// 2) Vector search: retrieve the topK most similar by cosine distance
		if ( options.doSearchVectorStore ( ) ) {
			doVectorSearch ( ctx, options ) ;
		}
		// I'm thinking in the future there may be other, wackier search types.
		// This should be flexible enough to accommodate those without too much trouble.
		// Maybe.

		if ( options.doExpandedSearch ( ) ) {
			doExpandedSearch ( ctx, options ) ;
		}

		// 6) Dedupe again.
		ctx.dedupe ( ) ;

		// I'm making a conscious decision to ignore topK here.  It's more of a spiritual topK.
		return ctx.getFinalResults ( ) ;
	}

	@SuppressWarnings ( "unchecked" )
	private void doExpandedSearch ( SearchContext ctx, DocumentSearchOptions options ) {
		Set <DocumentSearchOptions.SearchType> expandedSearchOnResultGroups = options.getResultGroupsForExpandedSearch ( ) ;
		if ( expandedSearchOnResultGroups.size ( ) == DocumentSearchOptions.SearchType.values ( ).length ) {
			// 3) Original comment invalid but this was my original position.  Optimize only if it makes sense at this point.
			ctx.dedupe ( ) ;
		}

		for ( Map.Entry <RagPullerService.ResultGroup, List <Document>> entry : ctx.getClusteredResults ( ).entrySet ( ) ) {
			RagPullerService.ResultGroup resultGroup = entry.getKey ( ) ;
			if ( ! ( resultGroup instanceof HybridResultGroup ) || ! expandedSearchOnResultGroups.contains ( resultGroup ) ) {
				continue ;
			}

			// 4) Convert to cluster for organization and expansion of neighbors while preserving score order of the original context of the findings.
			List <DocCluster> clusters = entry.getValue ( ).stream ( ).map ( d -> asDocCluster ( d ) ).toList ( ) ;

			// 5) Expand neighbors for each vector hit, adding them to the merged results
			for ( DocCluster dc : clusters ) {
				RagPullerService.ResultGroup searchType = dc.provenance ( ) ;
				Integer radius = options.getExpandedSearchRadius ( ).get ( searchType ) ;
				radius = radius == null ? options.getDefaultSearchRadius ( ) : radius ;
				List <Map <String, Object>> window = expandNeighbours ( dc.root ( ), radius ) ;
				for ( Map <String, Object> row : window ) {
					Document.Builder builder = Document.builder ( ) ;
					builder.text ( ( String ) row.get ( "content" ) ) ;
					builder.metadata ( provenance ( HybridResultGroup.NEIGHBOR_EXPANSION, ( Map <String, Object> ) row.get ( "metadata" ) ) ) ;
					builder.score ( 0.0 ) ; // If this wasn't picked up in a prior search, its score must be zero.  If it was picked up we'll have to replace it in the dedupe step later.
					builder.id ( ( String ) row.get ( "id" ) ) ;
					builder.media ( null ) ;
					dc.neighbors ( ).add ( builder.build ( ) ) ;
				}
			}

			ctx.getClusteredResults ( ).get ( entry.getKey ( ) ).clear ( ) ;
			ctx.getClusteredResults ( ).put ( resultGroup, clusters.stream ( ).flatMap ( dc -> dc.neighbors ( ).stream ( ) ).toList ( ) ) ;
		}
	}

	private SearchRequest buildSearchRequest ( SearchContext ctx, DocumentSearchOptions options ) {
		SearchRequest.Builder builder = SearchRequest.builder ( ) ;
		builder.query ( ctx.getQuery ( ) ) ;
		Double similarityThreshold = options.getSimilarityThreshold ( ) ;
		if ( similarityThreshold == null ) {
			builder.similarityThresholdAll ( ) ;
		} else {
			builder.similarityThreshold ( similarityThreshold ) ;
		}
		Integer topK = options.getSearchTopK ( ).get ( DocumentSearchOptions.SearchType.VECTOR ) ;
		if ( topK != null ) {
			builder.topK ( topK ) ;
		}
		Expression filterExpression = options.getFilterExpression ( ) ;
		if ( filterExpression != null ) {
			builder.filterExpression ( filterExpression ) ;
		} else {
			String strFilterExpression = options.getFilterExpressionString ( ) ;
			if ( strFilterExpression != null && ! strFilterExpression.isBlank ( ) ) {
				builder.filterExpression ( strFilterExpression ) ;
			}
		}

		return builder.build ( ) ;
	}

	private void doVectorSearch ( SearchContext ctx, DocumentSearchOptions options ) {
		String table = identifyTable ( ctx, options ) ;
		if ( ! canFulfill ( ctx, options, table ) ) {
			throw new IllegalArgumentException ( "Cannot fulfill vector search request with given options and table " + table ) ;
		}
		QueryParts qp = new QueryParts ( ) ;
		qp.addFrom ( table ) ;
		Integer searchTopK = options.getSearchTopK ( ).get ( DocumentSearchOptions.SearchType.VECTOR ) ;
		if ( searchTopK != null ) {
			qp.setLimit ( searchTopK ) ;
		}
		String sql = templateToSql ( qp, ctx, options ) ;
		List <Document> vectorDocs = jdbc.query ( sql, ( rs, rowNum ) -> {
			PGobject metadataObj = ( PGobject ) rs.getObject ( "metadata" ) ;
			Map <String, Object> metadataMap = pgObjectToMap ( metadataObj ) ;
			return Document.builder ( )
				.id ( rs.getString ( "id" ) )
				.text ( rs.getString ( "content" ) )
				.metadata ( provenance ( DocumentSearchOptions.SearchType.VECTOR, metadataMap ) )
				.score ( rs.getDouble ( "score" ) )
				.build ( ) ;
		}, qp.toValuesArray ( ) ) ;

//		List <Document> vectorDocs = provenance ( DocumentSearchOptions.SearchType.VECTOR, vectorStore.similaritySearch ( request ) ) ;
		vectorDocs = vectorDocs.stream ( ).sorted ( highestScoreFirst ).limit ( options.getTopK ( ) ).toList ( ) ;

		ctx.getClusteredResults ( ).put ( DocumentSearchOptions.SearchType.VECTOR, vectorDocs ) ;
	}

	private String templateToSql ( QueryParts qp, SearchContext ctx, DocumentSearchOptions options ) {
		String sqlTemplate = TEMPLATES.get ( options.getPreferredOperator ( ) ) ;
		SearchRequest request = buildSearchRequest ( ctx, options ) ;  // includes similarity threshold, topK, filter expression
		double distance = 1 - request.getSimilarityThreshold ( ) ;
		PGvector vec = vectorize ( ctx, options ) ;
		qp.addWhereValue ( vec ) ;
		qp.addWhereValue ( vec ) ;
		qp.addWhereValue ( distance ) ;
		qp.addWhereValue ( request.getFilterExpression ( ).toString ( ) ) ;
		qp.addSelectAndWhereClauseIfWarranted ( "tenant", options.getTenant ( ) ) ;
		qp.addSelectAndWhereClauseIfWarranted ( "collection", options.getCollection ( ) ) ;
		qp.addSelectAndWhereClauseIfWarranted ( "doc_id", options.getDocId ( ) ) ;

		// SqlTemplate is parameterized by select clause, table name, where clause, order by clause, limit clause
		return sqlTemplate.formatted ( qp.selectWords ( ), qp.tableName ( ), qp.toWhereSubclauses ( ), qp.limitValue ( ) ) ;
	}

	private boolean canFulfill ( SearchContext ctx, DocumentSearchOptions options, String table ) {
		return true ;
	}

	private String identifyTable ( SearchContext ctx, DocumentSearchOptions options ) {
		return "flexirag" ;
	}

	@SuppressWarnings ( "unchecked" )
	private Map <String, Object> pgObjectToMap ( PGobject metadataObj ) {
		
		try {
			String metadataJson = metadataObj.getValue ( ) ;
			return om.readValue ( metadataJson, Map.class ) ;
		} catch ( Exception e ) {
			throw new RuntimeException ( "Failed to convert PGobject to Map<String, Object>", e ) ;
		}
	}

	private PGvector vectorize ( SearchContext ctx, DocumentSearchOptions options ) {
		float [ ] embedding = embeddingModel.embed ( ctx.getQuery ( ) ) ;
		return new PGvector ( embedding ) ;
	}

	private class QueryParts {

		private List <String> selectWords = new ArrayList <> ( ) ;
		private List <String> whereWords = new ArrayList <> ( ) ;
		private List <Object> whereVals = new ArrayList <> ( ) ;
		private String table ;
		private Integer limit ;
		public void addSelectAndWhereClauseIfWarranted ( String keyword, Object value ) {
			if ( value != null && String.valueOf ( value ).trim ( ).isEmpty ( ) ) {
				selectWords.add ( keyword ) ;
				whereWords.add ( keyword ) ;
				whereVals.add ( value ) ;
			}
		}

		public Object tableName ( ) {
			return table ;
		}

		public Integer limitValue ( ) {
			return limit ;
		}

		public Object selectWords ( ) {
			return selectWords.stream ( ).reduce ( ( a, b ) -> a + ", " + b ).orElse ( "*" ) ;
		}

		public void addFrom ( String table ) {
			this.table = table ;
		}

		public void setLimit ( Integer searchTopK ) {
			this.limit = searchTopK ;
		}

		public void addWhereValue ( Object arg ) {
			whereVals.add ( arg ) ;
		}

		public String toWhereSubclauses ( ) {
			if ( whereWords.isEmpty ( ) ) {
				return "" ;
			}
			StringBuilder sb = new StringBuilder ( " " ) ;
			for ( int i = 0 ; i < whereWords.size ( ) ; i++ ) {
				if ( i > 0 ) {
					sb.append ( " AND " ) ;
				}
				if ( "NULL".equalsIgnoreCase ( String.valueOf ( whereVals.get ( i ) ) ) ) {
					sb.append ( whereWords.get ( i ) ).append ( " IS NULL" ) ;
					continue ;
				}
				sb.append ( whereWords.get ( i ) ).append ( " = ?" ) ;
			}
			sb.append ( " AND" ) ; // need to finish the last AND for the vector search clause that follows

			return sb.toString ( ) ;
		}

		public Object[] toValuesArray ( ) {
			// must exclude null String whereVals from the array, since they are not used in the prepared statement
			List <Object> tmp = new ArrayList <> ( whereVals ) ;
			tmp.add ( limit ) ;
			return tmp.stream ( ).filter ( v -> ! "NULL".equalsIgnoreCase ( String.valueOf ( v ) ) ).toArray ( ) ;
		}
	}

	private void doFullTextSearch ( SearchContext ctx, DocumentSearchOptions options ) {
		String sql = "SELECT id, content, metadata FROM flexirag WHERE fts @@ websearch_to_tsquery(?, ?)" ;
		List <Map <String, Object>> ftsRows = jdbc.queryForList ( sql, options.getSearchLanguage ( ), ctx.getQuery ( ) ) ;

		// Convert full-text rows to Spring AI Document
		List <Document> ftsDocs = new ArrayList <> ( ) ;
		for ( Map <String, Object> row : ftsRows ) {
			// When in Rome, I guess.  You can't set score any other way without a Media element.
			@SuppressWarnings ( "unchecked" )
			Document d = Document.builder ( )
				.text ( ( String ) row.get ( "content" ) )
				.metadata ( provenance ( DocumentSearchOptions.SearchType.FULL_TEXT, ( Map <String, Object> ) row.get ( "metadata" ) ) )
				.score ( options.getDefaultFullTextScore ( ) )
				.build ( ) ;
			ftsDocs.add ( d ) ;
		}

		Integer topK = options.getSearchTopK ( ).get ( DocumentSearchOptions.SearchType.FULL_TEXT ) ;
		if ( topK != null && topK > 0 && ftsDocs.size ( ) > topK ) {
			ftsDocs = ftsDocs.stream ( ).limit ( topK ).toList ( ) ;
		}
		ctx.getClusteredResults ( ).put ( DocumentSearchOptions.SearchType.FULL_TEXT, ftsDocs ) ;
	}

	private Map <String, Object> provenance ( RagPullerService.ResultGroup resultType, Map <String, Object> map ) {
		try {
			map.put ( "rag_pull_search_type", resultType ) ;
			return map ;
		} catch ( UnsupportedOperationException e ) {
			Map <String, Object> newMap = new LinkedHashMap <> ( map ) ;
			newMap.put ( "rag_pull_search_type", resultType ) ;
			return newMap ;
		}
	}

	@SafeVarargs
	private List <Document> dedupe ( List <Document> ... ddocs ) {
		Map <String, Document> merged = new LinkedHashMap <> ( ) ;
		for ( List <Document> docs : ddocs ) {
			for ( Document vd : docs ) {
				String docId = vd.getMetadata ( ).get ( "doc_id" ).toString ( ) ;
				// If we already have a document with this docId, prefer the copy with the higher score.
				Document alt = merged.get ( docId ) ;
				if ( alt == null || vd.getScore ( ) > alt.getScore ( ) ) {
					merged.put ( docId, vd ) ;
				}
			}
		}

		return new ArrayList <> ( merged.values ( ) ) ;
	}

	private DocCluster asDocCluster ( Document d ) {
		String docId = d.getMetadata ( ).get ( "doc_id" ).toString ( ) ;
		Double score = d.getScore ( ) ;
		SortedSet <Document> neighbors = new TreeSet <> ( documentOrder ) ;
		RagPullerService.ResultGroup provenance = ( RagPullerService.ResultGroup ) d.getMetadata ( ).get ( "rag_pull_search_type" ) ;
		neighbors.add ( d ) ;
		return new DocCluster ( docId, score, d, provenance, neighbors ) ;
	}

	private static final Comparator <Document> highestScoreFirst = new Comparator <Document> ( ) {

		@Override
		public int compare ( Document o1, Document o2 ) {
			Double score1 = o1.getScore ( ) ;
			Double score2 = o2.getScore ( ) ;
			return score2.compareTo ( score1 ) ;
		}
	} ;

	private static final Comparator <Document> documentOrder = new Comparator <Document> ( ) {

		@Override
		public int compare ( Document o1, Document o2 ) {
			Integer pos1 = Integer.parseInt ( o1.getMetadata ( ).get ( "doc_position" ).toString ( ) ) ;
			Integer pos2 = Integer.parseInt ( o2.getMetadata ( ).get ( "doc_position" ).toString ( ) ) ;
			return pos1.compareTo ( pos2 ) ;
		}
	} ;

	public List <Map <String, Object>> expandNeighbours ( Document hit, int radius ) {
		String docId = hit.getMetadata ( ).get ( "doc_id" ).toString ( ) ;
		int idx = Integer.parseInt ( hit.getMetadata ( ).get ( "chunk_index" ).toString ( ) ) ;
		// fetch index ±1, ±2, … depending on window size
		return jdbc.queryForList ( "SELECT id, content, metadata FROM flexirag " + "WHERE metadata->>'doc_id' = ? AND (metadata->>'doc_position')::int BETWEEN ? AND ?", docId, idx - radius,
				idx + radius ) ;
	}

	private record DocCluster ( String docId, Double score, Document root, RagPullerService.ResultGroup provenance, SortedSet <Document> neighbors ) {
		;
	}

	@Data
	private class SearchContext {
		private final String query ;
		private final DocumentSearchOptions options ;
		private Map <RagPullerService.ResultGroup, List <Document>> clusteredResults = new LinkedHashMap <> ( ) ;

		public SearchContext ( String query, DocumentSearchOptions options ) {
			String tsquery = query.replaceAll ( "[^\\w\\s]", " " ) ; // basic sanitization
			this.query = tsquery ;
			this.options = options ;
		}

		public List <Document> getFinalResults ( ) {
			return clusteredResults.values ( ).stream ( ).flatMap ( List::stream ).toList ( ) ;
		}

		public void dedupe ( ) {
			List <List <Document>> allDocs = new ArrayList <> ( clusteredResults.values ( ) ) ;
			@SuppressWarnings ( "unchecked" )
			List <Document> deduped = HybridRagService.this.dedupe ( allDocs.toArray ( new List[0] ) ) ;
			clusteredResults.clear ( ) ;
			clusteredResults.put ( HybridResultGroup.HYBRID_RESULTS, deduped ) ;
		}
	}
}
