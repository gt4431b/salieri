package bill.zeacc.salieri.fifthgraph.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import bill.zeacc.salieri.fifthgraph.agents.switchboard.SwitchboardState;
import bill.zeacc.salieri.fifthgraph.model.states.ResultOrientedState;
import bill.zeacc.salieri.fifthgraph.service.QueryProcessor.QueryResult;

@DisplayName("QueryProcessor Tests")
public class QueryProcessorTest {

    @Mock
    private GraphService mockGraphService;

    private QueryProcessor queryProcessor;

    @BeforeEach
    protected void setUp() {
        MockitoAnnotations.openMocks(this);
        queryProcessor = new QueryProcessor(mockGraphService);
    }

    @Test
    @DisplayName("Should process query successfully with high confidence")
    public void shouldProcessQuerySuccessfullyWithHighConfidence() throws Exception {
        // Given
        String query = "What is the weather today?";
        String sessionId = "session123";
        String recommendedAgent = "weather_agent";
        Integer confidence = 85;
        String finalAnswer = "It's sunny today!";

        ResultOrientedState switchboardResponse = createMockSwitchboardResponse(recommendedAgent, confidence);
        ResultOrientedState finalResponse = createMockFinalResponse(finalAnswer);

        when(mockGraphService.processQuery("switchboard_agent", query, sessionId))
            .thenReturn(switchboardResponse);
        when(mockGraphService.processQuery(recommendedAgent, query, sessionId))
            .thenReturn(finalResponse);

        // When
        QueryResult result = queryProcessor.processQuery(query, sessionId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getOriginalQuery()).isEqualTo(query);
        assertThat(result.getRecommendedAgent()).isEqualTo(recommendedAgent);
        assertThat(result.getTargetAgent()).isEqualTo(recommendedAgent);
        assertThat(result.getConfidence()).isEqualTo(confidence);
        assertThat(result.getResponse()).isEqualTo(finalAnswer);

        verify(mockGraphService).processQuery("switchboard_agent", query, sessionId);
        verify(mockGraphService).processQuery(recommendedAgent, query, sessionId);
    }

    @Test
    @DisplayName("Should use default agent when confidence is below threshold")
    public void shouldUseDefaultAgentWhenConfidenceBelowThreshold() throws Exception {
        // Given
        String query = "Some ambiguous query";
        String sessionId = "session123";
        String recommendedAgent = "specialized_agent";
        Integer confidence = 30; // Below threshold of 50
        String finalAnswer = "I'll help you with that.";

        ResultOrientedState switchboardResponse = createMockSwitchboardResponse(recommendedAgent, confidence);
        ResultOrientedState finalResponse = createMockFinalResponse(finalAnswer);

        when(mockGraphService.processQuery("switchboard_agent", query, sessionId))
            .thenReturn(switchboardResponse);
        when(mockGraphService.processQuery("default_agent", query, sessionId))
            .thenReturn(finalResponse);

        // When
        QueryResult result = queryProcessor.processQuery(query, sessionId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getRecommendedAgent()).isEqualTo(recommendedAgent);
        assertThat(result.getTargetAgent()).isEqualTo("default_agent");
        assertThat(result.getConfidence()).isEqualTo(confidence);

        verify(mockGraphService).processQuery("switchboard_agent", query, sessionId);
        verify(mockGraphService).processQuery("default_agent", query, sessionId);
    }

    @Test
    @DisplayName("Should handle missing recommended agent gracefully")
    public void shouldHandleMissingRecommendedAgentGracefully() throws Exception {
        // Given
        String query = "test query";
        String sessionId = "session123";
        Integer confidence = 75;

        ResultOrientedState switchboardResponse = mock(ResultOrientedState.class);
        when(switchboardResponse.value(SwitchboardState.RECOMMENDED_AGENT_KEY))
            .thenReturn(Optional.empty());
        when(switchboardResponse.value(SwitchboardState.CONFIDENCE_KEY))
            .thenReturn(Optional.of(confidence));

        ResultOrientedState finalResponse = createMockFinalResponse("Default response");

        when(mockGraphService.processQuery("switchboard_agent", query, sessionId))
            .thenReturn(switchboardResponse);
        when(mockGraphService.processQuery("default_agent", query, sessionId))
            .thenReturn(finalResponse);

        // When
        QueryResult result = queryProcessor.processQuery(query, sessionId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getRecommendedAgent()).isEqualTo("default_agent");
        assertThat(result.getTargetAgent()).isEqualTo("default_agent");
    }

    @Test
    @DisplayName("Should handle missing confidence gracefully")
    public void shouldHandleMissingConfidenceGracefully() throws Exception {
        // Given
        String query = "test query";
        String sessionId = "session123";
        String recommendedAgent = "test_agent";

        ResultOrientedState switchboardResponse = mock(ResultOrientedState.class);
        when(switchboardResponse.value(SwitchboardState.RECOMMENDED_AGENT_KEY))
            .thenReturn(Optional.of(recommendedAgent));
        when(switchboardResponse.value(SwitchboardState.CONFIDENCE_KEY))
            .thenReturn(Optional.empty());

        ResultOrientedState finalResponse = createMockFinalResponse("Default response");

        when(mockGraphService.processQuery("switchboard_agent", query, sessionId))
            .thenReturn(switchboardResponse);
        when(mockGraphService.processQuery("default_agent", query, sessionId))
            .thenReturn(finalResponse);

        // When
        QueryResult result = queryProcessor.processQuery(query, sessionId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getConfidence()).isEqualTo(0);
        assertThat(result.getTargetAgent()).isEqualTo("default_agent"); // Should use default due to 0 confidence
    }

    @Test
    @DisplayName("Should handle graph service exception gracefully")
    public void shouldHandleGraphServiceExceptionGracefully() throws Exception {
        // Given
        String query = "failing query";
        String sessionId = "session123";
        RuntimeException exception = new RuntimeException("Graph service error");

        when(mockGraphService.processQuery(anyString(), eq(query), eq(sessionId)))
            .thenThrow(exception);

        // When
        QueryResult result = queryProcessor.processQuery(query, sessionId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getOriginalQuery()).isEqualTo(query);
        assertThat(result.getResponse()).contains("Error: Graph service error");
        assertThat(result.getTargetAgent()).isNull();
        assertThat(result.getRecommendedAgent()).isNull();
        assertThat(result.getConfidence()).isNull();
    }

    @Test
    @DisplayName("Should handle confidence exactly at threshold")
    public void shouldHandleConfidenceExactlyAtThreshold() throws Exception {
        // Given
        String query = "threshold query";
        String sessionId = "session123";
        String recommendedAgent = "threshold_agent";
        Integer confidence = 50; // Exactly at threshold
        String finalAnswer = "Threshold response";

        ResultOrientedState switchboardResponse = createMockSwitchboardResponse(recommendedAgent, confidence);
        ResultOrientedState finalResponse = createMockFinalResponse(finalAnswer);

        when(mockGraphService.processQuery("switchboard_agent", query, sessionId))
            .thenReturn(switchboardResponse);
        when(mockGraphService.processQuery(recommendedAgent, query, sessionId))
            .thenReturn(finalResponse);

        // When
        QueryResult result = queryProcessor.processQuery(query, sessionId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getTargetAgent()).isEqualTo(recommendedAgent); // Should use recommended agent at threshold
        assertThat(result.getConfidence()).isEqualTo(confidence);
    }

    @Test
    @DisplayName("Should handle null or empty final answer")
    public void shouldHandleNullOrEmptyFinalAnswer() throws Exception {
        // Given
        String query = "empty response query";
        String sessionId = "session123";
        String recommendedAgent = "empty_agent";
        Integer confidence = 75;

        ResultOrientedState switchboardResponse = createMockSwitchboardResponse(recommendedAgent, confidence);
        ResultOrientedState finalResponse = mock(ResultOrientedState.class);
        when(finalResponse.getFinalAnswer()).thenReturn(null);

        when(mockGraphService.processQuery("switchboard_agent", query, sessionId))
            .thenReturn(switchboardResponse);
        when(mockGraphService.processQuery(recommendedAgent, query, sessionId))
            .thenReturn(finalResponse);

        // When
        QueryResult result = queryProcessor.processQuery(query, sessionId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getResponse()).isNull();
    }

    private ResultOrientedState createMockSwitchboardResponse(String recommendedAgent, Integer confidence) {
        ResultOrientedState response = mock(ResultOrientedState.class);
        when(response.value(SwitchboardState.RECOMMENDED_AGENT_KEY))
            .thenReturn(Optional.of(recommendedAgent));
        when(response.value(SwitchboardState.CONFIDENCE_KEY))
            .thenReturn(Optional.of(confidence));
        return response;
    }

    private ResultOrientedState createMockFinalResponse(String finalAnswer) {
        ResultOrientedState response = mock(ResultOrientedState.class);
        when(response.getFinalAnswer()).thenReturn(finalAnswer);
        return response;
    }
}