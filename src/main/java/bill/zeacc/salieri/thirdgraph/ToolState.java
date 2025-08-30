package bill.zeacc.salieri.thirdgraph;

import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;

import org.bsc.langgraph4j.state.AgentState ;
import org.bsc.langgraph4j.state.Channel ;
import org.bsc.langgraph4j.state.Channels ;

public class ToolState extends AgentState {
    public static final String MESSAGES_KEY = "messages";
    public static final String TOOL_CALLS_KEY = "tool_calls";
    public static final String TOOL_RESULTS_KEY = "tool_results";
    public static final String ORIGINAL_QUERY_KEY = "original_query";
    public static final String PENDING_TOOLS_KEY = "pending_tools";

    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            MESSAGES_KEY, Channels.appender(ArrayList::new),
            TOOL_CALLS_KEY, Channels.appender(ArrayList::new),
            TOOL_RESULTS_KEY, Channels.appender(ArrayList::new),
            ORIGINAL_QUERY_KEY, Channels.appender(ArrayList::new),
            PENDING_TOOLS_KEY, Channels.appender(ArrayList::new)
        );

    public ToolState(Map<String, Object> initData) {
        super(initData);
    }
    
    public List<String> messages() {
        return this.<List<String>>value(MESSAGES_KEY).orElse(List.of());
    }
    
    // Changed to return our serializable ToolCall objects
    public List<ToolCall> toolCalls() {
        return this.<List<ToolCall>>value(TOOL_CALLS_KEY).orElse(List.of());
    }
    
    public List<String> toolResults() {
        return this.<List<String>>value(TOOL_RESULTS_KEY).orElse(List.of());
    }
    public String originalQuery() {
        List<String> queries = this.<List<String>>value(ORIGINAL_QUERY_KEY).orElse(List.of());
        return queries.isEmpty() ? "" : queries.get(queries.size() - 1);
    }    
    public List<String> pendingTools() {
        return this.<List<String>>value(PENDING_TOOLS_KEY).orElse(List.of());
    }
}
