package bill.zeacc.salieri.fourthgraph.rag;

import lombok.Builder ;
import lombok.Data ;

@Data
@Builder
public class RagIngestionContext {

	private String batchId ;
	private String contentType ;
	private String collection ;
	private String tenant ;
	private String sessionId ;
	private String provenance ;
}
