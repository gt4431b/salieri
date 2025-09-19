package bill.zeacc.salieri.fifthgraph.model.states;

import java.util.HashMap ;
import java.util.Map ;

import org.bsc.langgraph4j.state.Channel ;
import org.bsc.langgraph4j.state.Channels ;

import bill.zeacc.salieri.fifthgraph.model.codeir.Codebase ;

public class EngineeringState extends ToolOrientedState {

	public static final String CODEBASE_KEY = "codebase" ;
	public static final String SANDBOX_KEY = "sandbox" ;

	public static final Map <String, Channel <?>> SCHEMA = new HashMap <> ( Map.of (
		CODEBASE_KEY, Channels.base ( ( a, b ) -> b ),
		SANDBOX_KEY, Channels.base ( ( a, b ) -> b )
	) ) ;

	static {
		SCHEMA.putAll ( ResultOrientedState.SCHEMA ) ;
	}

	public EngineeringState ( ) {
		super ( new HashMap <> ( ) ) ;
	}

	public EngineeringState ( Map <String, Object> initData ) {
		super ( initData ) ;
	}

	public Codebase getCodebase ( ) {
		return this.<Codebase>value ( CODEBASE_KEY ).orElse ( new Codebase ( ) ) ;
	}

	public Map <String, String> getSandbox ( ) {
		return this.<Map <String, String>>value ( SANDBOX_KEY ).orElse ( new HashMap <> ( ) ) ;
	}
}
