package bill.zeacc.salieri.fifthgraph.service;

import org.springframework.stereotype.Service;

import bill.zeacc.salieri.fifthgraph.agents.switchboard.SwitchboardState;
import bill.zeacc.salieri.fifthgraph.model.states.ResultOrientedState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for processing user queries through the agent system.
 * This class contains the core business logic extracted from the main application
 * to make it testable without threading concerns.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QueryProcessor {

    private final GraphService graphService;
    private static final String DEFAULT_AGENT = "default_agent";
    private static final String SWITCHBOARD_AGENT = "switchboard_agent";
    private static final int CONFIDENCE_THRESHOLD = 50;

    /**
     * Processes a user query by first routing it through the switchboard agent
     * to determine the best specialized agent, then processing it with that agent.
     *
     * @param query the user's input query
     * @param sessionId the session identifier
     * @return the final response from the processing agent
     */
    public QueryResult processQuery(String query, String sessionId) {
        try {
            log.debug("Processing query for session: {}", sessionId);
            
            // First, route through switchboard agent
            ResultOrientedState switchboardResponse = graphService.processQuery(SWITCHBOARD_AGENT, query, sessionId);
            
            // Extract routing information
            String recommendedAgent = extractRecommendedAgent(switchboardResponse);
            Integer confidence = extractConfidence(switchboardResponse);
            
            // Apply confidence threshold logic
            String targetAgent = determineTargetAgent(recommendedAgent, confidence);
            
            // Process with the selected agent
            ResultOrientedState finalResponse = graphService.processQuery(targetAgent, query, sessionId);
            
            return new QueryResult(
                query,
                targetAgent,
                recommendedAgent,
                confidence,
                finalResponse.getFinalAnswer(),
                true
            );
            
        } catch (Exception e) {
            log.error("Error processing query: {}", query, e);
            return new QueryResult(
                query,
                null,
                null,
                null,
                "Error: " + e.getMessage(),
                false
            );
        }
    }

    private String extractRecommendedAgent(ResultOrientedState response) {
        return (String) response.value(SwitchboardState.RECOMMENDED_AGENT_KEY)
            .orElse(DEFAULT_AGENT);
    }

    private Integer extractConfidence(ResultOrientedState response) {
        return (Integer) response.value(SwitchboardState.CONFIDENCE_KEY)
            .orElse(0);
    }

    private String determineTargetAgent(String recommendedAgent, Integer confidence) {
        if (confidence < CONFIDENCE_THRESHOLD) {
            log.debug("Confidence {} below threshold {}, using default agent", confidence, CONFIDENCE_THRESHOLD);
            return DEFAULT_AGENT;
        }
        return recommendedAgent;
    }

    /**
     * Result object containing all information about a processed query.
     */
    public static class QueryResult {
        private final String originalQuery;
        private final String targetAgent;
        private final String recommendedAgent;
        private final Integer confidence;
        private final String response;
        private final boolean successful;

        public QueryResult(String originalQuery, String targetAgent, String recommendedAgent, 
                          Integer confidence, String response, boolean successful) {
            this.originalQuery = originalQuery;
            this.targetAgent = targetAgent;
            this.recommendedAgent = recommendedAgent;
            this.confidence = confidence;
            this.response = response;
            this.successful = successful;
        }

        public String getOriginalQuery() { return originalQuery; }
        public String getTargetAgent() { return targetAgent; }
        public String getRecommendedAgent() { return recommendedAgent; }
        public Integer getConfidence() { return confidence; }
        public String getResponse() { return response; }
        public boolean isSuccessful() { return successful; }
    }
}
