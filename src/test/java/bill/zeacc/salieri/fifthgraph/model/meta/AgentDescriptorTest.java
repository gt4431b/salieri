package bill.zeacc.salieri.fifthgraph.model.meta;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgentDescriptor Tests")
public class AgentDescriptorTest {

    @Mock
    private AgentDefinition<?> mockAgentDefinition;

    @Test
    @DisplayName("Should create AgentDescriptor with all parameters")
    void shouldCreateAgentDescriptorWithAllParameters() {
        // Given
        String name = "test_agent";
        String description = "A test agent for unit testing";
        String usage = "Use this agent to test functionality";

        // When
        AgentDescriptor descriptor = new AgentDescriptor(name, description, usage);

        // Then
        assertThat(descriptor.name()).isEqualTo(name);
        assertThat(descriptor.description()).isEqualTo(description);
        assertThat(descriptor.hintsForUse()).isEqualTo(usage);
    }

    @Test
    @DisplayName("Should handle null values in constructor")
    void shouldHandleNullValuesInConstructor() {
        // When
        AgentDescriptor descriptor = new AgentDescriptor(null, null, null);

        // Then
        assertThat(descriptor.name()).isNull();
        assertThat(descriptor.description()).isNull();
        assertThat(descriptor.hintsForUse()).isNull();
    }

    @Test
    @DisplayName("Should handle empty strings")
    void shouldHandleEmptyStrings() {
        // When
        AgentDescriptor descriptor = new AgentDescriptor("", "", "");

        // Then
        assertThat(descriptor.name()).isEmpty();
        assertThat(descriptor.description()).isEmpty();
        assertThat(descriptor.hintsForUse()).isEmpty();
    }

    @Test
    @DisplayName("Should be immutable record")
    void shouldBeImmutableRecord() {
        // Given
        String name = "immutable_agent";
        String description = "Test immutability";
        String usage = "Cannot be changed after creation";
        
        // When
        AgentDescriptor descriptor = new AgentDescriptor(name, description, usage);

        // Then
        assertThat(descriptor.name()).isEqualTo(name);
        assertThat(descriptor.description()).isEqualTo(description);
        assertThat(descriptor.hintsForUse()).isEqualTo(usage);
        // Record should be immutable - no setters should exist
    }

    @Test
    @DisplayName("Should handle long descriptions and usage text")
    void shouldHandleLongDescriptionsAndUsageText() {
        // Given
        String name = "complex_agent";
        String longDescription = "This is a very detailed description of what this agent does. ".repeat(10);
        String longUsage = "Here are extensive usage instructions with multiple examples. ".repeat(15);

        // When
        AgentDescriptor descriptor = new AgentDescriptor(name, longDescription, longUsage);

        // Then
        assertThat(descriptor.name()).isEqualTo(name);
        assertThat(descriptor.description()).isEqualTo(longDescription);
        assertThat(descriptor.hintsForUse()).isEqualTo(longUsage);
        assertThat(descriptor.description().length()).isGreaterThan(500);
        assertThat(descriptor.hintsForUse().length()).isGreaterThan(700);
    }

    @Test
    @DisplayName("Should handle special characters and formatting")
    void shouldHandleSpecialCharactersAndFormatting() {
        // Given
        String name = "special_agent_émojis_🤖";
        String description = "Agent with special chars: áéíóú, symbols: ∑∆π, and formatting\n• Bullet points\n• More bullets";
        String usage = "Usage with JSON: {\"key\": \"value\"} and code: `agent.run()`";

        // When
        AgentDescriptor descriptor = new AgentDescriptor(name, description, usage);

        // Then
        assertThat(descriptor.name()).isEqualTo(name);
        assertThat(descriptor.description()).isEqualTo(description);
        assertThat(descriptor.hintsForUse()).isEqualTo(usage);
        assertThat(descriptor.name()).contains("émojis", "🤖");
        assertThat(descriptor.description()).contains("áéíóú", "∑∆π", "•");
        assertThat(descriptor.hintsForUse()).contains("{\"key\": \"value\"}", "`agent.run()`");
    }
}