package bill.zeacc.salieri.secondgraph ;

import java.util.List ;

import org.bsc.langgraph4j.GraphStateException ;

import dev.langchain4j.agent.tool.ToolExecutionRequest ;
import dev.langchain4j.agent.tool.ToolSpecification ;
import dev.langchain4j.data.message.AiMessage ;
import dev.langchain4j.data.message.ChatMessage ;
import dev.langchain4j.data.message.SystemMessage ;
import dev.langchain4j.data.message.UserMessage ;
import dev.langchain4j.model.chat.request.ChatRequest ;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema ;
import dev.langchain4j.model.chat.response.ChatResponse ;
import dev.langchain4j.model.ollama.OllamaChatModel ;
import dev.langchain4j.service.tool.ToolExecutor ;

public class TestAgent {

	public static void main(String[] argv) throws GraphStateException {
	    doTheThing ( ) ;
//	    makeTheFace ( ) ;
	}
/** /
	@SuppressWarnings ( "unused" )
	private static void testDirectToolCall3(
		    OllamaChatModel model,
		    ToolSpecification tool,
		    ToolExecutor executor
		) {
		    List<ToolSpecification> tools = List.of(tool);
		    // Simple math tool that we KNOW works from curl
		    ToolSpecification mathTool = ToolSpecification.builder()
		        .name("calculate")
		        .description("Performs basic math calculations")
		        .parameters(JsonObjectSchema.builder()
		            .addStringProperty("expression", "Math expression to evaluate")
		            .required("expression")
		            .build()
		        )
		        .build();
		    
		    // Test with the exact same query that worked in curl
		    ChatMessage query = UserMessage.from("What is 2+2?");
		    
		    ChatRequest req = ChatRequest.builder()
		        .toolSpecifications(List.of(mathTool))
		        .messages(query)
		        .build();
		    
		    ChatResponse response = model.chat(req);
		    
		    // Get the raw response
		    AiMessage content = response.aiMessage();
		    System.out.println("Full AI Message: " + content);
		    System.out.println("Has tool requests: " + content.hasToolExecutionRequests());
		    System.out.println("Tool requests: " + content.toolExecutionRequests());
		    System.out.println("Message text: " + content.text());
		    
		    // Try to access the raw response data
		    System.out.println("Response metadata: " + response.metadata());
		    System.out.println("Response token usage: " + response.tokenUsage());
		    
		    // Check if response has attributes that might contain tool calls
		    if (content.attributes() != null && !content.attributes().isEmpty()) {
		        System.out.println("Attributes: " + content.attributes());
		    }
		}
/**/
	@SuppressWarnings ( "unused" )
	private static void makeTheFace ( ) throws GraphStateException {
		ToolSpecification weatherTool = ToolSpecification.builder()
			    .name("get_weather")
			    .description("Get the current weather in a given location")
			    .parameters(JsonObjectSchema.builder()
			        .addStringProperty("location", "The city and state, e.g. San Francisco, CA")
			        .required("location")
			        .build()
			    )
			    .build();

			ChatMessage query = UserMessage.from("What's the weather in San Francisco?");

			ChatRequest req = ChatRequest.builder()
			    .toolSpecifications(List.of(weatherTool))
			    .messages(query)
			    .build();

			OllamaChatModel chatModel = OllamaChatModel.builder()
			        .modelName("llama3-groq-tool-use:latest")
			        .baseUrl("http://localhost:11434")
			        .logRequests(true)  // Add request logging too
			        .logResponses(true)

			        .temperature(0.3)   // Lower temperature for more deterministic behavior
			        .build();

			ChatResponse response = chatModel.doChat(req);
			System.out.println("Weather test response: " + response.aiMessage());
	}
	
	private static void doTheThing ( ) throws GraphStateException {
	    // Define tool more explicitly
	    ToolSpecification toolSpecification = ToolSpecification.builder()
	        .name("getPCName")
	        .description("Returns the name of the PC/computer/machine/station")
//	        .addParameter("dummy", JsonSchemaProperty.STRING)  // Some models need at least one parameter
	        .parameters ( JsonObjectSchema.builder()
//        		.addStringProperty ( "dummy" )
	            .build()
	        )
	        .build();
	    
	    // Debug: Print what's being sent
	    System.out.println("Tool spec: " + toolSpecification);
	    
	    ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
	        System.out.println("Tool called with: " + toolExecutionRequest);
	        return getPCName();
	    };
	    
	    // Try adding system message to enable tools
	    
	    OllamaChatModel chatModel = OllamaChatModel.builder()
	        .modelName("llama3-groq-tool-use:latest")
	        .baseUrl("http://localhost:11434")
	        .logRequests(true)  // Add request logging too
	        .logResponses(true)

	        .temperature(0.3)   // Lower temperature for more deterministic behavior
	        .build();
	    
	    // Test directly first
	    testDirectToolCall(chatModel, toolSpecification, toolExecutor);
	}

	// Test without the StateGraph first
	private static void testDirectToolCall(
	    OllamaChatModel model, 
	    ToolSpecification tool, 
	    ToolExecutor executor
	) {
	    // Direct test with tools
	    List<ToolSpecification> tools = List.of(tool);
	    
	    SystemMessage systemMessage = SystemMessage.from(
		        "You are a helpful assistant with access to tools. " +
		        "When a user asks you to run a test or get PC information, use the getPCName tool."
		    );
	    ChatMessage query = new UserMessage ( "What is the name of this computer?" ) ;
	    ChatRequest req = ChatRequest.builder ( ).toolSpecifications ( tools ).messages ( systemMessage, query ).build ( ) ;
	    ChatResponse response = model.doChat ( req ) ;
	    
	    AiMessage content = response.aiMessage ( ) ;
	    System.out.println("Direct response: " + content);
	    
	    // Check if there are tool execution requests
	    if (content.hasToolExecutionRequests()) {
	        System.out.println("Tool requests found!");
	        for (ToolExecutionRequest request : content.toolExecutionRequests()) {
	            System.out.println("Executing tool: " + request.name());
	            String result = (String) executor.execute(request, null);
	            System.out.println("Tool result: " + result);
	        }
	    }
	}

	private static String getPCName ( ) {
		return "Bob" ;
	}
}
