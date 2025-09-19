package bill.zeacc.salieri.fifthgraph.agents.justchat.config;

import org.bsc.langgraph4j.GraphStateException ;
import org.bsc.langgraph4j.StateGraph ;
import org.springframework.ai.chat.model.ChatModel ;
import org.springframework.beans.factory.annotation.Qualifier ;
import org.springframework.context.annotation.Bean ;
import org.springframework.context.annotation.Configuration ;

import bill.zeacc.salieri.fifthgraph.model.meta.AgentDefinition ;
import bill.zeacc.salieri.fifthgraph.model.states.ToolOrientedState ;
import bill.zeacc.salieri.fifthgraph.nodes.ResponseFormatterNode ;

import static bill.zeacc.salieri.fifthgraph.util.NodeHelper.toAsync ;

@Configuration
public class JustChatConfig {

	@Bean
	public ResponseFormatterNode justChatResponseFormatterNode ( ChatModel chatModel ) {
		return new ResponseFormatterNode ( chatModel, "You will answer queries by talking like a pirate." ) { } ;
	}

	@Bean
	public AgentDefinition <ToolOrientedState> justChatAgent ( @Qualifier ( "justChatResponseFormatterNode" ) ResponseFormatterNode responseFormatterNode ) throws GraphStateException {
		StateGraph <ToolOrientedState> graph = new StateGraph <> ( ToolOrientedState.SCHEMA, ToolOrientedState::new )
				.addNode ( "formatter", toAsync ( responseFormatterNode ) )
				.addEdge ( StateGraph.START, "formatter" )
				.addEdge ( "formatter", StateGraph.END )
				;

		return new AgentDefinition <ToolOrientedState> ( "default_agent", graph, q ->  new ToolOrientedState ( ), "A simple chat agent that responds to queries in a pirate-like manner.", "Use this agent for casual conversations or when you want responses with a pirate flair." ) ;
	}
}
