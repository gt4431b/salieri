package bill.zeacc.salieri.fifthgraph.service;

import org.springframework.stereotype.Service;

import bill.zeacc.salieri.fifthgraph.service.QueryProcessor.QueryResult;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for formatting responses for display to the user.
 * Extracted from the main application to enable testing without I/O concerns.
 */
@Service
@Slf4j
public class ResponseFormatter {

    private static final String ASSISTANT_PREFIX = "\nAssistant: ";
    private static final String ERROR_PREFIX = "Error: ";
    private static final String GOODBYE_MESSAGE = "\nGoodbye!";
    private static final String WELCOME_MESSAGE = "Assistant: Hello! How can I assist you today?";

    /**
     * Formats a successful query result for display.
     *
     * @param queryResult the result of processing a query
     * @return formatted response string
     */
    public String formatQueryResponse(QueryResult queryResult) {
        if (queryResult == null) {
            log.warn("Received null query result");
            return formatError("No response available");
        }

        if (!queryResult.isSuccessful()) {
            log.debug("Formatting error response for query: {}", queryResult.getOriginalQuery());
            return formatError(queryResult.getResponse());
        }

        log.debug("Formatting successful response for query: {} using agent: {}", 
                 queryResult.getOriginalQuery(), queryResult.getTargetAgent());
        
        return ASSISTANT_PREFIX + queryResult.getResponse() + "\n";
    }

    /**
     * Formats an error message for display.
     *
     * @param errorMessage the error message to format
     * @return formatted error string
     */
    public String formatError(String errorMessage) {
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            return ERROR_PREFIX + "Unknown error occurred\n";
        }
        
        String message = errorMessage.trim();
        if (!message.startsWith(ERROR_PREFIX)) {
            message = ERROR_PREFIX + message;
        }
        
        return message + "\n";
    }

    /**
     * Returns the goodbye message for when the user exits.
     *
     * @return formatted goodbye message
     */
    public String formatGoodbye() {
        return GOODBYE_MESSAGE;
    }

    /**
     * Returns the welcome message for when the application starts.
     *
     * @return formatted welcome message
     */
    public String formatWelcome() {
        return WELCOME_MESSAGE;
    }

    /**
     * Formats debug information about the query processing.
     *
     * @param queryResult the query result containing debug info
     * @return formatted debug string, or empty string if debug is disabled
     */
    public String formatDebugInfo(QueryResult queryResult) {
        if (queryResult == null) {
            return "";
        }

        StringBuilder debug = new StringBuilder();
        debug.append("[Debug] ");
        debug.append("Recommended: ").append(queryResult.getRecommendedAgent());
        debug.append(", Confidence: ").append(queryResult.getConfidence());
        debug.append(", Used: ").append(queryResult.getTargetAgent());
        debug.append("\n");

        return debug.toString();
    }
}