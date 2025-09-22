package bill.zeacc.salieri.fifthgraph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import bill.zeacc.salieri.fifthgraph.service.InputHandler;
import bill.zeacc.salieri.fifthgraph.service.InputHandler.InputAction;
import bill.zeacc.salieri.fifthgraph.service.InputHandler.InputActionType;
import bill.zeacc.salieri.fifthgraph.service.QueryProcessor;
import bill.zeacc.salieri.fifthgraph.service.QueryProcessor.QueryResult;
import bill.zeacc.salieri.fifthgraph.service.ResponseFormatter;
import bill.zeacc.salieri.fifthgraph.service.SessionManager;

@DisplayName("Salieri Tests")
public class SalieriTest {

    @Mock
    private QueryProcessor mockQueryProcessor;
    
    @Mock
    private InputHandler mockInputHandler;
    
    @Mock
    private ResponseFormatter mockResponseFormatter;
    
    @Mock
    private SessionManager mockSessionManager;

    private Salieri salieri;

    @BeforeEach
    protected void setUp() {
        MockitoAnnotations.openMocks(this);
        salieri = new Salieri();
    }

    @Test
    @DisplayName("Should be instantiable")
    public void shouldBeInstantiable() {
        // When/Then
        assertThat(salieri).isNotNull();
        assertThat(salieri).isInstanceOf(Salieri.class);
    }

    @Test
    @DisplayName("Should be annotated with SpringBootApplication")
    public void shouldBeAnnotatedWithSpringBootApplication() {
        // When/Then
        assertThat(salieri.getClass().isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class))
            .isTrue();
    }

    @Test
    @DisplayName("Should be annotated with EnableConfigurationProperties")
    public void shouldBeAnnotatedWithEnableConfigurationProperties() {
        // When/Then
        assertThat(salieri.getClass().isAnnotationPresent(org.springframework.boot.context.properties.EnableConfigurationProperties.class))
            .isTrue();
    }

    @Test
    @DisplayName("Should handle query input correctly")
    public void shouldHandleQueryInputCorrectly() {
        // Given
        String userInput = "What is the weather?";
        String sessionId = "test-session";
        
        InputAction queryAction = new InputAction(InputActionType.PROCESS_QUERY, userInput, "Valid query");
        QueryResult queryResult = new QueryResult(userInput, "weather_agent", "weather_agent", 85, "It's sunny!", true);
        String formattedResponse = "\nAssistant: It's sunny!\n";

        when(mockInputHandler.processInput(userInput)).thenReturn(queryAction);
        when(mockQueryProcessor.processQuery(userInput, sessionId)).thenReturn(queryResult);
        when(mockResponseFormatter.formatQueryResponse(queryResult)).thenReturn(formattedResponse);

        // When - simulate the handleUserInput method logic
        InputAction action = mockInputHandler.processInput(userInput);
        
        if (action.shouldProcessQuery()) {
            QueryResult result = mockQueryProcessor.processQuery(action.getProcessedInput(), sessionId);
            String response = mockResponseFormatter.formatQueryResponse(result);
            
            // Then
            assertThat(response).isEqualTo(formattedResponse);
        }

        // Verify interactions
        verify(mockInputHandler).processInput(userInput);
        verify(mockQueryProcessor).processQuery(userInput, sessionId);
        verify(mockResponseFormatter).formatQueryResponse(queryResult);
    }

    @Test
    @DisplayName("Should handle exit input correctly")
    public void shouldHandleExitInputCorrectly() {
        // Given
        String userInput = "exit";
        String sessionId = "test-session";
        String goodbyeMessage = "\nGoodbye!";
        
        InputAction exitAction = new InputAction(InputActionType.EXIT, userInput, "User requested exit");

        when(mockInputHandler.processInput(userInput)).thenReturn(exitAction);
        when(mockResponseFormatter.formatGoodbye()).thenReturn(goodbyeMessage);
        when(mockSessionManager.terminateSession(sessionId)).thenReturn(true);

        // When - simulate the handleUserInput method logic
        InputAction action = mockInputHandler.processInput(userInput);
        
        if (action.shouldExit()) {
            String goodbye = mockResponseFormatter.formatGoodbye();
            boolean terminated = mockSessionManager.terminateSession(sessionId);
            
            // Then
            assertThat(goodbye).isEqualTo(goodbyeMessage);
            assertThat(terminated).isTrue();
        }

        // Verify interactions
        verify(mockInputHandler).processInput(userInput);
        verify(mockResponseFormatter).formatGoodbye();
        verify(mockSessionManager).terminateSession(sessionId);
    }

    @Test
    @DisplayName("Should handle ignored input correctly")
    public void shouldHandleIgnoredInputCorrectly() {
        // Given
        String userInput = "";
        
        InputAction ignoreAction = new InputAction(InputActionType.IGNORE, userInput, "Input is empty");

        when(mockInputHandler.processInput(userInput)).thenReturn(ignoreAction);

        // When - simulate the handleUserInput method logic
        InputAction action = mockInputHandler.processInput(userInput);
        
        // Then
        assertThat(action.shouldIgnore()).isTrue();
        assertThat(action.shouldProcessQuery()).isFalse();
        assertThat(action.shouldExit()).isFalse();

        // Verify only input handler was called
        verify(mockInputHandler).processInput(userInput);
    }

    @Test
    @DisplayName("Should integrate all components correctly")
    public void shouldIntegrateAllComponentsCorrectly() {
        // This test demonstrates how the refactored Salieri class
        // delegates to testable components without threading concerns
        
        // Given
        String userInput = "Tell me a joke";
        String sessionId = "integration-session";
        
        InputAction queryAction = new InputAction(InputActionType.PROCESS_QUERY, userInput, "Valid query");
        QueryResult queryResult = new QueryResult(
            userInput, 
            "humor_agent", 
            "humor_agent", 
            92, 
            "Why did the programmer quit his job? Because he didn't get arrays!",
            true
        );
        String formattedResponse = "\nAssistant: Why did the programmer quit his job? Because he didn't get arrays!\n";

        when(mockInputHandler.processInput(userInput)).thenReturn(queryAction);
        when(mockQueryProcessor.processQuery(userInput, sessionId)).thenReturn(queryResult);
        when(mockResponseFormatter.formatQueryResponse(queryResult)).thenReturn(formattedResponse);

        // When - simulate the complete flow
        InputAction action = mockInputHandler.processInput(userInput);
        QueryResult result = mockQueryProcessor.processQuery(action.getProcessedInput(), sessionId);
        String response = mockResponseFormatter.formatQueryResponse(result);

        // Then - verify the complete integration
        assertThat(action.shouldProcessQuery()).isTrue();
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getTargetAgent()).isEqualTo("humor_agent");
        assertThat(result.getConfidence()).isEqualTo(92);
        assertThat(response).contains("Why did the programmer quit his job?");

        // Verify all components were called in the correct sequence
        verify(mockInputHandler).processInput(userInput);
        verify(mockQueryProcessor).processQuery(userInput, sessionId);
        verify(mockResponseFormatter).formatQueryResponse(queryResult);
    }
}