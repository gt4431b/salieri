package bill.zeacc.salieri.fourthgraph ;

import org.bsc.langgraph4j.action.NodeAction ;
//ToolExecutorNode.java
import org.springframework.stereotype.Component ;

import lombok.extern.slf4j.Slf4j ;
import java.util.* ;

@Component
@Slf4j
public class ToolExecutorNode implements NodeAction <GraphState> {

	private final Map <String, SpringTool> toolMap ;

	public ToolExecutorNode ( List <SpringTool> tools ) {
		this.toolMap = new HashMap <> ( ) ;
		for ( SpringTool tool : tools ) {
			toolMap.put ( tool.getName ( ), tool ) ;
		}
	}

	@Override
	public Map <String, Object> apply ( GraphState state ) {
		List <ToolCall> toolCalls = state.getToolCalls ( ) ;

		if ( toolCalls.isEmpty ( ) ) {
			log.debug ( "No tools to execute" ) ;
			return Map.of ( ) ;
		}

		log.info ( "Executing {} tool(s)", toolCalls.size ( ) ) ;
		List <ToolResponse> results = new ArrayList <> ( ) ;

		for ( ToolCall call : toolCalls ) {
			SpringTool tool = toolMap.get ( call.getName ( ) ) ;
			if ( tool != null ) {
				try {
					ToolResponse result = tool.execute ( call.getArguments ( ) ) ;
					result.setId ( call.getId ( ) ) ;
					results.add ( result ) ;
					log.info ( "Executed {}: {}", call.getName ( ), result ) ;
				} catch ( Exception e ) {
					log.error ( "Error executing tool {}: {}", call.getName ( ), e.getMessage ( ) ) ;
				}
			} else {
				log.warn ( "Tool not found: {}", call.getName ( ) ) ;
			}
		}

		return Map.of ( GraphState.TOOL_RESULTS_KEY, results ) ;
	}
}
