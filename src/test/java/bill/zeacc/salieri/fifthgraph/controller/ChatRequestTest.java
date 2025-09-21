package bill.zeacc.salieri.fifthgraph.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatRequest Tests")
public class ChatRequestTest {

    @Test
    @DisplayName("Should create ChatRequest with valid parameters")
    void shouldCreateChatRequestWithValidParameters() {
        // Given
        String agentName = "hello_agent";
        String query = "Hello, how are you?";

        // When
        ChatRequest request = new ChatRequest(agentName, query);

        // Then
        assertThat(request.agentName()).isEqualTo(agentName);
        assertThat(request.query()).isEqualTo(query);
    }

    @Test
    @DisplayName("Should handle empty agent name")
    void shouldHandleEmptyAgentName() {
        // Given
        String agentName = "";
        String query = "Test query";

        // When
        ChatRequest request = new ChatRequest(agentName, query);

        // Then
        assertThat(request.agentName()).isEmpty();
        assertThat(request.query()).isEqualTo(query);
    }

    @Test
    @DisplayName("Should handle empty query")
    void shouldHandleEmptyQuery() {
        // Given
        String agentName = "test_agent";
        String query = "";

        // When
        ChatRequest request = new ChatRequest(agentName, query);

        // Then
        assertThat(request.agentName()).isEqualTo(agentName);
        assertThat(request.query()).isEmpty();
    }

    @Test
    @DisplayName("Should handle null values")
    void shouldHandleNullValues() {
        // When
        ChatRequest request = new ChatRequest(null, null);

        // Then
        assertThat(request.agentName()).isNull();
        assertThat(request.query()).isNull();
    }

    @Test
    @DisplayName("Should be immutable record")
    void shouldBeImmutableRecord() {
        // Given
        String agentName = "test_agent";
        String query = "test query";
        ChatRequest request = new ChatRequest(agentName, query);

        // Then - Record should be immutable, no setters available
        assertThat(request.agentName()).isEqualTo(agentName);
        assertThat(request.query()).isEqualTo(query);
    }

    @Test
    @DisplayName("Should handle special characters in query")
    void shouldHandleSpecialCharactersInQuery() {
        // Given
        String agentName = "test_agent";
        String query = "What's the weather like? Can you help with émojis 🤖 and símb∅ls?";

        // When
        ChatRequest request = new ChatRequest(agentName, query);

        // Then
        assertThat(request.query()).isEqualTo(query);
        assertThat(request.agentName()).isEqualTo(agentName);
    }

    @Test
    @DisplayName("Should handle very long queries")
    void shouldHandleVeryLongQueries() {
        // Given
        String agentName = "test_agent";
        String longQuery = "This is a very long query that might be used to test the limits of the system. ".repeat(100);

        // When
        ChatRequest request = new ChatRequest(agentName, longQuery);

        // Then
        assertThat(request.query()).isEqualTo(longQuery);
        assertThat(request.query().length()).isGreaterThan(1000);
    }
}