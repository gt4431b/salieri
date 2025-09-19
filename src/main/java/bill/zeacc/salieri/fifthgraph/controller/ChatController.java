package bill.zeacc.salieri.fifthgraph.controller ;

import org.springframework.web.bind.annotation.* ;

import bill.zeacc.salieri.fifthgraph.service.GraphService ;

import org.bsc.langgraph4j.state.AgentState ;
import org.springframework.http.ResponseEntity ;
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
	public ResponseEntity <ChatResponse> chat ( @RequestBody ChatRequest request ) {
		log.info ( "Received chat request: {}", request.query ( ) ) ;
		AgentState response = graphService.processQuery ( request.agentName ( ), request.query ( ), "temp-session-id" ) ;
		String result = ( String ) response.value ( "final_answer" ).orElse ( "No answer generated." ) ;
		return ResponseEntity.ok ( new ChatResponse ( result ) ) ;
	}

	@GetMapping ( "/health" )
	public ResponseEntity <String> health ( ) {
		return ResponseEntity.ok ( "OK" ) ;
	}
}
