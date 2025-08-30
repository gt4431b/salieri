package bill.zeacc.salieri.fourthgraph ;

//ChatController.java
import org.springframework.web.bind.annotation.* ;
import org.springframework.http.ResponseEntity ;
import lombok.Data ;
import lombok.AllArgsConstructor ;
import lombok.extern.slf4j.Slf4j ;

@RestController
@RequestMapping ( "/api/chat" )
@Slf4j
public class ChatController {

	private final GraphService graphService ;

	public ChatController ( GraphService graphService ) {
		this.graphService = graphService ;
	}

	@PostMapping
	public ResponseEntity <CChatResponse> chat ( @RequestBody ChatRequest request ) {
		log.info ( "Received chat request: {}", request.getQuery ( ) ) ;
		String response = graphService.processQuery ( request.getQuery ( ) ) ;
		return ResponseEntity.ok ( new CChatResponse ( response ) ) ;
	}

	@GetMapping ( "/health" )
	public ResponseEntity <String> health ( ) {
		return ResponseEntity.ok ( "OK" ) ;
	}
}

@Data
class ChatRequest {

	private String query ;
}

@Data
@AllArgsConstructor
class CChatResponse {

	private String response ;
}
