package bill.zeacc.salieri.fifthgraph.agents.switchboard.config;

import org.bsc.langgraph4j.GraphStateException ;
import org.bsc.langgraph4j.StateGraph ;
import org.springframework.context.annotation.Bean ;
import org.springframework.context.annotation.Configuration ;

import bill.zeacc.salieri.fifthgraph.agents.switchboard.SwitchboardState ;
import bill.zeacc.salieri.fifthgraph.agents.switchboard.nodes.SwitchboardAnalysisNode ;
import bill.zeacc.salieri.fifthgraph.model.meta.AgentDefinition ;

import static bill.zeacc.salieri.fifthgraph.util.NodeHelper.toAsync ;

@Configuration
public class SwitchboardAgentConfig {

	@Bean
	public AgentDefinition <SwitchboardState> switchboardAgent ( SwitchboardAnalysisNode analysisNode ) throws GraphStateException {

		StateGraph <SwitchboardState> graph = new StateGraph <> ( SwitchboardState.SCHEMA, SwitchboardState::new )
				.addNode ( "analyzer", toAsync ( analysisNode ) )
				.addEdge ( StateGraph.START, "analyzer" )
				.addEdge ( "analyzer", StateGraph.END )
				;

		return new AgentDefinition <SwitchboardState> ( "switchboard_agent", graph, q ->  new SwitchboardState ( ), "A switchboard agent that routes requests to the appropriate specialized agent based on the input query.", "Use this agent when you need to determine which specialized agent is best suited to handle a particular query." ) ;
	}
}
