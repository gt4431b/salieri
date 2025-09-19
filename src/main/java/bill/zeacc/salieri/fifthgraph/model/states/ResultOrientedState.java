package bill.zeacc.salieri.fifthgraph.model.states;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.bsc.langgraph4j.state.AgentState ;
import org.bsc.langgraph4j.state.Channel ;
import org.bsc.langgraph4j.state.Channels ;

import bill.zeacc.salieri.fifthgraph.model.meta.ChatMsg ;

public class ResultOrientedState extends AgentState {

	public static final String FINAL_ANSWER_KEY = "final_answer" ;
	public static final String QUERY_KEY = "query" ;
	public static final String MESSAGES_KEY = "messages" ;

	public static final Map <String, Channel <?>> SCHEMA = Map.of (
			QUERY_KEY, Channels.base ( ( a, b ) -> b ),
			FINAL_ANSWER_KEY, Channels.base ( ( a, b ) -> b ),
			MESSAGES_KEY, Channels.appender ( ArrayList::new )
	) ;

	public ResultOrientedState ( ) {
		super ( new HashMap <> ( ) ) ;
	}

	public ResultOrientedState ( Map <String, Object> initData ) {
		super ( initData ) ;
		
	}

	public String getQuery ( ) {
		return this.<String>value ( QUERY_KEY ).orElse ( "" ) ;
	}

	public List <ChatMsg> getMessages ( ) {
		return this.<List <ChatMsg>>value ( MESSAGES_KEY ).orElse ( List.of ( ) ) ;
	}
	public String getFinalAnswer ( ) {
		return this.<String>value ( FINAL_ANSWER_KEY ).orElse ( "" ) ;
	}
}
