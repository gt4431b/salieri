package bill.zeacc.salieri.thirdgraph;

import java.util.Date ;
import java.util.List ;
import java.util.Map ;
import java.util.concurrent.CompletableFuture ;

import org.bsc.langgraph4j.CompiledGraph ;
import org.bsc.langgraph4j.GraphStateException ;
import org.bsc.langgraph4j.NodeOutput ;
import org.bsc.langgraph4j.StateGraph ;
import org.bsc.langgraph4j.action.AsyncEdgeAction ;
import org.bsc.langgraph4j.action.AsyncNodeAction ;
import org.bsc.langgraph4j.action.NodeAction ;
import org.bsc.langgraph4j.state.AgentState ;

import dev.langchain4j.agent.tool.ToolSpecification ;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema ;
import dev.langchain4j.model.ollama.OllamaChatModel ;
import dev.langchain4j.service.tool.ToolExecutor ;

public class ToolGraphDemo {
    
    public static void main(String[] args) throws GraphStateException {
        // Initialize the model
        OllamaChatModel model = OllamaChatModel.builder()
            .modelName("llama3-groq-tool-use:latest")
            .baseUrl("http://localhost:11434")
            .temperature(0.3)
            .logRequests(true)
            .logResponses(true)
            .build();
        
        // Define tools
        ToolSpecification pcNameTool = ToolSpecification.builder()
            .name("getPCName")
            .description("There was a question involving the name of the PC/computer/machine, so this tool was called to get that name")
            .parameters(JsonObjectSchema.builder().build())
            .build();
        
        ToolSpecification timeTool = ToolSpecification.builder()
            .name("getCurrentTime")
            .description("There was a question involving the current time, so this tool was called to get it")
            .parameters(JsonObjectSchema.builder().build())
            .build();
        
        List<ToolSpecification> tools = List.of(pcNameTool, timeTool);
        
        // Define tool executors
        Map<String, ToolExecutor> toolExecutors = Map.of(
            "getPCName", (request, memoryId) -> {
                return "Bob";
            },
            "getCurrentTime", (request, memoryId) -> {
                return new Date().toString();
            }
        );
        
        // Initialize nodes
        LLMNode llmNode = new LLMNode(model, tools);
        ToolExecutorNode toolNode = new ToolExecutorNode(toolExecutors);
        FormatterNode formatterNode = new FormatterNode(model);
        
        // Define conditional edge function
        AsyncEdgeAction<ToolState> shouldCallTools = (state) -> {
            String nextNode;
            
            // Check if there are unexecuted tool calls
            int toolCallCount = state.toolCalls().size();
            int toolResultCount = state.toolResults().size();
            
            if (toolCallCount > toolResultCount) {
                // Tools called but not all executed yet
                nextNode = "tool_executor";
            } else if (toolResultCount > 0) {
                // Tools were executed, go to formatter
                nextNode = "formatter";
            } else {
                // No tools called or needed, skip to formatter
                // The formatter will check if it needs to do anything
                nextNode = "formatter";
            }
            
            return CompletableFuture.completedFuture(nextNode);
        };

        // Build the graph
        StateGraph<ToolState> graph = new StateGraph<>(ToolState.SCHEMA, ToolState::new)
            .addNode("llm", node_async(llmNode))
            .addNode("tool_executor", node_async(toolNode))
            .addNode("formatter", node_async(formatterNode))
            .addEdge(StateGraph.START, "llm")
            .addConditionalEdges("llm", shouldCallTools, Map.of(
                    "tool_executor", "tool_executor",
                    "formatter", "formatter"
                ))
            .addEdge("tool_executor", "formatter")
            .addEdge("formatter", StateGraph.END);
        
        // Compile the graph
        CompiledGraph<ToolState> compiledGraph = graph.compile();
        
        // Test queries
        String[] testQueries = {
            "What is the name of this computer?",
            "What time is it?",
            "Hello, how are you?",  // This shouldn't trigger tools
            "Tell me the PC name and current time"
        };
        
        for (String query : testQueries) {
            System.out.println("\n========================================");
            System.out.println("Query: " + query);
            System.out.println("========================================");
            
            Map<String, Object> inputs = Map.of(ToolState.MESSAGES_KEY, query);
            
            for (NodeOutput<ToolState> output : compiledGraph.stream(inputs)) {
                System.out.println("Node: " + output.node());
                System.out.println("Messages: " + output.state().messages());
                
                if (!output.state().toolCalls().isEmpty()) {
                    System.out.println("Tool calls: " + output.state().toolCalls().size());
                }
                if (!output.state().toolResults().isEmpty()) {
                    System.out.println("Tool results: " + output.state().toolResults());
                }
            }
            
            System.out.println("----------------------------------------");
        }
    }
    
    // Helper method for async nodes
    private static <T extends AgentState> AsyncNodeAction<T> node_async(NodeAction<T> action) throws GraphStateException {
        return (state) -> {
			try {
				return CompletableFuture.completedFuture(action.apply(state)) ;
			} catch ( Exception e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException ( e ) ;
			}
		};
    }
}
