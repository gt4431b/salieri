package bill.zeacc.salieri.fifthgraph.agents.hello.config;

import java.util.List ;
import java.util.Map ;
import java.util.concurrent.CompletableFuture ;

import org.bsc.langgraph4j.GraphStateException ;
import org.bsc.langgraph4j.StateGraph ;
import org.bsc.langgraph4j.action.AsyncEdgeAction ;
import org.springframework.ai.chat.model.ChatModel ;
import org.springframework.beans.factory.annotation.Qualifier ;
import org.springframework.context.annotation.Bean ;
import org.springframework.context.annotation.Configuration ;

import bill.zeacc.salieri.fifthgraph.agents.hello.GraphState ;
import bill.zeacc.salieri.fifthgraph.model.meta.AgentDefinition ;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolCall ;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolChooser ;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolResponse ;
import bill.zeacc.salieri.fifthgraph.nodes.ToolAnalyzerNode ;
import bill.zeacc.salieri.fifthgraph.nodes.ResponseFormatterNode ;
import bill.zeacc.salieri.fifthgraph.nodes.ToolExecutorNode ;

import static bill.zeacc.salieri.fifthgraph.util.NodeHelper.toAsync ;

@Configuration
public class HelloAgentConfig {

	@Bean ( "helloResponseFormatterNode" )
	public ResponseFormatterNode helloResponseFormatterNode ( ChatModel chatModel ) {
		return new ResponseFormatterNode ( chatModel, "You are a helpful assistant but also kind of a snarky little jerk. Keep answers concise.  Be natural and conversational." ) { } ;
	}

	@Bean ( "helloToolAnalyzerNode" )
	public ToolAnalyzerNode helloToolAnalyzerNode ( ChatModel cm, @Qualifier ( "helloTools" ) ToolChooser availableTools ) {
		return new ToolAnalyzerNode ( cm, availableTools ) { } ;
	}

	@Bean ( "helloToolExecutorNode" )
	public ToolExecutorNode helloToolExecutorNode ( @Qualifier ( "helloTools" ) ToolChooser availableTools ) {
		return new ToolExecutorNode ( availableTools ) { } ;
	}

	@Bean
	public AgentDefinition <GraphState> helloAgent ( @Qualifier ( "helloToolAnalyzerNode" ) ToolAnalyzerNode analyzerNode,
								@Qualifier ( "helloToolExecutorNode" ) ToolExecutorNode toolExecutorNode,
								@Qualifier ( "helloResponseFormatterNode" ) ResponseFormatterNode responseFormatterNode ) throws GraphStateException {
		if ( analyzerNode == null || toolExecutorNode == null || responseFormatterNode == null ) {
			throw new IllegalStateException ( "analyzerNode is null" ) ;
		}
		// Define conditional edge
		AsyncEdgeAction <GraphState> routeOnTools = ( state ) -> {
			List <ToolCall> toolCalls = state.getToolCalls ( ) ;
			List <ToolResponse> toolResults = state.getToolResults ( ) ;
			if ( ! toolCalls.isEmpty ( ) && toolResults.isEmpty ( ) ) { // Given we're calling this before tool execution, what's the point of checking toolResults?
				return CompletableFuture.completedFuture ( "tool_executor" ) ;
			}
			return CompletableFuture.completedFuture ( "formatter" ) ;
		} ;

		// Build graph
		StateGraph <GraphState> graph = new StateGraph <> ( GraphState.SCHEMA, GraphState::new )
				.addNode ( "analyzer", toAsync ( analyzerNode ) )
				.addNode ( "tool_executor", toAsync ( toolExecutorNode ) )
				.addNode ( "formatter", toAsync ( responseFormatterNode ) )
				.addEdge ( StateGraph.START, "analyzer" )
				.addConditionalEdges ( "analyzer", routeOnTools, Map.of ( "tool_executor", "tool_executor", "formatter", "formatter" ) )
				.addEdge ( "tool_executor", "formatter" )
				.addEdge ( "formatter", StateGraph.END )
				;

		return new AgentDefinition <> ( "hello_agent", graph, q -> new GraphState ( ), "A simple hello world agent using tools", "Good for getting time, system information, or reading files." ) ;
	}
}