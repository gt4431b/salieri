package bill.zeacc.salieri.fourthgraph ;

//GraphState.java
import org.bsc.langgraph4j.state.AgentState ;
import org.bsc.langgraph4j.state.Channel ;
import org.bsc.langgraph4j.state.Channels ;

import java.util.* ;

public class GraphState extends AgentState {

	public static final String ANALYSIS_KEY = "analysis" ;
	public static final String QUERY_KEY = "query" ;
	public static final String MESSAGES_KEY = "messages" ;
	public static final String TOOL_CALLS_KEY = "tool_calls" ;
	public static final String TOOL_RESULTS_KEY = "tool_results" ;
	public static final String FINAL_ANSWER_KEY = "final_answer" ;

	public static final Map <String, Channel <?>> SCHEMA = Map.of (
			QUERY_KEY, Channels.base ( ( a, b ) -> b ),
			MESSAGES_KEY, Channels.appender ( ArrayList::new ),
			TOOL_CALLS_KEY, Channels.base ( ( a, b ) -> b ),
			TOOL_RESULTS_KEY, Channels.base ( ( a, b ) -> b ),
			FINAL_ANSWER_KEY, Channels.base ( ( a, b ) -> b ),
			ANALYSIS_KEY, Channels.base ( ( a, b ) -> b ) ) ;

	public GraphState ( ) {
		super ( new HashMap <> ( ) ) ;
	}

	public GraphState ( Map <String, Object> initData ) {
		super ( initData ) ;
	}

	public String getQuery ( ) {
		return this.<String>value ( QUERY_KEY ).orElse ( "" ) ;
	}

	public List <ChatMsg> getMessages ( ) {
		return this.<List <ChatMsg>>value ( MESSAGES_KEY ).orElse ( List.of ( ) ) ;
	}

	public List <ToolCall> getToolCalls ( ) {
		return this.<List <ToolCall>>value ( TOOL_CALLS_KEY ).orElse ( List.of ( ) ) ;
	}

	public List <ToolResponse> getToolResults ( ) {
		return this.<List <ToolResponse>>value ( TOOL_RESULTS_KEY ).orElse ( List.of ( ) ) ;
	}

	public String getFinalAnswer ( ) {
		return this.<String>value ( FINAL_ANSWER_KEY ).orElse ( "" ) ;
	}
}
