package bill.zeacc.salieri.fifthgraph.agents.switchboard;

import java.util.HashMap ;
import java.util.Map ;

import org.bsc.langgraph4j.state.Channel ;
import org.bsc.langgraph4j.state.Channels ;

import bill.zeacc.salieri.fifthgraph.model.states.ResultOrientedState ;
import bill.zeacc.salieri.fifthgraph.model.states.ToolOrientedState ;

public class SwitchboardState extends ResultOrientedState {

	public static final Map <String, Channel <?>> SCHEMA = new HashMap <> ( ) ;
	public static final String RECOMMENDED_AGENT_KEY = "recommended_agent" ;
	public static final String CONFIDENCE_KEY = "confidence" ;

	static {
		SCHEMA.putAll ( ToolOrientedState.SCHEMA ) ;
		SCHEMA.put ( RECOMMENDED_AGENT_KEY.toString ( ), Channels.base ( ( a, b ) -> b ) ) ;
		SCHEMA.put ( CONFIDENCE_KEY.toString ( ), Channels.base ( ( a, b ) -> b ) ) ;
	}

	public SwitchboardState ( ) {
		super ( new HashMap <> ( ) ) ;
	}

	public SwitchboardState ( Map <String, Object> initData ) {
		super ( initData ) ;
	}
}
