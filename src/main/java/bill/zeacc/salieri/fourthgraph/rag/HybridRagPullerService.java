package bill.zeacc.salieri.fourthgraph.rag;

import java.util.ArrayList ;
import java.util.LinkedHashMap ;
import java.util.List ;
import java.util.Map ;

import org.springframework.ai.document.Document ;
import org.springframework.ai.vectorstore.SearchRequest ;
import org.springframework.ai.vectorstore.VectorStore ;
import org.springframework.jdbc.core.JdbcTemplate ;

public class HybridRagPullerService implements RagPullerService {
	private final JdbcTemplate jdbc;
    private final VectorStore vectorStore;

    public HybridRagPullerService (JdbcTemplate jdbc, VectorStore vectorStore) {
        this.jdbc = jdbc;
        this.vectorStore = vectorStore;
    }

    @SuppressWarnings ( "unchecked" )
	public List <Document> search(String query, int topK) {
        // 1) Full-text search: find chunks whose fts vector matches the query
        String tsquery = query.replaceAll("[^\\w\\s]", " "); // basic sanitization
        List<Map<String,Object>> ftsRows = jdbc.queryForList(
            "SELECT id, content, metadata FROM rag_chunks " +
            "WHERE fts @@ websearch_to_tsquery('english', ?)",
            tsquery);

        // Convert full-text rows to Spring AI Document
        List<Document> ftsDocs = new ArrayList<>();
        for (Map<String,Object> row : ftsRows) {
            ftsDocs.add(new Document(
                (String) row.get("content"),
                (Map<String,Object>) row.get("metadata")));
        }

        // 2) Vector search: retrieve the topK most similar by cosine distance
        SearchRequest request = SearchRequest.builder()
            .query(query)
            .topK(topK)
            .similarityThreshold(0.0)
            .build();

        List<Document> vectorDocs = vectorStore.similaritySearch(request);

        // 3) Merge and deduplicate, preferring vector hits but adding FTS hits
        Map<String, Document> merged = new LinkedHashMap<>();
        for (Document d : vectorDocs) {
            merged.put((String) d.getMetadata().get("chunk_id"), d);
        }
        for (Document d : ftsDocs) {
            merged.putIfAbsent((String) d.getMetadata().get("chunk_id"), d);
        }

        // 4) Radius search // REDO!  This is a wild attempt by the LLM but it's not anywhere close to right.  See HybdiridizingSegmentMapper for what's needed.
        // Also, if any chunk pulled is a title card, maybe we should pull the whole doc?
        for ( Document d : vectorDocs ) {
			String chunkId = (String) d.getMetadata().get("chunk_id");
			List<Map<String,Object>> radiusRows = jdbc.queryForList(
				"SELECT id, content, metadata FROM rag_chunks " +
				"WHERE id != ? AND " +
				"cube_distance(embedding, (SELECT embedding FROM rag_chunks WHERE id = ?)) < 0.3", // adjust radius as needed
				chunkId, chunkId);
			for (Map<String,Object> row : radiusRows) {
				Document radiusDoc = new Document(
					(String) row.get("content"),
					(Map<String,Object>) row.get("metadata"));
				merged.putIfAbsent((String) radiusDoc.getMetadata().get("chunk_id"), radiusDoc);
			}
		}

        // Return the first N merged results (you might choose a larger K here)
        return new ArrayList<>(merged.values()).subList(0, Math.min(topK, merged.size()));  // Maybe if we're doing radius search, we should return more than topK?  Or filter by topK before step 4.
    }
}
