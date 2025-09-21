package bill.zeacc.salieri.fifthgraph.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatResponse Tests")
public class ChatResponseTest {

    @Test
    @DisplayName("Should create ChatResponse with valid result")
    void shouldCreateChatResponseWithValidResult() {
        // Given
        String result = "Hello! I'm doing well, thank you for asking.";

        // When
        ChatResponse response = new ChatResponse(result);

        // Then
        assertThat(response.response()).isEqualTo(result);
    }

    @Test
    @DisplayName("Should handle empty result")
    void shouldHandleEmptyResult() {
        // Given
        String result = "";

        // When
        ChatResponse response = new ChatResponse(result);

        // Then
        assertThat(response.response()).isEmpty();
    }

    @Test
    @DisplayName("Should handle null result")
    void shouldHandleNullResult() {
        // When
        ChatResponse response = new ChatResponse(null);

        // Then
        assertThat(response.response()).isNull();
    }

    @Test
    @DisplayName("Should be immutable record")
    void shouldBeImmutableRecord() {
        // Given
        String result = "Test response";
        ChatResponse response = new ChatResponse(result);

        // Then - Record should be immutable, no setters available
        assertThat(response.response()).isEqualTo(result);
    }

    @Test
    @DisplayName("Should handle special characters and formatting")
    void shouldHandleSpecialCharactersAndFormatting() {
        // Given
        String complexResult = "Here's your answer:\n• Point 1 with émojis 🎯\n• Point 2 with symbols ∑∆\n• JSON: {\"key\": \"value\"}";

        // When
        ChatResponse response = new ChatResponse(complexResult);

        // Then
        assertThat(response.response()).isEqualTo(complexResult);
        assertThat(response.response()).contains("émojis", "∑∆", "JSON");
    }

    @Test
    @DisplayName("Should handle very long responses")
    void shouldHandleVeryLongResponses() {
        // Given
        String longResult = "This is a very long response that might contain extensive analysis or detailed explanations. ".repeat(50);

        // When
        ChatResponse response = new ChatResponse(longResult);

        // Then
        assertThat(response.response()).isEqualTo(longResult);
        assertThat(response.response().length()).isGreaterThan(1000);
    }

    @Test
    @DisplayName("Should handle multiline responses")
    void shouldHandleMultilineResponses() {
        // Given
        String multilineResult = "Line 1\nLine 2\n\nLine 4 after blank line\r\nWindows line ending";

        // When
        ChatResponse response = new ChatResponse(multilineResult);

        // Then
        assertThat(response.response()).isEqualTo(multilineResult);
        assertThat(response.response()).contains("\n", "\r\n");
    }
}