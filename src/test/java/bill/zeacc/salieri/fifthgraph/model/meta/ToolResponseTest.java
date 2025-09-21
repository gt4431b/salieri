package bill.zeacc.salieri.fifthgraph.model.meta;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ToolResponse Tests")
public class ToolResponseTest {

    @Test
    @DisplayName("Should create ToolResponse with constructor parameters")
    public void shouldCreateToolResponseWithConstructorParameters() {
        // Given
        String toolExecutionId = "exec-123";
        String toolName = "calculator";
        String result = "42";

        // When
        ToolResponse toolResponse = new ToolResponse(toolExecutionId, toolName, result);

        // Then
        assertThat(toolResponse.getId()).isEqualTo(toolExecutionId);
        assertThat(toolResponse.getToolName()).isEqualTo(toolName);
        assertThat(toolResponse.getOutput()).isEqualTo(result);
    }

    @Test
    @DisplayName("Should handle null constructor parameters")
    public void shouldHandleNullConstructorParameters() {
        // When
        ToolResponse toolResponse = new ToolResponse(null, null, null);

        // Then
        assertThat(toolResponse.getId()).isNull();
        assertThat(toolResponse.getToolName()).isNull();
        assertThat(toolResponse.getOutput()).isNull();
    }

    @Test
    @DisplayName("Should set and get id correctly")
    public void shouldSetAndGetIdCorrectly() {
        // Given
        ToolResponse toolResponse = new ToolResponse("initial-id", "tool", "result");
        String newId = "new-id-456";

        // When
        toolResponse.setId(newId);

        // Then
        assertThat(toolResponse.getId()).isEqualTo(newId);
    }

    @Test
    @DisplayName("Should set and get tool name correctly")
    public void shouldSetAndGetToolNameCorrectly() {
        // Given
        ToolResponse toolResponse = new ToolResponse("id", "initial-tool", "result");
        String newToolName = "new-tool-name";

        // When
        toolResponse.setToolName(newToolName);

        // Then
        assertThat(toolResponse.getToolName()).isEqualTo(newToolName);
    }

    @Test
    @DisplayName("Should set and get output correctly")
    void shouldSetAndGetOutputCorrectly() {
        // Given
        ToolResponse toolResponse = new ToolResponse("id", "tool", "initial-result");
        String newOutput = "new-output-data";

        // When
        toolResponse.setOutput(newOutput);

        // Then
        assertThat(toolResponse.getOutput()).isEqualTo(newOutput);
    }

    @Test
    @DisplayName("Should be serializable")
    void shouldBeSerializable() {
        // Given
        ToolResponse toolResponse = new ToolResponse("id", "tool", "result");

        // Then
        assertThat(toolResponse).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    @DisplayName("Should handle empty string values")
    void shouldHandleEmptyStringValues() {
        // When
        ToolResponse toolResponse = new ToolResponse("", "", "");

        // Then
        assertThat(toolResponse.getId()).isEmpty();
        assertThat(toolResponse.getToolName()).isEmpty();
        assertThat(toolResponse.getOutput()).isEmpty();
    }

    @Test
    @DisplayName("Should maintain independent property state")
    void shouldMaintainIndependentPropertyState() {
        // Given
        ToolResponse toolResponse = new ToolResponse("id1", "tool1", "result1");

        // When
        toolResponse.setId("id2");
        toolResponse.setToolName("tool2");

        // Then
        assertThat(toolResponse.getId()).isEqualTo("id2");
        assertThat(toolResponse.getToolName()).isEqualTo("tool2");
        assertThat(toolResponse.getOutput()).isEqualTo("result1"); // Should remain unchanged
    }

    @Test
    @DisplayName("Should handle complex output data")
    void shouldHandleComplexOutputData() {
        // Given
        String complexOutput = "{\"status\": \"success\", \"data\": [1, 2, 3], \"nested\": {\"key\": \"value\"}}";

        // When
        ToolResponse toolResponse = new ToolResponse("id", "json-tool", complexOutput);

        // Then
        assertThat(toolResponse.getOutput()).isEqualTo(complexOutput);
    }
}