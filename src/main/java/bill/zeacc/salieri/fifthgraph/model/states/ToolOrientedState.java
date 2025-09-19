package bill.zeacc.salieri.fifthgraph.model.states;

import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.bsc.langgraph4j.state.Channel ;
import org.bsc.langgraph4j.state.Channels ;

import bill.zeacc.salieri.fifthgraph.model.meta.ToolCall ;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolResponse ;

public class ToolOrientedState extends ResultOrientedState {

	public static final String TOOL_CALLS_KEY = "tool_calls" ;
	public static final String TOOL_RESULTS_KEY = "tool_results" ;
	public static final String ANALYSIS_KEY = "analysis" ;

	public static final Map <String, Channel <?>> SCHEMA = new HashMap <> ( Map.of (
			TOOL_CALLS_KEY, Channels.base ( ( a, b ) -> b ),
			TOOL_RESULTS_KEY, Channels.base ( ( a, b ) -> b ),
			ANALYSIS_KEY, Channels.base ( ( a, b ) -> b )
	) ) ;

	static {
		SCHEMA.putAll ( ResultOrientedState.SCHEMA ) ;
	}

	public ToolOrientedState ( ) {
		super ( new HashMap <> ( ) ) ;
	}

	public ToolOrientedState ( Map <String, Object> initData ) {
		super ( initData ) ;
	}

	public List <ToolCall> getToolCalls ( ) {
		return this.<List <ToolCall>>value ( TOOL_CALLS_KEY ).orElse ( List.of ( ) ) ;
	}

	public List <ToolResponse> getToolResults ( ) {
		return this.<List <ToolResponse>>value ( TOOL_RESULTS_KEY ).orElse ( List.of ( ) ) ;
	}
}
