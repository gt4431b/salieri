package bill.zeacc.salieri.fifthgraph.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bill.zeacc.salieri.fifthgraph.service.QueryProcessor.QueryResult;

@DisplayName("ResponseFormatter Tests")
public class ResponseFormatterTest {

    private ResponseFormatter responseFormatter;

    @BeforeEach
    protected void setUp() {
        responseFormatter = new ResponseFormatter();
    }

    @Test
    @DisplayName("Should format successful query response")
    public void shouldFormatSuccessfulQueryResponse() {
        // Given
        QueryResult queryResult = new QueryResult(
            "What is the weather?",
            "weather_agent",
            "weather_agent", 
            85,
            "It's sunny today!",
            true
        );

        // When
        String formatted = responseFormatter.formatQueryResponse(queryResult);

        // Then
        assertThat(formatted).isNotNull();
        assertThat(formatted).contains("\nAssistant: It's sunny today!\n");
    }

    @Test
    @DisplayName("Should format error response")
    public void shouldFormatErrorResponse() {
        // Given
        QueryResult queryResult = new QueryResult(
            "Failing query",
            null,
            null,
            null,
            "Something went wrong",
            false
        );

        // When
        String formatted = responseFormatter.formatQueryResponse(queryResult);

        // Then
        assertThat(formatted).isNotNull();
        assertThat(formatted).contains("Error: Something went wrong\n");
    }

    @Test
    @DisplayName("Should handle null query result")
    public void shouldHandleNullQueryResult() {
        // When
        String formatted = responseFormatter.formatQueryResponse(null);

        // Then
        assertThat(formatted).isNotNull();
        assertThat(formatted).contains("Error: No response available\n");
    }

    @Test
    @DisplayName("Should format error message with prefix")
    public void shouldFormatErrorMessageWithPrefix() {
        // Given
        String errorMessage = "Connection failed";

        // When
        String formatted = responseFormatter.formatError(errorMessage);

        // Then
        assertThat(formatted).isEqualTo("Error: Connection failed\n");
    }

    @Test
    @DisplayName("Should not duplicate error prefix")
    public void shouldNotDuplicateErrorPrefix() {
        // Given
        String errorMessage = "Error: Already prefixed message";

        // When
        String formatted = responseFormatter.formatError(errorMessage);

        // Then
        assertThat(formatted).isEqualTo("Error: Already prefixed message\n");
        assertThat(formatted).doesNotContain("Error: Error:");
    }

    @Test
    @DisplayName("Should handle null error message")
    public void shouldHandleNullErrorMessage() {
        // When
        String formatted = responseFormatter.formatError(null);

        // Then
        assertThat(formatted).isEqualTo("Error: Unknown error occurred\n");
    }

    @Test
    @DisplayName("Should handle empty error message")
    public void shouldHandleEmptyErrorMessage() {
        // When
        String formatted = responseFormatter.formatError("");

        // Then
        assertThat(formatted).isEqualTo("Error: Unknown error occurred\n");
    }

    @Test
    @DisplayName("Should handle whitespace-only error message")
    public void shouldHandleWhitespaceOnlyErrorMessage() {
        // When
        String formatted = responseFormatter.formatError("   \t\n  ");

        // Then
        assertThat(formatted).isEqualTo("Error: Unknown error occurred\n");
    }

    @Test
    @DisplayName("Should format goodbye message")
    public void shouldFormatGoodbyeMessage() {
        // When
        String goodbye = responseFormatter.formatGoodbye();

        // Then
        assertThat(goodbye).isEqualTo("\nGoodbye!");
    }

    @Test
    @DisplayName("Should format welcome message")
    public void shouldFormatWelcomeMessage() {
        // When
        String welcome = responseFormatter.formatWelcome();

        // Then
        assertThat(welcome).isEqualTo("Assistant: Hello! How can I assist you today?");
    }

    @Test
    @DisplayName("Should format debug info when available")
    public void shouldFormatDebugInfoWhenAvailable() {
        // Given
        QueryResult queryResult = new QueryResult(
            "test query",
            "weather_agent",
            "specialized_agent",
            75,
            "response",
            true
        );

        // When
        String debugInfo = responseFormatter.formatDebugInfo(queryResult);

        // Then
        // Note: This test assumes debug logging is enabled in the test environment
        // The actual behavior depends on the logging configuration
        assertThat(debugInfo).isNotNull();
    }

    @Test
    @DisplayName("Should handle null query result for debug info")
    public void shouldHandleNullQueryResultForDebugInfo() {
        // When
        String debugInfo = responseFormatter.formatDebugInfo(null);

        // Then
        assertThat(debugInfo).isEqualTo("");
    }

    @Test
    @DisplayName("Should format response with null response text")
    public void shouldFormatResponseWithNullResponseText() {
        // Given
        QueryResult queryResult = new QueryResult(
            "test query",
            "test_agent",
            "test_agent",
            80,
            null,
            true
        );

        // When
        String formatted = responseFormatter.formatQueryResponse(queryResult);

        // Then
        assertThat(formatted).contains("\nAssistant: null\n");
    }

    @Test
    @DisplayName("Should format response with empty response text")
    public void shouldFormatResponseWithEmptyResponseText() {
        // Given
        QueryResult queryResult = new QueryResult(
            "test query",
            "test_agent",
            "test_agent",
            80,
            "",
            true
        );

        // When
        String formatted = responseFormatter.formatQueryResponse(queryResult);

        // Then
        assertThat(formatted).contains("\nAssistant: \n");
    }

    @Test
    @DisplayName("Should trim error message whitespace")
    public void shouldTrimErrorMessageWhitespace() {
        // Given
        String errorMessage = "  Network timeout  ";

        // When
        String formatted = responseFormatter.formatError(errorMessage);

        // Then
        assertThat(formatted).isEqualTo("Error: Network timeout\n");
    }

    @Test
    @DisplayName("Should handle multiline response")
    public void shouldHandleMultilineResponse() {
        // Given
        QueryResult queryResult = new QueryResult(
            "tell me a story",
            "story_agent",
            "story_agent",
            90,
            "Once upon a time...\nThere was a brave knight.\nThe end.",
            true
        );

        // When
        String formatted = responseFormatter.formatQueryResponse(queryResult);

        // Then
        assertThat(formatted).contains("\nAssistant: Once upon a time...\nThere was a brave knight.\nThe end.\n");
    }

    @Test
    @DisplayName("Should handle very long response")
    public void shouldHandleVeryLongResponse() {
        // Given
        StringBuilder longResponse = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longResponse.append("This is a very long response. ");
        }
        
        QueryResult queryResult = new QueryResult(
            "long query",
            "verbose_agent",
            "verbose_agent",
            95,
            longResponse.toString(),
            true
        );

        // When
        String formatted = responseFormatter.formatQueryResponse(queryResult);

        // Then
        assertThat(formatted).startsWith("\nAssistant: This is a very long response.");
        assertThat(formatted).endsWith("This is a very long response. \n");
        assertThat(formatted.length()).isGreaterThan(1000);
    }

    @Test
    @DisplayName("Should handle special characters in response")
    public void shouldHandleSpecialCharactersInResponse() {
        // Given
        QueryResult queryResult = new QueryResult(
            "special chars",
            "special_agent",
            "special_agent",
            80,
            "Response with émojis 🎉 and symbols: @#$%^&*()",
            true
        );

        // When
        String formatted = responseFormatter.formatQueryResponse(queryResult);

        // Then
        assertThat(formatted).contains("Response with émojis 🎉 and symbols: @#$%^&*()");
    }
}