package bill.zeacc.salieri.fourthgraph.rag;

import java.util.List ;

import org.springframework.ai.document.Document ;

public interface RagPullerService {

	public List <Document> search ( String query, int topK ) ;
}
