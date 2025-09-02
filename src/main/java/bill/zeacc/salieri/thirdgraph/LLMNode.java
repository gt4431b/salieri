package bill.zeacc.salieri.thirdgraph;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;
import java.util.stream.Collectors ;

import org.bsc.langgraph4j.action.NodeAction ;

import dev.langchain4j.agent.tool.ToolSpecification ;
import dev.langchain4j.data.message.AiMessage ;
import dev.langchain4j.data.message.SystemMessage ;
import dev.langchain4j.data.message.UserMessage ;
import dev.langchain4j.model.chat.request.ChatRequest ;
import dev.langchain4j.model.chat.response.ChatResponse ;
import dev.langchain4j.model.ollama.OllamaChatModel ;

public class LLMNode implements NodeAction<ToolState> {
    private final OllamaChatModel model;
    private final List<ToolSpecification> tools;
    
    public LLMNode(OllamaChatModel model, List<ToolSpecification> tools) {
        this.model = model;
        this.tools = tools;
    }
    
    @Override
    public Map<String, Object> apply(ToolState state) {
        System.out.println("LLMNode: Processing user query...");
        
        List<String> messages = state.messages();
        String userQuery;
        
        // Check if this is the first call or a retry for more tools
        if (state.originalQuery().isEmpty()) {
            // First call
            if (messages.isEmpty()) {
                return Map.of(ToolState.MESSAGES_KEY, "No query provided");
            }
            userQuery = messages.get(messages.size() - 1);
        } else {
            // Retry - use original query
            userQuery = state.originalQuery();
        }
        
        // Analyze what tools are needed
        List<String> neededTools = analyzeNeededTools(userQuery);
        
        // Build the system message
        SystemMessage systemMessage;
        if (!neededTools.isEmpty()) {
            // Tools might be needed
            systemMessage = SystemMessage.from(
                "You are a helpful assistant with access to tools. " +
                "Use the getPCName tool when asked about the computer, PC, or machine name. " +
                "Use the getCurrentTime tool when asked about the time. " +
                "If asked for multiple pieces of information, call ALL relevant tools."
            );
        } else {
            // No tools needed, just be helpful
            systemMessage = SystemMessage.from(
                "You are a helpful assistant. Answer the user's question directly."
            );
        }
        
        UserMessage userMessage = UserMessage.from(userQuery);
        
        // Always make the LLM call
        ChatRequest request = ChatRequest.builder()
            .toolSpecifications(tools)  // Tools available even if not needed
            .messages(systemMessage, userMessage)
            .build();
        
        ChatResponse response = model.doChat(request);
        AiMessage aiMessage = response.aiMessage();
        
        Map<String, Object> updates = new HashMap<>();
        
        if (aiMessage.hasToolExecutionRequests()) {
            System.out.println("LLMNode: Tool calls requested");
            updates.put(ToolState.ORIGINAL_QUERY_KEY, userQuery);
            List<ToolCall> toolCalls = aiMessage.toolExecutionRequests().stream()
                .map(req -> ToolCall.from(req, userQuery, tools))
                .collect(Collectors.toList());
            updates.put(ToolState.TOOL_CALLS_KEY, toolCalls);
            updates.put(ToolState.MESSAGES_KEY, "Tool calls requested");
        } else if (aiMessage.text() != null) {
            System.out.println("LLMNode: Direct response (no tools needed)");
            updates.put(ToolState.MESSAGES_KEY, aiMessage.text());
            updates.put(ToolState.ORIGINAL_QUERY_KEY, userQuery);
        } else {
            updates.put(ToolState.MESSAGES_KEY, "No response generated");
        }
        
        return updates;
    }
    
    private List<String> analyzeNeededTools(String query) {
        List<String> needed = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        if (lowerQuery.contains("pc") || lowerQuery.contains("computer") || 
            lowerQuery.contains("machine") || lowerQuery.contains("name")) {
            needed.add("getPCName");
        }
        
        if (lowerQuery.contains("time") || lowerQuery.contains("when") || 
            lowerQuery.contains("clock") || lowerQuery.contains("current")) {
            needed.add("getCurrentTime");
        }
        
        return needed;
    }
}
