package bill.zeacc.salieri.thirdgraph;

import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;

import org.bsc.langgraph4j.action.NodeAction ;

import dev.langchain4j.agent.tool.ToolExecutionRequest ;
import dev.langchain4j.service.tool.ToolExecutor ;

public class ToolExecutorNode implements NodeAction<ToolState> {
    private final Map<String, ToolExecutor> toolExecutors;
    
    public ToolExecutorNode(Map<String, ToolExecutor> toolExecutors) {
        this.toolExecutors = toolExecutors;
    }
    
    @Override
    public Map<String, Object> apply(ToolState state) {
        List<ToolCall> toolCalls = state.toolCalls();
        
        if (toolCalls.isEmpty()) {
            System.out.println("ToolExecutorNode: No tools to execute");
            return Map.of();
        }
        
        System.out.println("ToolExecutorNode: Executing " + toolCalls.size() + " tool(s)");
        
        List<String> results = new ArrayList<>();
        for (ToolCall toolCall : toolCalls) {
            System.out.println("Executing tool: " + toolCall.getName());
            
            ToolExecutor executor = toolExecutors.get(toolCall.getName());
            if (executor != null) {
                // Convert back to ToolExecutionRequest for execution
                ToolExecutionRequest request = toolCall.toExecutionRequest();
                String result = (String) executor.execute(request, null);
                results.add(toolCall.getToolDescription ( ) + ": " + result);
                System.out.println("Tool result: " + result);
            } else {
                results.add(toolCall.getName() + ": Tool not found");
            }
        }

        return Map.of(
            ToolState.TOOL_RESULTS_KEY, results,
            ToolState.MESSAGES_KEY, "Tools executed"
        );
    }
}

