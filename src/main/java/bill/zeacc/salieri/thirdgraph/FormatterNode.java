package bill.zeacc.salieri.thirdgraph;

import java.util.List ;
import java.util.Map ;

import org.bsc.langgraph4j.action.NodeAction ;

import dev.langchain4j.data.message.SystemMessage ;
import dev.langchain4j.data.message.UserMessage ;
import dev.langchain4j.model.chat.request.ChatRequest ;
import dev.langchain4j.model.chat.response.ChatResponse ;
import dev.langchain4j.model.ollama.OllamaChatModel ;

public class FormatterNode implements NodeAction<ToolState> {
    private final OllamaChatModel model;
    
    public FormatterNode(OllamaChatModel model) {
        this.model = model;
    }
    
    @Override
    public Map<String, Object> apply(ToolState state) {
        List<String> toolResults = state.toolResults();
        
        if (toolResults.isEmpty()) {
            // No tools were called - the response is already in messages
            System.out.println("FormatterNode: No formatting needed (no tools used)");
            return Map.of();
        }
        
        System.out.println("FormatterNode: Formatting tool results");
        
        // Parse the tool results
        StringBuilder resultDetails = new StringBuilder();
        for (String result : toolResults) {
            resultDetails.append(result).append("\n");
        }
        
        SystemMessage systemMessage = SystemMessage.from(
            "You must format these tool results into a natural response. " +
            "Include ALL the information from the tool results in your response. " +
            "Do not ignore any results."
        );
        
        String originalQuery = state.originalQuery();
        UserMessage resultsMessage = UserMessage.from(
            "The user asked: " + originalQuery + "\n" +
            "Tool results:\n" + resultDetails.toString() +
            "\nProvide a complete response using ALL this information but don't just repeat it verbatim, and don't talk about the tools."
        );
        
        ChatRequest request = ChatRequest.builder()
            .messages(systemMessage, resultsMessage)
            .build();
        
        ChatResponse response = model.chat(request);
        String formattedResponse = response.aiMessage().text();
        System.err.println("Formatted response: " + formattedResponse);
        return Map.of(ToolState.MESSAGES_KEY, formattedResponse);
    }
}
