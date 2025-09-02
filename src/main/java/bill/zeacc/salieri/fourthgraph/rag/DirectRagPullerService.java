package bill.zeacc.salieri.fourthgraph.rag;

import java.util.List ;

import org.springframework.ai.document.Document ;
import org.springframework.ai.vectorstore.SearchRequest ;
import org.springframework.ai.vectorstore.VectorStore ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.stereotype.Service ;

@Service ( "DirectRagPullerService" )
public class DirectRagPullerService implements RagPullerService {
	@Autowired
	private VectorStore vectorStore;

    /**
     * Returns the top K similar chunks to the query text.
     */
    public List <Document> search ( String query, int topK ) {
        SearchRequest req = SearchRequest.builder()
            .query(query)
            .topK(topK)
            .similarityThreshold(0.0) // adjust threshold if you want to filter weak matches
            .build();
        return vectorStore.similaritySearch(req);
    }
}