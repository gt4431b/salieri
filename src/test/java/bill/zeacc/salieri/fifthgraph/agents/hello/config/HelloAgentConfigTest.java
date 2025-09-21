package bill.zeacc.salieri.fifthgraph.agents.hello.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

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
import bill.zeacc.salieri.fifthgraph.model.meta.ToolChooser;
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
}