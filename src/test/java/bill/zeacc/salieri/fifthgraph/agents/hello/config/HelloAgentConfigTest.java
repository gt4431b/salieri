package bill.zeacc.salieri.fifthgraph.agents.hello.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.bsc.langgraph4j.GraphStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;

import bill.zeacc.salieri.fifthgraph.agents.hello.GraphState;
import bill.zeacc.salieri.fifthgraph.model.meta.AgentDefinition;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolCall;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolChooser;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolResponse;
import bill.zeacc.salieri.fifthgraph.nodes.ResponseFormatterNode;
import bill.zeacc.salieri.fifthgraph.nodes.ToolAnalyzerNode;
import bill.zeacc.salieri.fifthgraph.nodes.ToolExecutorNode;

@ExtendWith(MockitoExtension.class)
@DisplayName("HelloAgentConfig Tests")
public class HelloAgentConfigTest {

    @Mock
    private ChatModel mockChatModel;

    @Mock
    private ToolChooser mockToolChooser;

    @Mock
    private ToolAnalyzerNode mockAnalyzerNode;

    @Mock
    private ToolExecutorNode mockToolExecutorNode;

    @Mock
    private ResponseFormatterNode mockResponseFormatterNode;

    private HelloAgentConfig helloAgentConfig;

    @BeforeEach
    void setUp() {
        helloAgentConfig = new HelloAgentConfig();
    }

    @Test
    @DisplayName("Should create ResponseFormatterNode bean with correct configuration")
    void shouldCreateResponseFormatterNodeBeanWithCorrectConfiguration() {
        // When
        ResponseFormatterNode node = helloAgentConfig.helloResponseFormatterNode(mockChatModel);

        // Then
        assertThat(node).isNotNull();
        assertThat(node).isInstanceOf(ResponseFormatterNode.class);
    }

    @Test
    @DisplayName("Should create ToolAnalyzerNode bean with correct dependencies")
    void shouldCreateToolAnalyzerNodeBeanWithCorrectDependencies() {
        // When
        ToolAnalyzerNode node = helloAgentConfig.helloToolAnalyzerNode(mockChatModel, mockToolChooser);

        // Then
        assertThat(node).isNotNull();
        assertThat(node).isInstanceOf(ToolAnalyzerNode.class);
    }

    @Test
    @DisplayName("Should create ToolExecutorNode bean with correct dependencies")
    void shouldCreateToolExecutorNodeBeanWithCorrectDependencies() {
        // When
    	when(mockToolChooser.get()).thenReturn(List.of());
        ToolExecutorNode node = helloAgentConfig.helloToolExecutorNode(mockToolChooser);

        // Then
        assertThat(node).isNotNull();
        assertThat(node).isInstanceOf(ToolExecutorNode.class);
    }

    @Test
    @DisplayName("Should create hello agent definition successfully")
    void shouldCreateHelloAgentDefinitionSuccessfully() throws GraphStateException {
        // When
        AgentDefinition<GraphState> agentDef = helloAgentConfig.helloAgent(
            mockAnalyzerNode, mockToolExecutorNode, mockResponseFormatterNode);

        // Then
        assertThat(agentDef).isNotNull();
        assertThat(agentDef.name()).isEqualTo("hello_agent");
        assertThat(agentDef.description()).isEqualTo("A simple hello world agent using tools");
        assertThat(agentDef.hintsForUse()).isEqualTo("Good for getting time, system information, or reading files.");
    }

    @Test
    @DisplayName("Should handle null chat model gracefully")
    void shouldHandleNullChatModelGracefully() {
        // When/Then - Should not throw exception during bean creation
        ResponseFormatterNode node = helloAgentConfig.helloResponseFormatterNode(null);
        assertThat(node).isNotNull();
    }

    @Test
    @DisplayName("Should handle null tool chooser gracefully")
    void shouldHandleNullToolChooserGracefully() {
        // When/Then - Should not throw exception during bean creation
        ToolAnalyzerNode analyzerNode = helloAgentConfig.helloToolAnalyzerNode(mockChatModel, null);
        ToolExecutorNode executorNode = helloAgentConfig.helloToolExecutorNode(null);

        assertThat(analyzerNode).isNotNull();
        assertThat(executorNode).isNotNull();
    }

    @Test
    @DisplayName("Should create agent with proper graph structure")
    void shouldCreateAgentWithProperGraphStructure() throws GraphStateException {
        // When
        AgentDefinition<GraphState> agentDef = helloAgentConfig.helloAgent(
            mockAnalyzerNode, mockToolExecutorNode, mockResponseFormatterNode);

        // Then
        assertThat(agentDef.graph()).isNotNull();
        assertThat(agentDef.stateBuilder()).isNotNull();
        
        // Test that state builder creates correct state type
        GraphState testState = agentDef.stateBuilder().build("test");
        assertThat(testState).isInstanceOf(GraphState.class);
    }

    @Test
    @DisplayName("Should handle agent creation with null nodes")
    void shouldHandleAgentCreationWithNullNodes() {
        // When/Then
        assertThatThrownBy(() -> helloAgentConfig.helloAgent(null, null, null))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Should create multiple instances of beans independently")
    void shouldCreateMultipleInstancesOfBeansIndependently() {
        // When
        ResponseFormatterNode node1 = helloAgentConfig.helloResponseFormatterNode(mockChatModel);
        ResponseFormatterNode node2 = helloAgentConfig.helloResponseFormatterNode(mockChatModel);

        // Then
        assertThat(node1).isNotNull();
        assertThat(node2).isNotNull();
        assertThat(node1).isNotSameAs(node2); // Should be different instances
    }

    @Test 
    @DisplayName("Should validate agent definition properties are correctly set")
    void shouldValidateAgentDefinitionProperties() throws GraphStateException {
        // When
        AgentDefinition<GraphState> agentDef = helloAgentConfig.helloAgent(
            mockAnalyzerNode, mockToolExecutorNode, mockResponseFormatterNode);

        // Then - Validate all properties
        assertThat(agentDef.name()).isEqualTo("hello_agent");
        assertThat(agentDef.description()).isEqualTo("A simple hello world agent using tools");
        assertThat(agentDef.hintsForUse()).isEqualTo("Good for getting time, system information, or reading files.");
        assertThat(agentDef.graph()).isNotNull();
        assertThat(agentDef.stateBuilder()).isNotNull();
        
        // Test state builder functionality
        GraphState testState = agentDef.stateBuilder().build("test query");
        assertThat(testState).isNotNull();
        assertThat(testState).isInstanceOf(GraphState.class);
    }

    @Test
    @DisplayName("Should handle partial null node scenarios for comprehensive error testing")
    void shouldHandlePartialNullNodeScenarios() {
        // Test various combinations of null nodes to ensure proper error handling
        
        // Scenario 1: Only analyzer is null
        assertThatThrownBy(() -> helloAgentConfig.helloAgent(null, mockToolExecutorNode, mockResponseFormatterNode))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("analyzerNode is null");
            
        // Scenario 2: Only tool executor is null  
        assertThatThrownBy(() -> helloAgentConfig.helloAgent(mockAnalyzerNode, null, mockResponseFormatterNode))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("analyzerNode is null");
            
        // Scenario 3: Only response formatter is null
        assertThatThrownBy(() -> helloAgentConfig.helloAgent(mockAnalyzerNode, mockToolExecutorNode, null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("analyzerNode is null");
    }

    // DIRECT ROUTING TESTS - These test the exposed routeOnTools method directly

    @Test
    @DisplayName("Should route to tool_executor when tool calls present and no results")
    void shouldRouteToToolExecutorWhenToolCallsPresentAndNoResults() throws Exception {
        // Given
        Map<String, Object> stateData = new HashMap<>();
        List<ToolCall> toolCalls = List.of(new ToolCall("tool1", "test_tool", "{}"));
        stateData.put("tool_calls", toolCalls);
        stateData.put("tool_results", List.of());
        GraphState state = new GraphState(stateData);
        
        // When
        CompletableFuture<String> result = helloAgentConfig.routeOnTools(state);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.get()).isEqualTo("tool_executor");
        assertThat(state.getToolCalls()).hasSize(1);
        assertThat(state.getToolResults()).isEmpty();
    }

    @Test
    @DisplayName("Should route to formatter when no tool calls")
    void shouldRouteToFormatterWhenNoToolCalls() throws Exception {
        // Given
        Map<String, Object> stateData = new HashMap<>();
        stateData.put("tool_calls", List.of());
        stateData.put("tool_results", List.of());
        GraphState state = new GraphState(stateData);
        
        // When
        CompletableFuture<String> result = helloAgentConfig.routeOnTools(state);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.get()).isEqualTo("formatter");
        assertThat(state.getToolCalls()).isEmpty();
        assertThat(state.getToolResults()).isEmpty();
    }

    @Test
    @DisplayName("Should route to formatter when tool calls and results both present")
    void shouldRouteToFormatterWhenToolCallsAndResultsPresent() throws Exception {
        // Given
        Map<String, Object> stateData = new HashMap<>();
        List<ToolCall> toolCalls = List.of(new ToolCall("tool1", "test_tool", "{}"));
        List<ToolResponse> toolResults = List.of(new ToolResponse("tool1", "test_tool", "result"));
        stateData.put("tool_calls", toolCalls);
        stateData.put("tool_results", toolResults);
        GraphState state = new GraphState(stateData);
        
        // When
        CompletableFuture<String> result = helloAgentConfig.routeOnTools(state);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.get()).isEqualTo("formatter");
        assertThat(state.getToolCalls()).hasSize(1);
        assertThat(state.getToolResults()).hasSize(1);
    }

    @Test
    @DisplayName("Should route to formatter when empty tool calls but results present")
    void shouldRouteToFormatterWhenEmptyToolCallsButResultsPresent() throws Exception {
        // Given
        Map<String, Object> stateData = new HashMap<>();
        stateData.put("tool_calls", List.of());
        List<ToolResponse> toolResults = List.of(new ToolResponse("tool1", "test_tool", "result"));
        stateData.put("tool_results", toolResults);
        GraphState state = new GraphState(stateData);
        
        // When
        CompletableFuture<String> result = helloAgentConfig.routeOnTools(state);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.get()).isEqualTo("formatter");
        assertThat(state.getToolCalls()).isEmpty();
        assertThat(state.getToolResults()).hasSize(1);
    }

    @Test
    @DisplayName("Should route to formatter with null/default state")
    void shouldRouteToFormatterWithNullDefaultState() throws Exception {
        // Given - default state with no tool calls or results
        GraphState state = new GraphState();
        
        // When
        CompletableFuture<String> result = helloAgentConfig.routeOnTools(state);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.get()).isEqualTo("formatter");
        assertThat(state.getToolCalls()).isEmpty(); // Should default to empty list
        assertThat(state.getToolResults()).isEmpty(); // Should default to empty list
    }

    @Test
    @DisplayName("Should route to tool_executor with multiple tool calls and no results")
    void shouldRouteToToolExecutorWithMultipleToolCallsAndNoResults() throws Exception {
        // Given
        Map<String, Object> stateData = new HashMap<>();
        List<ToolCall> multipleCalls = List.of(
            new ToolCall("tool1", "test_tool1", "{}"),
            new ToolCall("tool2", "test_tool2", "{}"),
            new ToolCall("tool3", "test_tool3", "{}")
        );
        stateData.put("tool_calls", multipleCalls);
        stateData.put("tool_results", List.of());
        GraphState state = new GraphState(stateData);
        
        // When
        CompletableFuture<String> result = helloAgentConfig.routeOnTools(state);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.get()).isEqualTo("tool_executor");
        assertThat(state.getToolCalls()).hasSize(3);
        assertThat(state.getToolResults()).isEmpty();
    }

    @Test
    @DisplayName("Should route to formatter with mismatched calls and results")
    void shouldRouteToFormatterWithMismatchedCallsAndResults() throws Exception {
        // Given - more tool calls than results (but results not empty)
        Map<String, Object> stateData = new HashMap<>();
        List<ToolCall> toolCalls = List.of(
            new ToolCall("tool1", "test_tool1", "{}"),
            new ToolCall("tool2", "test_tool2", "{}"),
            new ToolCall("tool3", "test_tool3", "{}")
        );
        List<ToolResponse> toolResults = List.of(
            new ToolResponse("tool1", "test_tool1", "result1")
        );
        stateData.put("tool_calls", toolCalls);
        stateData.put("tool_results", toolResults);
        GraphState state = new GraphState(stateData);
        
        // When
        CompletableFuture<String> result = helloAgentConfig.routeOnTools(state);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.get()).isEqualTo("formatter");
        assertThat(state.getToolCalls()).hasSize(3);
        assertThat(state.getToolResults()).hasSize(1);
    }
}
