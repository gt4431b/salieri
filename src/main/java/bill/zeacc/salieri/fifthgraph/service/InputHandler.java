package bill.zeacc.salieri.fifthgraph.service;

import org.springframework.stereotype.Service;

import bill.zeacc.salieri.fifthgraph.service.QueryProcessor.QueryResult;
import bill.zeacc.salieri.fifthgraph.util.DebouncedStdInBlocks;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for handling and validating user input, and orchestrating
 * the complete user input processing workflow.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InputHandler {

    private final QueryProcessor queryProcessor;
    private final ResponseFormatter responseFormatter;
    private final SessionManager sessionManager;

    /**
     * Processes raw user input and returns the appropriate action to take.
     *
     * @param rawInput the raw input from the user
     * @return an InputAction indicating what should be done with this input
     */
    public InputAction processInput(String rawInput) {
        if (rawInput == null) {
            return new InputAction(InputActionType.IGNORE, null, "Input is null");
        }

        String trimmedInput = rawInput.trim();
        
        if (isExitCommand(trimmedInput)) {
            log.debug("Exit command detected: {}", trimmedInput);
            return new InputAction(InputActionType.EXIT, trimmedInput, "User requested exit");
        }

        if (isValidQuery(trimmedInput)) {
            log.debug("Valid query received: {}", trimmedInput);
            return new InputAction(InputActionType.PROCESS_QUERY, trimmedInput, "Valid query to process");
        }
        
        log.debug("Invalid input received: {}", trimmedInput);
        return new InputAction(InputActionType.IGNORE, trimmedInput, "Input is not a valid query");
    }

    /**
     * Handles user input through the complete workflow including processing and output.
     * This method orchestrates the entire user input flow and handles I/O operations.
     *
     * @param rawInput the raw user input
     * @param sessionId the session identifier
     * @param ctx the CLI context for controlling the application flow
     */
    public void handleUserInput(String rawInput, String sessionId, DebouncedStdInBlocks.CliContext ctx) {
        log.debug("Handling user input for session: {}", sessionId);
        
        InputAction action = processInput(rawInput);
        
        if (action.shouldExit()) {
            handleExitAction(sessionId, ctx);
            return;
        }
        
        if (action.shouldIgnore()) {
            handleIgnoreAction(action);
            return;
        }
        
        if (action.shouldProcessQuery()) {
            handleQueryAction(action, sessionId);
            return;
        }
        
        // This should never happen with current implementation, but handle gracefully
        log.warn("Unexpected input action type: {}", action.getType());
        String errorResponse = responseFormatter.formatError("Unexpected input type");
        System.out.print(errorResponse);
    }

    private void handleExitAction(String sessionId, DebouncedStdInBlocks.CliContext ctx) {
        log.debug("Handling exit action for session: {}", sessionId);
        
        String goodbyeMessage = responseFormatter.formatGoodbye();
        boolean sessionTerminated = sessionManager.terminateSession(sessionId);
        
        if (!sessionTerminated) {
            log.warn("Failed to terminate session: {}", sessionId);
        }
        
        System.out.println(goodbyeMessage);
        ctx.stop();
    }

    private void handleIgnoreAction(InputAction action) {
        log.debug("Ignoring input: {}", action.getReason());
        // Do nothing for ignored input - no output
    }

    private void handleQueryAction(InputAction action, String sessionId) {
        log.debug("Processing query: {}", action.getProcessedInput());
        
        QueryResult result = queryProcessor.processQuery(action.getProcessedInput(), sessionId);
        String response = responseFormatter.formatQueryResponse(result);
        
        System.out.print(response);
    }

    private boolean isExitCommand(String input) {
        return input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit");
    }

    private boolean isValidQuery(String input) {
        // Add any validation logic here - for now, any non-empty, non-exit input is valid
        return input.length() > 0;
    }

    /**
     * Represents the type of action to take based on user input.
     */
    public enum InputActionType {
        PROCESS_QUERY,  // Input should be processed as a query
        EXIT,           // User wants to exit the application
        IGNORE          // Input should be ignored (empty, invalid, etc.)
    }

    /**
     * Represents an action to take based on processed input.
     */
    public static class InputAction {
        private final InputActionType type;
        private final String processedInput;
        private final String reason;

        public InputAction(InputActionType type, String processedInput, String reason) {
            this.type = type;
            this.processedInput = processedInput;
            this.reason = reason;
        }

        public InputActionType getType() { return type; }
        public String getProcessedInput() { return processedInput; }
        public String getReason() { return reason; }

        public boolean shouldProcessQuery() { return type == InputActionType.PROCESS_QUERY; }
        public boolean shouldExit() { return type == InputActionType.EXIT; }
        public boolean shouldIgnore() { return type == InputActionType.IGNORE; }
    }
}