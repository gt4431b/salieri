package bill.zeacc.salieri.fifthgraph.rags;

import java.util.UUID ;

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
	@Builder.Default
	private Integer maxInputTokenCount = 2000 ;
	@Builder.Default
	private Double reservePercentage = 0.2d ;
	@Builder.Default
	private String requestId = UUID.randomUUID ( ).toString ( ) ;
}
