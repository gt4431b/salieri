package bill.zeacc.salieri.fifthgraph.components.meta;

import org.bsc.langgraph4j.state.AgentState ;

@FunctionalInterface
public interface StateBuilder <T extends AgentState> {

	public T build ( String initialQuery ) ;
}
