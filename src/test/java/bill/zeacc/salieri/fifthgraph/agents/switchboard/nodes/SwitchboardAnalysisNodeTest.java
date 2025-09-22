package bill.zeacc.salieri.fifthgraph.agents.switchboard.nodes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage ;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import bill.zeacc.salieri.fifthgraph.agents.switchboard.SwitchboardState;
import bill.zeacc.salieri.fifthgraph.model.meta.AgentDescriptor;
import bill.zeacc.salieri.fifthgraph.service.GraphService;

@ExtendWith(MockitoExtension.class)
public class SwitchboardAnalysisNodeTest {

    private SwitchboardAnalysisNode analysisNode;
    
    @Mock
    private ChatModel chatModel;
    
    @Mock
    private ApplicationContext applicationContext;
    
    @Mock
    private GraphService graphService;
    
    @Mock
    private SwitchboardState state;
    
    @Mock
    private ChatResponse chatResponse;
    
    @Mock
    private Generation generation;
    
    @BeforeEach
    public void setUp() {
        analysisNode = new SwitchboardAnalysisNode();
        analysisNode.setChatModel ( chatModel );
        analysisNode.setObjectMapper(new ObjectMapper());
        analysisNode.setApplicationContext(applicationContext);
    }

    @Test
    public void testApplyWithValidResponse() throws Exception {
        // Arrange
        String query = "What is the weather today?";
        String jsonResponse = "{\"recommendedAgent\": \"weather_agent\", \"confidence\": 85}";

        when(state.getQuery()).thenReturn(query);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        AssistantMessage output = mock(AssistantMessage.class);
        when(generation.getOutput()).thenReturn(output);
        when(output.getText()).thenReturn(jsonResponse);
        when(applicationContext.getBean(GraphService.class)).thenReturn(graphService);
        when(graphService.listAgents()).thenReturn(createMockAgents());
        
        // Act
        Map<String, Object> resultMap = analysisNode.apply(state);
        
        // Assert
        assertNotNull(resultMap);
        assertEquals("weather_agent", resultMap.get(SwitchboardState.RECOMMENDED_AGENT_KEY));
        assertEquals(85, resultMap.get(SwitchboardState.CONFIDENCE_KEY));

        // Re-act
        resultMap = analysisNode.apply(state);
        assertNotNull(resultMap);
        assertEquals("weather_agent", resultMap.get(SwitchboardState.RECOMMENDED_AGENT_KEY));
        assertEquals(85, resultMap.get(SwitchboardState.CONFIDENCE_KEY));
    }

	@Test
    public void testApplyWithInvalidJsonResponse() throws Exception {
        // Arrange
        String query = "Hello there";
        String invalidJsonResponse = "This is not valid JSON";
        
        when(state.getQuery()).thenReturn(query);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        AssistantMessage output = mock(AssistantMessage.class);
        when(generation.getOutput()).thenReturn(output);
        when(output.getText()).thenReturn(invalidJsonResponse);
        when(applicationContext.getBean(GraphService.class)).thenReturn(graphService);
        when(graphService.listAgents()).thenReturn(createMockAgents());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            analysisNode.apply(state);
        });
        
        assertTrue(exception.getMessage().contains("Failed to parse analysis response"));
    }

    @Test
    public void testApplyWithDifferentAgent() throws Exception {
        // Arrange
        String query = "Tell me a joke";
        String jsonResponse = "{\"recommendedAgent\": \"default_agent\", \"confidence\": 60}";

        when(state.getQuery()).thenReturn(query);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        AssistantMessage output = mock(AssistantMessage.class);
        when(generation.getOutput()).thenReturn(output);
        when(output.getText()).thenReturn(jsonResponse);
        when(applicationContext.getBean(GraphService.class)).thenReturn(graphService);
        when(graphService.listAgents()).thenReturn(createMockAgents());
        
        // Act
        Map<String, Object> resultMap = analysisNode.apply(state);
        
        // Assert
        assertNotNull(resultMap);
        assertEquals("default_agent", resultMap.get(SwitchboardState.RECOMMENDED_AGENT_KEY));
        assertEquals(60, resultMap.get(SwitchboardState.CONFIDENCE_KEY));
    }

    @Test
    public void testApplyWithHighConfidence() throws Exception {
        // Arrange
        String query = "What's 2 + 2?";
        String jsonResponse = "{\"recommendedAgent\": \"math_agent\", \"confidence\": 95}";
        
        when(state.getQuery()).thenReturn(query);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        AssistantMessage output = mock(AssistantMessage.class);
        when(generation.getOutput()).thenReturn(output);
        when(output.getText()).thenReturn(jsonResponse);
        when(applicationContext.getBean(GraphService.class)).thenReturn(graphService);
        when(graphService.listAgents()).thenReturn(createMockAgents());
        
        // Act
        Map<String, Object> resultMap = analysisNode.apply(state);
        
        // Assert
        assertNotNull(resultMap);
        assertEquals("math_agent", resultMap.get(SwitchboardState.RECOMMENDED_AGENT_KEY));
        assertEquals(95, resultMap.get(SwitchboardState.CONFIDENCE_KEY));
    }

    @Test
    public void testApplyWithLowConfidence() throws Exception {
        // Arrange
        String query = "Ambiguous query";
        String jsonResponse = "{\"recommendedAgent\": \"default_agent\", \"confidence\": 20}";
        
        when(state.getQuery()).thenReturn(query);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        AssistantMessage output = mock(AssistantMessage.class);
		when(generation.getOutput()).thenReturn(output);
        when(output.getText()).thenReturn(jsonResponse);
        when(applicationContext.getBean(GraphService.class)).thenReturn(graphService);
        when(graphService.listAgents()).thenReturn(createMockAgents());
        
        // Act
        Map<String, Object> resultMap = analysisNode.apply(state);
        
        // Assert
        assertNotNull(resultMap);
        assertEquals("default_agent", resultMap.get(SwitchboardState.RECOMMENDED_AGENT_KEY));
        assertEquals(20, resultMap.get(SwitchboardState.CONFIDENCE_KEY));
    }

    @Test
    public void testSetApplicationContext() {
        // Act
        analysisNode.setApplicationContext(applicationContext);
        
        // Assert - no exception should be thrown
        // The application context should be set for later use
        assertDoesNotThrow(() -> analysisNode.setApplicationContext(applicationContext));
    }

    @Test
    public void testSystemPromptBuilding() throws Exception {
        // Arrange
        when(applicationContext.getBean(GraphService.class)).thenReturn(graphService);
        when(graphService.listAgents()).thenReturn(createMockAgents());
        
        String query = "Test query";
        String jsonResponse = "{\"recommendedAgent\": \"test_agent\", \"confidence\": 75}";
        
        when(state.getQuery()).thenReturn(query);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        AssistantMessage output = mock(AssistantMessage.class);
        when(generation.getOutput()).thenReturn(output);
        when(output.getText()).thenReturn(jsonResponse);
        
        // Act
        Map<String, Object> resultMap = analysisNode.apply(state);
        
        // Assert
        assertNotNull(resultMap);
        verify(graphService).listAgents();
    }

    @Test
    public void testSystemPromptBuildingFailure() throws Exception {
        // Arrange
        when(applicationContext.getBean(GraphService.class)).thenReturn(graphService);
        when(graphService.listAgents()).thenReturn(createMockAgents());
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("Failed to build system prompt"));

        String query = "Test query";
        when(state.getQuery()).thenReturn(query);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            analysisNode.apply(state);
        });
        
        assertTrue(exception.getMessage().contains("Failed to build system prompt"));
    }

    @Test
    public void testApplyWithNullQuery() throws Exception {
        // Arrange
        when(state.getQuery()).thenReturn(null);
        when(applicationContext.getBean(GraphService.class)).thenReturn(graphService);
        when(graphService.listAgents()).thenReturn(createMockAgents());
        
        // Act & Assert - should handle null query gracefully or throw appropriate exception
        assertThrows(Exception.class, () -> {
            analysisNode.apply(state);
        });
    }

    @Test
    public void testApplyWithEmptyQuery() throws Exception {
        // Arrange
        String query = "";
        String jsonResponse = "{\"recommendedAgent\": \"default_agent\", \"confidence\": 10}";
        
        when(state.getQuery()).thenReturn(query);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        AssistantMessage output = mock(AssistantMessage.class);
        when(generation.getOutput()).thenReturn(output);
        when(output.getText()).thenReturn(jsonResponse);
        when(applicationContext.getBean(GraphService.class)).thenReturn(graphService);
        when(graphService.listAgents()).thenReturn(createMockAgents());
        
        // Act
        Map<String, Object> resultMap = analysisNode.apply(state);
        
        // Assert
        assertNotNull(resultMap);
        assertEquals("default_agent", resultMap.get(SwitchboardState.RECOMMENDED_AGENT_KEY));
        assertEquals(10, resultMap.get(SwitchboardState.CONFIDENCE_KEY));
    }

    private List<AgentDescriptor> createMockAgents() {
        AgentDescriptor agent1 = new AgentDescriptor("weather_agent", "Handles weather queries", "Use for weather-related questions");
        AgentDescriptor agent2 = new AgentDescriptor("math_agent", "Handles math calculations", "Use for mathematical problems");
        AgentDescriptor agent3 = new AgentDescriptor("default_agent", "Default chat agent", "Use for general conversations");
        return Arrays.asList(agent1, agent2, agent3);
    }
}
