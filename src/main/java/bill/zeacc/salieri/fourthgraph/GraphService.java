package bill.zeacc.salieri.fourthgraph ;

//GraphService.java
import org.bsc.langgraph4j.* ;
import org.bsc.langgraph4j.action.AsyncEdgeAction ;
import org.bsc.langgraph4j.action.AsyncNodeAction ;
import org.bsc.langgraph4j.action.NodeAction ;
import org.bsc.langgraph4j.checkpoint.MemorySaver ;
import org.bsc.langgraph4j.state.AgentState ;
import org.springframework.stereotype.Service ;
import lombok.extern.slf4j.Slf4j ;
import java.util.* ;
import java.util.concurrent.CompletableFuture ;

@Service
@Slf4j
public class GraphService {

	private final CompiledGraph <GraphState> compiledGraph ;

	public GraphService ( AnalyzerNode analyzerNode, ToolExecutorNode toolExecutorNode, ResponseFormatterNode responseFormatterNode ) throws GraphStateException {

		// Define conditional edge
		AsyncEdgeAction <GraphState> routeOnTools = ( state ) -> {
			List <ToolCall> toolCalls = state.getToolCalls ( ) ;
			List <ToolResponse> toolResults = state.getToolResults ( ) ;
			if ( ! toolCalls.isEmpty ( ) && toolResults.isEmpty ( ) ) { // Given we're calling this before tool execution, what's the point of checking toolResults?
				return CompletableFuture.completedFuture ( "tool_executor" ) ;
			}
			return CompletableFuture.completedFuture ( "formatter" ) ;
		} ;

		// Build graph
		StateGraph <GraphState> graph = new StateGraph <> ( GraphState.SCHEMA, GraphState::new )
				.addNode ( "analyzer", toAsync ( analyzerNode ) )
				.addNode ( "tool_executor", toAsync ( toolExecutorNode ) )
				.addNode ( "formatter", toAsync ( responseFormatterNode ) )
				.addEdge ( StateGraph.START, "analyzer" )
				.addConditionalEdges ( "analyzer", routeOnTools, Map.of ( "tool_executor", "tool_executor", "formatter", "formatter" ) )
				.addEdge ( "tool_executor", "formatter" )
				.addEdge ( "formatter", StateGraph.END ) ;

		MemorySaver saver = new MemorySaver ( ) ;
		CompileConfig compileConfig = CompileConfig.builder()
	            .checkpointSaver(saver)
	            .build() ;
		this.compiledGraph = graph.compile ( compileConfig ) ;
		log.info ( "Graph compiled successfully" ) ;
	}

	public String processQuery ( String query ) {
		Map <String, Object> inputs = Map.of ( GraphState.QUERY_KEY, query,
				GraphState.MESSAGES_KEY, List.of(new ChatMsg(ChatMsg.Role.USER, query) ) ) ;

		GraphState finalState = null ;
		RunnableConfig runCfg = RunnableConfig.builder()
	            .threadId("user-42")        // <-- reuse this for the same conversation
	            .build();
		try {
			for ( NodeOutput <GraphState> output : compiledGraph.stream ( inputs, runCfg ) ) {
				log.debug ( "Node: {}, Messages: {}", output.node ( ), output.state ( ).getMessages ( ) ) ;
				finalState = output.state ( ) ;
			}
		} catch ( Exception e ) {
			log.error ( "Error processing query", e ) ;
			return "Error processing query: " + e.getMessage ( ) ;
		}

		if ( finalState != null ) {
			return finalState.getFinalAnswer ( ) ;
		}

		return "No response generated" ;
	}

	private static <T extends AgentState> AsyncNodeAction <T> toAsync ( NodeAction <T> action ) {
		return ( state ) -> {
			try {
				return CompletableFuture.completedFuture ( action.apply ( state ) ) ;
			} catch ( Exception e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException ( e ) ;
			}
		} ;
	}
}
