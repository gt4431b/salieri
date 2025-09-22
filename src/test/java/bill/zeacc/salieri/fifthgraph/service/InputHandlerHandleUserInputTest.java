package bill.zeacc.salieri.fifthgraph.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import bill.zeacc.salieri.fifthgraph.service.QueryProcessor.QueryResult;
import bill.zeacc.salieri.fifthgraph.util.DebouncedStdInBlocks;

@DisplayName("InputHandler handleUserInput Tests")
public class InputHandlerHandleUserInputTest {

    @Mock
    private QueryProcessor mockQueryProcessor;
    
    @Mock
    private ResponseFormatter mockResponseFormatter;
    
    @Mock
    private SessionManager mockSessionManager;

    private InputHandler inputHandler;

    @BeforeEach
    protected void setUp() {
        MockitoAnnotations.openMocks(this);
        inputHandler = new InputHandler(mockQueryProcessor, mockResponseFormatter, mockSessionManager);
    }

    @Test
    @DisplayName("Should handle user input with query processing")
    public void shouldHandleUserInputWithQueryProcessing() {
        // Given
        String userInput = "What is the weather?";
        String sessionId = "test-session";
        DebouncedStdInBlocks.CliContext mockCtx = mock(DebouncedStdInBlocks.CliContext.class);
        
        QueryResult queryResult = new QueryResult(userInput, "weather_agent", "weather_agent", 85, "It's sunny!", true);
        String formattedResponse = "\nAssistant: It's sunny!\n";

        when(mockQueryProcessor.processQuery(userInput, sessionId)).thenReturn(queryResult);
        when(mockResponseFormatter.formatQueryResponse(queryResult)).thenReturn(formattedResponse);

        // When
        inputHandler.handleUserInput(userInput, sessionId, mockCtx);

        // Then
        verify(mockQueryProcessor).processQuery(userInput, sessionId);
        verify(mockResponseFormatter).formatQueryResponse(queryResult);
        // Note: We can't easily verify System.out.print() calls in unit tests
    }

    @Test
    @DisplayName("Should handle user input with exit command")
    public void shouldHandleUserInputWithExitCommand() {
        // Given
        String userInput = "exit";
        String sessionId = "test-session";
        DebouncedStdInBlocks.CliContext mockCtx = mock(DebouncedStdInBlocks.CliContext.class);
        String goodbyeMessage = "\nGoodbye!";

        when(mockSessionManager.terminateSession(sessionId)).thenReturn(true);
        when(mockResponseFormatter.formatGoodbye()).thenReturn(goodbyeMessage);

        // When
        inputHandler.handleUserInput(userInput, sessionId, mockCtx);

        // Then
        verify(mockSessionManager).terminateSession(sessionId);
        verify(mockResponseFormatter).formatGoodbye();
        verify(mockCtx).stop();
    }

    @Test
    @DisplayName("Should handle user input with ignored input")
    public void shouldHandleUserInputWithIgnoredInput() {
        // Given
        String userInput = "";
        String sessionId = "test-session";
        DebouncedStdInBlocks.CliContext mockCtx = mock(DebouncedStdInBlocks.CliContext.class);

        // When
        inputHandler.handleUserInput(userInput, sessionId, mockCtx);

        // Then - verify that no processing services are called for ignored input
        verify(mockQueryProcessor, never()).processQuery(anyString(), anyString());
        verify(mockSessionManager, never()).terminateSession(anyString());
        verify(mockCtx, never()).stop();
    }

    @Test
    @DisplayName("Should handle session termination failure gracefully")
    public void shouldHandleSessionTerminationFailureGracefully() {
        // Given
        String userInput = "quit";
        String sessionId = "test-session";
        DebouncedStdInBlocks.CliContext mockCtx = mock(DebouncedStdInBlocks.CliContext.class);
        String goodbyeMessage = "\nGoodbye!";

        when(mockSessionManager.terminateSession(sessionId)).thenReturn(false); // Simulate failure
        when(mockResponseFormatter.formatGoodbye()).thenReturn(goodbyeMessage);

        // When
        inputHandler.handleUserInput(userInput, sessionId, mockCtx);

        // Then - should still proceed with goodbye and stop
        verify(mockSessionManager).terminateSession(sessionId);
        verify(mockResponseFormatter).formatGoodbye();
        verify(mockCtx).stop();
    }

    @Test
    @DisplayName("Should handle query processing with debug info")
    public void shouldHandleQueryProcessingWithDebugInfo() {
        // Given
        String userInput = "debug query";
        String sessionId = "test-session";
        DebouncedStdInBlocks.CliContext mockCtx = mock(DebouncedStdInBlocks.CliContext.class);
        
        QueryResult queryResult = new QueryResult(userInput, "debug_agent", "debug_agent", 95, "Debug response", true);
        String formattedResponse = "\nAssistant: Debug response\n";
        String debugInfo = "[Debug] Agent: debug_agent, Confidence: 95\n";

        when(mockQueryProcessor.processQuery(userInput, sessionId)).thenReturn(queryResult);
        when(mockResponseFormatter.formatQueryResponse(queryResult)).thenReturn(formattedResponse);
        when(mockResponseFormatter.formatDebugInfo(queryResult)).thenReturn(debugInfo);

        // When
        inputHandler.handleUserInput(userInput, sessionId, mockCtx);

        // Then
        verify(mockQueryProcessor).processQuery(userInput, sessionId);
        verify(mockResponseFormatter).formatQueryResponse(queryResult);
    }

    @Test
    @DisplayName("Should handle null input gracefully")
    public void shouldHandleNullInputGracefully() {
        // Given
        String userInput = null;
        String sessionId = "test-session";
        DebouncedStdInBlocks.CliContext mockCtx = mock(DebouncedStdInBlocks.CliContext.class);

        // When
        inputHandler.handleUserInput(userInput, sessionId, mockCtx);

        // Then - should be ignored, no services called
        verify(mockQueryProcessor, never()).processQuery(anyString(), anyString());
        verify(mockSessionManager, never()).terminateSession(anyString());
        verify(mockCtx, never()).stop();
    }

    @Test
    @DisplayName("Should handle whitespace-only input gracefully")
    public void shouldHandleWhitespaceOnlyInputGracefully() {
        // Given
        String userInput = "   \t\n  ";
        String sessionId = "test-session";
        DebouncedStdInBlocks.CliContext mockCtx = mock(DebouncedStdInBlocks.CliContext.class);

        // When
        inputHandler.handleUserInput(userInput, sessionId, mockCtx);

        // Then - should be ignored, no services called
        verify(mockQueryProcessor, never()).processQuery(anyString(), anyString());
        verify(mockSessionManager, never()).terminateSession(anyString());
        verify(mockCtx, never()).stop();
    }

    @Test
    @DisplayName("Should handle mixed case exit commands in full workflow")
    public void shouldHandleMixedCaseExitCommandsInFullWorkflow() {
        // Given
        String userInput = "EXIT";
        String sessionId = "test-session";
        DebouncedStdInBlocks.CliContext mockCtx = mock(DebouncedStdInBlocks.CliContext.class);
        String goodbyeMessage = "\nGoodbye!";

        when(mockSessionManager.terminateSession(sessionId)).thenReturn(true);
        when(mockResponseFormatter.formatGoodbye()).thenReturn(goodbyeMessage);

        // When
        inputHandler.handleUserInput(userInput, sessionId, mockCtx);

        // Then
        verify(mockSessionManager).terminateSession(sessionId);
        verify(mockResponseFormatter).formatGoodbye();
        verify(mockCtx).stop();
    }

    @Test
    @DisplayName("Should handle query processing errors gracefully")
    public void shouldHandleQueryProcessingErrorsGracefully() {
        // Given
        String userInput = "failing query";
        String sessionId = "test-session";
        DebouncedStdInBlocks.CliContext mockCtx = mock(DebouncedStdInBlocks.CliContext.class);
        
        QueryResult errorResult = new QueryResult(userInput, null, null, null, "Error: Something went wrong", false);
        String formattedError = "Error: Something went wrong\n";

        when(mockQueryProcessor.processQuery(userInput, sessionId)).thenReturn(errorResult);
        when(mockResponseFormatter.formatQueryResponse(errorResult)).thenReturn(formattedError);

        // When
        inputHandler.handleUserInput(userInput, sessionId, mockCtx);

        // Then
        verify(mockQueryProcessor).processQuery(userInput, sessionId);
        verify(mockResponseFormatter).formatQueryResponse(errorResult);
    }

    @Test
    @DisplayName("Should integrate complete workflow correctly")
    public void shouldIntegrateCompleteWorkflowCorrectly() {
        // This test verifies the complete integration of the handleUserInput method
        // Given
        String userInput = "Tell me a joke";
        String sessionId = "integration-session";
        DebouncedStdInBlocks.CliContext mockCtx = mock(DebouncedStdInBlocks.CliContext.class);
        
        QueryResult queryResult = new QueryResult(
            userInput, 
            "humor_agent", 
            "humor_agent", 
            92, 
            "Why did the programmer quit his job? Because he didn't get arrays!",
            true
        );
        String formattedResponse = "\nAssistant: Why did the programmer quit his job? Because he didn't get arrays!\n";

        when(mockQueryProcessor.processQuery(userInput, sessionId)).thenReturn(queryResult);
        when(mockResponseFormatter.formatQueryResponse(queryResult)).thenReturn(formattedResponse);

        // When
        inputHandler.handleUserInput(userInput, sessionId, mockCtx);

        // Then - verify the complete workflow
        verify(mockQueryProcessor).processQuery(userInput, sessionId);
        verify(mockResponseFormatter).formatQueryResponse(queryResult);
        verify(mockCtx, never()).stop(); // Should not stop for regular queries
        verify(mockSessionManager, never()).terminateSession(anyString()); // Should not terminate for regular queries
    }
}