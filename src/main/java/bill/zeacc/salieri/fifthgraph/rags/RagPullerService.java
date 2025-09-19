package bill.zeacc.salieri.fifthgraph.rags;

import java.util.List ;

import org.springframework.ai.document.Document ;

public interface RagPullerService {

	public interface ResultGroup {
		public String name ( ) ;
		public String namespace ( ) ;
	}

	public List <Document> search ( String query, DocumentSearchOptions options ) ;
}
