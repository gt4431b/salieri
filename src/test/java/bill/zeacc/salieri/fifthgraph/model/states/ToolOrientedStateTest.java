package bill.zeacc.salieri.fifthgraph.model.states;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import bill.zeacc.salieri.fifthgraph.model.meta.ToolCall;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("ToolOrientedState Tests")
public class ToolOrientedStateTest {

    private ToolOrientedState toolOrientedState;
    private Map<String, Object> initialData;

    @BeforeEach
    void setUp() {
        initialData = new HashMap<>();
        toolOrientedState = new ToolOrientedState(initialData);
    }

    @Test
    @DisplayName("Should initialize with empty tool calls and results")
    void shouldInitializeWithEmptyToolCallsAndResults() {
        // When
        List<ToolCall> toolCalls = toolOrientedState.getToolCalls();
        List<ToolResponse> toolResults = toolOrientedState.getToolResults();

        // Then
        assertThat(toolCalls).isEmpty();
        assertThat(toolResults).isEmpty();
    }

    @Test
    @DisplayName("Should store and retrieve tool calls correctly")
    void shouldStoreAndRetrieveToolCallsCorrectly() {
        // Given
        ToolCall toolCall1 = new ToolCall("id1", "tool1", "{\"param\": \"value1\"}");
        ToolCall toolCall2 = new ToolCall("id2", "tool2", "{\"param\": \"value2\"}");
        List<ToolCall> expectedToolCalls = List.of(toolCall1, toolCall2);

        // When
        initialData.put(ToolOrientedState.TOOL_CALLS_KEY, expectedToolCalls);
        toolOrientedState = new ToolOrientedState(initialData);
        List<ToolCall> actualToolCalls = toolOrientedState.getToolCalls();

        // Then
        assertThat(actualToolCalls).isEqualTo(expectedToolCalls);
        assertThat(actualToolCalls).hasSize(2);
    }

    @Test
    @DisplayName("Should store and retrieve tool results correctly")
    void shouldStoreAndRetrieveToolResultsCorrectly() {
        // Given
        ToolResponse response1 = new ToolResponse("id1", "tool1", "result1");
        ToolResponse response2 = new ToolResponse("id2", "tool2", "result2");
        List<ToolResponse> expectedResponses = List.of(response1, response2);

        // When
        initialData.put(ToolOrientedState.TOOL_RESULTS_KEY, expectedResponses);
        toolOrientedState = new ToolOrientedState(initialData);
        List<ToolResponse> actualResponses = toolOrientedState.getToolResults();

        // Then
        assertThat(actualResponses).isEqualTo(expectedResponses);
        assertThat(actualResponses).hasSize(2);
    }

    @Test
    @DisplayName("Should handle null tool calls gracefully")
    void shouldHandleNullToolCallsGracefully() {
        // When
        initialData.put(ToolOrientedState.TOOL_CALLS_KEY, null);
        toolOrientedState = new ToolOrientedState(initialData);
        List<ToolCall> toolCalls = toolOrientedState.getToolCalls();

        // Then
        assertThat(toolCalls).isEmpty();
    }

    @Test
    @DisplayName("Should handle null tool results gracefully")
    void shouldHandleNullToolResultsGracefully() {
        // When
        initialData.put(ToolOrientedState.TOOL_RESULTS_KEY, null);
        toolOrientedState = new ToolOrientedState(initialData);
        List<ToolResponse> toolResults = toolOrientedState.getToolResults();

        // Then
        assertThat(toolResults).isEmpty();
    }

    @Test
    @DisplayName("Should inherit from ResultOrientedState")
    void shouldInheritFromResultOrientedState() {
        // Then
        assertThat(toolOrientedState).isInstanceOf(ResultOrientedState.class);
    }

    @Test
    @DisplayName("Should contain correct schema keys")
    void shouldContainCorrectSchemaKeys() {
        // Then
        assertThat(ToolOrientedState.SCHEMA).containsKeys(
            ToolOrientedState.TOOL_CALLS_KEY,
            ToolOrientedState.TOOL_RESULTS_KEY
        );
        assertThat(ToolOrientedState.TOOL_CALLS_KEY).isEqualTo("tool_calls");
        assertThat(ToolOrientedState.TOOL_RESULTS_KEY).isEqualTo("tool_results");
    }

    @Test
    @DisplayName("Should preserve existing data when setting tool calls")
    void shouldPreserveExistingDataWhenSettingToolCalls() {
        // Given
        String existingKey = "existing_data";
        String existingValue = "existing_value";
        initialData.put(existingKey, existingValue);
        
        ToolCall toolCall = new ToolCall("id1", "tool", "{}");
        List<ToolCall> toolCalls = List.of(toolCall);

        // When
        initialData.put(ToolOrientedState.TOOL_CALLS_KEY, toolCalls);
        toolOrientedState = new ToolOrientedState(initialData);

        // Then
        assertThat(toolOrientedState.value(existingKey)).hasValue(existingValue);
        assertThat(toolOrientedState.getToolCalls()).isEqualTo(toolCalls);
    }
}