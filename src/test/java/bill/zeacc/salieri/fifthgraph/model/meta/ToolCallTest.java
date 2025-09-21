package bill.zeacc.salieri.fifthgraph.model.meta;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ToolCall Tests")
public class ToolCallTest {

    private ToolCall toolCall;

    @BeforeEach
    void setUp() {
        toolCall = new ToolCall();
    }

    @Test
    @DisplayName("Should create empty ToolCall with default constructor")
    void shouldCreateEmptyToolCallWithDefaultConstructor() {
        // Then
        assertThat(toolCall.getId()).isNull();
        assertThat(toolCall.getName()).isNull();
        assertThat(toolCall.getArguments()).isNull();
    }

    @Test
    @DisplayName("Should create ToolCall with parameterized constructor")
    void shouldCreateToolCallWithParameterizedConstructor() {
        // Given
        String id = "tool-call-123";
        String name = "test_tool";
        String arguments = "{\"param\": \"value\"}";

        // When
        ToolCall toolCall = new ToolCall(id, name, arguments);

        // Then
        assertThat(toolCall.getId()).isEqualTo(id);
        assertThat(toolCall.getName()).isEqualTo(name);
        assertThat(toolCall.getArguments()).isEqualTo(arguments);
    }

    @Test
    @DisplayName("Should set and get id correctly")
    void shouldSetAndGetIdCorrectly() {
        // Given
        String expectedId = "new-id-456";

        // When
        toolCall.setId(expectedId);

        // Then
        assertThat(toolCall.getId()).isEqualTo(expectedId);
    }

    @Test
    @DisplayName("Should set and get name correctly")
    void shouldSetAndGetNameCorrectly() {
        // Given
        String expectedName = "calculator_tool";

        // When
        toolCall.setName(expectedName);

        // Then
        assertThat(toolCall.getName()).isEqualTo(expectedName);
    }

    @Test
    @DisplayName("Should set and get arguments correctly")
    void shouldSetAndGetArgumentsCorrectly() {
        // Given
        String expectedArguments = "{\"operation\": \"add\", \"operands\": [1, 2]}";

        // When
        toolCall.setArguments(expectedArguments);

        // Then
        assertThat(toolCall.getArguments()).isEqualTo(expectedArguments);
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValuesGracefully() {
        // When
        toolCall.setId(null);
        toolCall.setName(null);
        toolCall.setArguments(null);

        // Then
        assertThat(toolCall.getId()).isNull();
        assertThat(toolCall.getName()).isNull();
        assertThat(toolCall.getArguments()).isNull();
    }

    @Test
    @DisplayName("Should generate correct toString representation")
    void shouldGenerateCorrectToStringRepresentation() {
        // Given
        String id = "test-id";
        String name = "test-tool";
        String arguments = "{\"test\": \"value\"}";
        toolCall.setId(id);
        toolCall.setName(name);
        toolCall.setArguments(arguments);

        // When
        String toString = toolCall.toString();

        // Then
        assertThat(toString).contains("ToolCall{");
        assertThat(toString).contains("id='" + id + "'");
        assertThat(toString).contains("name='" + name + "'");
        assertThat(toString).contains("arguments='" + arguments + "'");
    }

    @Test
    @DisplayName("Should handle toString with null values")
    void shouldHandleToStringWithNullValues() {
        // When
        String toString = toolCall.toString();

        // Then
        assertThat(toString).contains("ToolCall{");
        assertThat(toString).contains("id='null'");
        assertThat(toString).contains("name='null'");
        assertThat(toString).contains("arguments='null'");
    }

    @Test
    @DisplayName("Should be serializable")
    void shouldBeSerializable() {
        // Then
        assertThat(toolCall).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    @DisplayName("Should maintain state across property changes")
    void shouldMaintainStateAcrossPropertyChanges() {
        // Given
        String initialId = "initial-id";
        String initialName = "initial-name";
        String initialArgs = "initial-args";

        // When
        toolCall.setId(initialId);
        toolCall.setName(initialName);
        toolCall.setArguments(initialArgs);

        String newId = "new-id";
        toolCall.setId(newId);

        // Then
        assertThat(toolCall.getId()).isEqualTo(newId);
        assertThat(toolCall.getName()).isEqualTo(initialName); // Should remain unchanged
        assertThat(toolCall.getArguments()).isEqualTo(initialArgs); // Should remain unchanged
    }
}