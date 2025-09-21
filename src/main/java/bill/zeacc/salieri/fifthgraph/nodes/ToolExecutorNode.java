package bill.zeacc.salieri.fifthgraph.nodes ;

import org.bsc.langgraph4j.action.NodeAction ;
//ToolExecutorNode.java

import bill.zeacc.salieri.fifthgraph.model.meta.InternalTool ;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolCall ;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolChooser ;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolResponse ;
import bill.zeacc.salieri.fifthgraph.model.states.ToolOrientedState ;
import lombok.extern.slf4j.Slf4j ;
import java.util.* ;

@Slf4j
public abstract class ToolExecutorNode implements NodeAction <ToolOrientedState> {

	private final Map <String, InternalTool> toolMap ;

	protected ToolExecutorNode ( ToolChooser toolProvider ) {
		List <InternalTool> tools =  toolProvider == null ? Collections.emptyList ( ) : toolProvider.get ( ) ;
		this.toolMap = new HashMap <> ( ) ;
		for ( InternalTool tool : tools ) {
			toolMap.put ( tool.getName ( ), tool ) ;
		}
	}

	@Override
	public Map <String, Object> apply ( ToolOrientedState state ) {
		List <ToolCall> toolCalls = state.getToolCalls ( ) ;

		if ( toolCalls.isEmpty ( ) ) {
			log.debug ( "No tools to execute" ) ;
			return Map.of ( ) ;
		}

		log.info ( "Executing {} tool(s)", toolCalls.size ( ) ) ;
		List <ToolResponse> results = new ArrayList <> ( ) ;

		for ( ToolCall call : toolCalls ) {
			InternalTool tool = toolMap.get ( call.getName ( ) ) ;
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

		return Map.of ( ToolOrientedState.TOOL_RESULTS_KEY, results ) ;
	}
}
