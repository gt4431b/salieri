package bill.zeacc.salieri.fifthgraph.agents.hello ;

//GraphState.java
import org.bsc.langgraph4j.state.Channel ;

import bill.zeacc.salieri.fifthgraph.model.states.ToolOrientedState ;

import java.util.* ;

public class GraphState extends ToolOrientedState {

	public static final Map <String, Channel <?>> SCHEMA = new HashMap <> ( ) ;

	static {
		SCHEMA.putAll ( ToolOrientedState.SCHEMA ) ;
	}

	public GraphState ( ) {
		super ( new HashMap <> ( ) ) ;
	}

	public GraphState ( Map <String, Object> initData ) {
		super ( initData ) ;
	}
}
