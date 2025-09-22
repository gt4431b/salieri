package bill.zeacc.salieri.fifthgraph.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bill.zeacc.salieri.fifthgraph.service.InputHandler.InputAction;
import bill.zeacc.salieri.fifthgraph.service.InputHandler.InputActionType;

@DisplayName("InputHandler Tests")
public class InputHandlerTest {

    private InputHandler inputHandler;

    @BeforeEach
    protected void setUp() {
        // Create mock dependencies for InputHandler
        QueryProcessor mockQueryProcessor = mock(QueryProcessor.class);
        ResponseFormatter mockResponseFormatter = mock(ResponseFormatter.class);
        SessionManager mockSessionManager = mock(SessionManager.class);
        
        inputHandler = new InputHandler(mockQueryProcessor, mockResponseFormatter, mockSessionManager);
    }

    @Test
    @DisplayName("Should process valid query input")
    public void shouldProcessValidQueryInput() {
        // Given
        String input = "What is the weather today?";

        // When
        InputAction action = inputHandler.processInput(input);

        // Then
        assertThat(action).isNotNull();
        assertThat(action.getType()).isEqualTo(InputActionType.PROCESS_QUERY);
        assertThat(action.getProcessedInput()).isEqualTo(input);
        assertThat(action.shouldProcessQuery()).isTrue();
        assertThat(action.shouldExit()).isFalse();
        assertThat(action.shouldIgnore()).isFalse();
        assertThat(action.getReason()).contains("Valid query");
    }

    @Test
    @DisplayName("Should detect exit command - lowercase")
    public void shouldDetectExitCommandLowercase() {
        // Given
        String input = "exit";

        // When
        InputAction action = inputHandler.processInput(input);

        // Then
        assertThat(action).isNotNull();
        assertThat(action.getType()).isEqualTo(InputActionType.EXIT);
        assertThat(action.getProcessedInput()).isEqualTo(input);
        assertThat(action.shouldExit()).isTrue();
        assertThat(action.shouldProcessQuery()).isFalse();
        assertThat(action.shouldIgnore()).isFalse();
        assertThat(action.getReason()).contains("User requested exit");
    }

    @Test
    @DisplayName("Should detect exit command - uppercase")
    public void shouldDetectExitCommandUppercase() {
        // Given
        String input = "EXIT";

        // When
        InputAction action = inputHandler.processInput(input);

        // Then
        assertThat(action).isNotNull();
        assertThat(action.getType()).isEqualTo(InputActionType.EXIT);
        assertThat(action.shouldExit()).isTrue();
    }

    @Test
    @DisplayName("Should detect quit command - lowercase")
    public void shouldDetectQuitCommandLowercase() {
        // Given
        String input = "quit";

        // When
        InputAction action = inputHandler.processInput(input);

        // Then
        assertThat(action).isNotNull();
        assertThat(action.getType()).isEqualTo(InputActionType.EXIT);
        assertThat(action.shouldExit()).isTrue();
    }

    @Test
    @DisplayName("Should detect quit command - uppercase")
    public void shouldDetectQuitCommandUppercase() {
        // Given
        String input = "QUIT";

        // When
        InputAction action = inputHandler.processInput(input);

        // Then
        assertThat(action).isNotNull();
        assertThat(action.getType()).isEqualTo(InputActionType.EXIT);
        assertThat(action.shouldExit()).isTrue();
    }

    @Test
    @DisplayName("Should ignore empty input")
    public void shouldIgnoreEmptyInput() {
        // Given
        String input = "";

        // When
        InputAction action = inputHandler.processInput(input);

        // Then
        assertThat(action).isNotNull();
        assertThat(action.getType()).isEqualTo(InputActionType.IGNORE);
        assertThat(action.shouldIgnore()).isTrue();
        assertThat(action.shouldProcessQuery()).isFalse();
        assertThat(action.shouldExit()).isFalse();
        assertThat(action.getReason()).contains("Input is not a valid query");
    }

    @Test
    @DisplayName("Should ignore whitespace-only input")
    public void shouldIgnoreWhitespaceOnlyInput() {
        // Given
        String input = "   \t\n  ";

        // When
        InputAction action = inputHandler.processInput(input);

        // Then
        assertThat(action).isNotNull();
        assertThat(action.getType()).isEqualTo(InputActionType.IGNORE);
        assertThat(action.shouldIgnore()).isTrue();
        assertThat(action.getReason()).contains("Input is not a valid query");
    }

    @Test
    @DisplayName("Should handle null input")
    public void shouldHandleNullInput() {
        // Given
        String input = null;

        // When
        InputAction action = inputHandler.processInput(input);

        // Then
        assertThat(action).isNotNull();
        assertThat(action.getType()).isEqualTo(InputActionType.IGNORE);
        assertThat(action.shouldIgnore()).isTrue();
        assertThat(action.getReason()).contains("Input is null");
    }

    @Test
    @DisplayName("Should trim whitespace from valid input")
    public void shouldTrimWhitespaceFromValidInput() {
        // Given
        String input = "  Hello, how are you?  ";
        String expectedTrimmed = "Hello, how are you?";

        // When
        InputAction action = inputHandler.processInput(input);

        // Then
        assertThat(action).isNotNull();
        assertThat(action.getType()).isEqualTo(InputActionType.PROCESS_QUERY);
        assertThat(action.getProcessedInput()).isEqualTo(expectedTrimmed);
        assertThat(action.shouldProcessQuery()).isTrue();
    }

    @Test
    @DisplayName("Should trim whitespace from exit commands")
    public void shouldTrimWhitespaceFromExitCommands() {
        // Given
        String input = "  exit  ";

        // When
        InputAction action = inputHandler.processInput(input);

        // Then
        assertThat(action).isNotNull();
        assertThat(action.getType()).isEqualTo(InputActionType.EXIT);
        assertThat(action.getProcessedInput()).isEqualTo("exit");
        assertThat(action.shouldExit()).isTrue();
    }

    @Test
    @DisplayName("Should process single character input")
    public void shouldProcessSingleCharacterInput() {
        // Given
        String input = "?";

        // When
        InputAction action = inputHandler.processInput(input);

        // Then
        assertThat(action).isNotNull();
        assertThat(action.getType()).isEqualTo(InputActionType.PROCESS_QUERY);
        assertThat(action.shouldProcessQuery()).isTrue();
    }

    @Test
    @DisplayName("Should process long input")
    public void shouldProcessLongInput() {
        // Given
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longInput.append("This is a very long query. ");
        }
        String input = longInput.toString();

        // When
        InputAction action = inputHandler.processInput(input);

        // Then
        assertThat(action).isNotNull();
        assertThat(action.getType()).isEqualTo(InputActionType.PROCESS_QUERY);
        assertThat(action.shouldProcessQuery()).isTrue();
        assertThat(action.getProcessedInput()).isEqualTo(input.trim());
    }

    @Test
    @DisplayName("Should handle input that contains exit as substring")
    public void shouldHandleInputThatContainsExitAsSubstring() {
        // Given
        String input = "How do I exit this program?";

        // When
        InputAction action = inputHandler.processInput(input);

        // Then
        assertThat(action).isNotNull();
        assertThat(action.getType()).isEqualTo(InputActionType.PROCESS_QUERY);
        assertThat(action.shouldProcessQuery()).isTrue();
        assertThat(action.shouldExit()).isFalse();
    }

    @Test
    @DisplayName("Should handle mixed case exit commands")
    public void shouldHandleMixedCaseExitCommands() {
        // When/Then
        assertThat(inputHandler.processInput("Exit").shouldExit()).isTrue();
        assertThat(inputHandler.processInput("eXiT").shouldExit()).isTrue();
        assertThat(inputHandler.processInput("Quit").shouldExit()).isTrue();
        assertThat(inputHandler.processInput("qUiT").shouldExit()).isTrue();
    }

    @Test
    @DisplayName("Should provide meaningful reasons for actions")
    public void shouldProvideMeaningfulReasonsForActions() {
        // Test various inputs and verify reasons
        InputAction queryAction = inputHandler.processInput("test query");
        assertThat(queryAction.getReason()).isNotNull().contains("Valid query");

        InputAction exitAction = inputHandler.processInput("exit");
        assertThat(exitAction.getReason()).isNotNull().contains("User requested exit");

        InputAction emptyAction = inputHandler.processInput("");
        assertThat(emptyAction.getReason()).isNotNull().contains("Input is not a valid query");

        InputAction nullAction = inputHandler.processInput(null);
        assertThat(nullAction.getReason()).isNotNull().contains("Input is null");
    }

    @Test
    public void invalidInputTest ( ) {
    	InputAction processInput = inputHandler.processInput ( "" ) ;
    	assertThat ( processInput.getType ( ) ).isEqualTo ( InputActionType.IGNORE ) ;
    }
}
