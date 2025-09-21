package bill.zeacc.salieri.fifthgraph.nodes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.* ;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import bill.zeacc.salieri.fifthgraph.model.meta.InternalTool;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolCall;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolChooser;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolResponse;
import bill.zeacc.salieri.fifthgraph.model.states.ToolOrientedState;

@ExtendWith(MockitoExtension.class)
@DisplayName("ToolExecutorNode Tests")
public class ToolExecutorNodeTest {

    @Mock
    private ToolChooser mockToolChooser;

    @Mock
    private InternalTool mockTool1;

    @Mock
    private InternalTool mockTool2;

    @Mock
    private ToolOrientedState mockState;

    private TestToolExecutorNode toolExecutorNode;

    @BeforeEach
    void setUp() {
        // Setup mock tools
        when(mockTool1.getName()).thenReturn("tool1");
        when(mockTool2.getName()).thenReturn("tool2");
        
        List<InternalTool> tools = List.of(mockTool1, mockTool2);
        when(mockToolChooser.get()).thenReturn(tools);

        toolExecutorNode = new TestToolExecutorNode(mockToolChooser);
    }

    @Test
    @DisplayName("Should initialize tool map correctly from tool chooser")
    void shouldInitializeToolMapCorrectlyFromToolChooser() {
        // Then
        verify(mockToolChooser).get();
        verify(mockTool1).getName();
        verify(mockTool2).getName();
    }

    @Test
    @DisplayName("Should return empty map when no tool calls")
    void shouldReturnEmptyMapWhenNoToolCalls() {
        // Given
        when(mockState.getToolCalls()).thenReturn(new ArrayList<>());

        // When
        Map<String, Object> result = toolExecutorNode.apply(mockState);

        // Then
        assertThat(result).isEmpty();
        verify(mockState).getToolCalls();
    }

    @Test
    @DisplayName("Should execute tool calls successfully")
    void shouldExecuteToolCallsSuccessfully() throws Exception {
        // Given
        ToolCall call1 = new ToolCall("id1", "tool1", "{\"param1\": \"value1\"}");
        ToolCall call2 = new ToolCall("id2", "tool2", "{\"param2\": \"value2\"}");
        List<ToolCall> toolCalls = List.of(call1, call2);

        ToolResponse response1 = new ToolResponse("id1", "tool1", "result1");
        ToolResponse response2 = new ToolResponse("id2", "tool2", "result2");

        when(mockState.getToolCalls()).thenReturn(toolCalls);
        when(mockTool1.execute("{\"param1\": \"value1\"}")).thenReturn(response1);
        when(mockTool2.execute("{\"param2\": \"value2\"}")).thenReturn(response2);

        // When
        Map<String, Object> result = toolExecutorNode.apply(mockState);

        // Then
        assertThat(result).containsKey(ToolOrientedState.TOOL_RESULTS_KEY);
        
        @SuppressWarnings("unchecked")
        List<ToolResponse> actualResults = (List<ToolResponse>) result.get(ToolOrientedState.TOOL_RESULTS_KEY);
        assertThat(actualResults).hasSize(2);
        assertThat(actualResults).containsExactly(response1, response2);

        verify(mockTool1).execute("{\"param1\": \"value1\"}");
        verify(mockTool2).execute("{\"param2\": \"value2\"}");
    }

    @Test
    @DisplayName("Should handle tool execution exceptions gracefully")
    void shouldHandleToolExecutionExceptionsGracefully() throws Exception {
        // Given
        String args = "{\"param\": \"value\"}";
        ToolCall call = new ToolCall("id1", "tool1", args);
        List<ToolCall> toolCalls = List.of(call);

        when(mockState.getToolCalls()).thenReturn(toolCalls);
        doThrow(new RuntimeException("Tool execution failed")).when(mockTool1).execute(args);

        // When
        Map<String, Object> result = toolExecutorNode.apply(mockState);

        // Then
        assertThat(result).containsKey(ToolOrientedState.TOOL_RESULTS_KEY);
        
        @SuppressWarnings("unchecked")
        List<ToolResponse> actualResults = (List<ToolResponse>) result.get(ToolOrientedState.TOOL_RESULTS_KEY);
        assertThat(actualResults).isEmpty(); // No results when execution fails

        verify(mockTool1).execute(args);
    }

    @Test
    @DisplayName("Should handle unknown tool calls gracefully")
    void shouldHandleUnknownToolCallsGracefully() {
        // Given
        String args = "{\"param\": \"value\"}";
        ToolCall unknownCall = new ToolCall("id1", "unknown_tool", args);
        List<ToolCall> toolCalls = List.of(unknownCall);

        when(mockState.getToolCalls()).thenReturn(toolCalls);

        // When
        Map<String, Object> result = toolExecutorNode.apply(mockState);

        // Then
        assertThat(result).containsKey(ToolOrientedState.TOOL_RESULTS_KEY);
        
        @SuppressWarnings("unchecked")
        List<ToolResponse> actualResults = (List<ToolResponse>) result.get(ToolOrientedState.TOOL_RESULTS_KEY);
        assertThat(actualResults).isEmpty(); // No results for unknown tools
    }

    @Test
    @DisplayName("Should execute only known tools and skip unknown ones")
    void shouldExecuteOnlyKnownToolsAndSkipUnknownOnes() throws Exception {
        // Given
        String args1 = "{\"param1\": \"value1\"}";
        String args2 = "{\"param2\": \"value2\"}";
        
        ToolCall knownCall = new ToolCall("id1", "tool1", args1);
        ToolCall unknownCall = new ToolCall("id2", "unknown_tool", args2);
        List<ToolCall> toolCalls = List.of(knownCall, unknownCall);

        ToolResponse response1 = new ToolResponse("id1", "tool1", "result1");

        when(mockState.getToolCalls()).thenReturn(toolCalls);
        when(mockTool1.execute(args1)).thenReturn(response1);

        // When
        Map<String, Object> result = toolExecutorNode.apply(mockState);

        // Then
        assertThat(result).containsKey(ToolOrientedState.TOOL_RESULTS_KEY);
        
        @SuppressWarnings("unchecked")
        List<ToolResponse> actualResults = (List<ToolResponse>) result.get(ToolOrientedState.TOOL_RESULTS_KEY);
        assertThat(actualResults).hasSize(1);
        assertThat(actualResults.get(0)).isEqualTo(response1);

        verify(mockTool1).execute(args1);
    }

    @Test
    @DisplayName("Should set tool response IDs correctly")
    void shouldSetToolResponseIdsCorrectly() throws Exception {
        // Given
        String expectedId = "test-call-id";
        String args = "{\"param\": \"value\"}";
        ToolCall call = new ToolCall(expectedId, "tool1", args);
        List<ToolCall> toolCalls = List.of(call);

        ToolResponse response = new ToolResponse(null, "tool1", "result");
        when(mockState.getToolCalls()).thenReturn(toolCalls);
        when(mockTool1.execute(args)).thenReturn(response);

        // When
        Map <String, Object> result = toolExecutorNode.apply(mockState);

        // Then
        assertEquals ( response.getId ( ), expectedId, "ToolResponse ID should be set to ToolCall ID" ) ;

        @SuppressWarnings("unchecked")
        List<ToolResponse> actualResults = (List<ToolResponse>) result.get(ToolOrientedState.TOOL_RESULTS_KEY);
        assertThat(actualResults.get(0).getId()).isEqualTo(expectedId);
    }

    // Concrete test implementation of the abstract class
    private static class TestToolExecutorNode extends ToolExecutorNode {
        public TestToolExecutorNode(ToolChooser toolProvider) {
            super(toolProvider);
        }
    }
}