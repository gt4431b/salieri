package bill.zeacc.salieri.fifthgraph.model.meta;

import org.bsc.langgraph4j.StateGraph ;
import org.bsc.langgraph4j.state.AgentState ;

import bill.zeacc.salieri.fifthgraph.components.meta.StateBuilder ;

public record AgentDefinition <T extends AgentState> ( String name, StateGraph <T> graph, StateBuilder <T> stateBuilder, String description, String hintsForUse ) {

	public AgentDescriptor toDescriptor ( ) {
		return new AgentDescriptor( name, description, hintsForUse ) ;
	}
}
