package bill.zeacc.salieri.fourthgraph.rag ;

import dev.langchain4j.data.document.Document ;               // <-- LangChain4j Document
import dev.langchain4j.data.segment.TextSegment ;             // <-- LangChain4j TextSegment
import org.springframework.ai.vectorstore.VectorStore ;
import org.springframework.stereotype.Component ;

import java.io.File ;
import java.io.IOException ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.UUID ;
import java.util.concurrent.atomic.AtomicInteger ;

@Component
public class RagIngestService {

	private final VectorStore vectorStore ;

	// Parsing/splitting/mapping strategy objects
	private final List <CorpusParser> parsers ;
	private final TextChunker chunker ;
	private final SegmentMapper mapper ;

	private final int batchSize = 200 ;

	public RagIngestService ( VectorStore vectorStore ) {
		this.vectorStore = vectorStore ;

		// stable default implementations
		this.parsers = List.of ( new PdfBoxCorpusParser ( ) ) ;
		this.chunker = new TextSplitterChunker ( 800, 120 ) ;                 // paragraph → sentence fallback
		this.mapper = new HybridizingSegmentMapper ( true /* deterministicIds */ ) ;
	}

	/** Walk a directory and ingest supported files */
	public void indexPath ( Path root ) throws IOException {
		RagIngestionContext ctx = new RagIngestionContext ( ) ;
		ctx.setBatchRoot ( root ) ;
		ctx.setProperty ( HybridizableRagIngestionKey.BATCH_ID, UUID.randomUUID ( ).toString ( ) ) ;
		List <File> files = collectFiles ( root ) ;

		List <org.springframework.ai.document.Document> batch = new ArrayList <> ( batchSize ) ;            // Spring AI Document
		for ( File file : files ) {
			CorpusParser parser = findParser ( file, ctx ) ;
			if ( parser == null )
				continue ;

			ctx.setCurrentFile ( file ) ;
			ctx.setProperty ( HybridizableRagIngestionKey.FILE_ID, UUID.randomUUID ( ).toString ( ) ) ;
			List <Document> docs = parser.parse ( file, ctx ) ;                      // LangChain4j Document
			for ( Document doc : docs ) {
				ctx.setCurrentDocument ( doc ) ;
				ctx.setProperty ( HybridizableRagIngestionKey.DOC_ID, UUID.randomUUID ( ).toString ( ) ) ;
				ctx.setProperty ( HybridizableRagIngestionKey.DOC_POSITION, new AtomicInteger ( 0 ) ) ;
				List <TextSegment> segments = chunker.split ( doc, ctx ) ;           // LangChain4j TextSegment
				for ( TextSegment seg : segments ) {
					ctx.setCurrentSegment ( seg ) ;
					batch.add ( mapper.toSpringDoc ( seg, ctx ) ) ;               // Spring AI Document
					if ( batch.size ( ) >= batchSize ) {
						vectorStore.add ( batch ) ;                                // persists to PG + embeddings
						batch.clear ( ) ;
					}
					ctx.setCurrentSegment ( null ) ;
				}
				ctx.setCurrentDocument ( null ) ;
			}

			ctx.setCurrentFile ( null ) ;
		}
		if ( ! batch.isEmpty ( ) ) {
			vectorStore.add ( batch ) ;
		}
	}

	private List <File> collectFiles ( Path root ) throws IOException {
		List <File> files = new ArrayList <> ( ) ;
		Files.walk ( root ).filter ( Files::isRegularFile ).forEach ( p -> files.add ( p.toFile ( ) ) ) ;
		return files ;
	}

	private CorpusParser findParser ( File file, RagIngestionContext ctx ) {
		for ( CorpusParser p : parsers ) {
			if ( p.supports ( file, ctx ) )
				return p ;
		}
		return null ;
	}
}
