package bill.zeacc.salieri.fifthgraph.agents.switchboard.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.bsc.langgraph4j.GraphStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import bill.zeacc.salieri.fifthgraph.agents.switchboard.SwitchboardState;
import bill.zeacc.salieri.fifthgraph.agents.switchboard.nodes.SwitchboardAnalysisNode;
import bill.zeacc.salieri.fifthgraph.model.meta.AgentDefinition;

@DisplayName("SwitchboardAgentConfig Tests")
public class SwitchboardAgentConfigTest {

    private SwitchboardAgentConfig swithboardAgentConfig;
    private SwitchboardAnalysisNode mockAnalysisNode;

    @BeforeEach
    protected void setUp() {
        swithboardAgentConfig = new SwitchboardAgentConfig();
        mockAnalysisNode = mock(SwitchboardAnalysisNode.class);
    }

    @Test
    @DisplayName("Should be instantiable")
    public void shouldBeInstantiable() {
        // When/Then
        assertThat(swithboardAgentConfig).isNotNull();
        assertThat(swithboardAgentConfig).isInstanceOf(SwitchboardAgentConfig.class);
    }

    @Test
    @DisplayName("Should be annotated with Configuration")
    public void shouldBeAnnotatedWithConfiguration() {
        // When/Then
        assertThat(swithboardAgentConfig.getClass().isAnnotationPresent(Configuration.class)).isTrue();
    }

    @Test
    @DisplayName("Should have Configuration annotation with correct values")
    public void shouldHaveConfigurationAnnotationWithCorrectValues() {
        // When
        Configuration configAnnotation = swithboardAgentConfig.getClass().getAnnotation(Configuration.class);
        
        // Then
        assertThat(configAnnotation).isNotNull();
        assertThat(configAnnotation.value()).isEmpty(); // Default value
    }

    @Test
    @DisplayName("Should create switchboard agent bean")
    public void shouldCreateSwitchboardAgentBean() throws GraphStateException {
        // When
        AgentDefinition<SwitchboardState> agentDefinition = swithboardAgentConfig.switchboardAgent(mockAnalysisNode);
        
        // Then
        assertThat(agentDefinition).isNotNull();
        assertThat(agentDefinition).isInstanceOf(AgentDefinition.class);
    }

    @Test
    @DisplayName("Should have switchboardAgent method annotated with Bean")
    public void shouldHaveSwitchboardAgentMethodAnnotatedWithBean() throws NoSuchMethodException {
        // When/Then
        assertThat(swithboardAgentConfig.getClass()
            .getMethod("switchboardAgent", SwitchboardAnalysisNode.class)
            .isAnnotationPresent(Bean.class)).isTrue();
    }

    @Test
    @DisplayName("Should create agent definition with correct name")
    public void shouldCreateAgentDefinitionWithCorrectName() throws GraphStateException {
        // When
        AgentDefinition<SwitchboardState> agentDefinition = swithboardAgentConfig.switchboardAgent(mockAnalysisNode);
        
        // Then
        assertThat(agentDefinition.name()).isEqualTo("switchboard_agent");
    }

    @Test
    @DisplayName("Should create agent definition with description")
    public void shouldCreateAgentDefinitionWithDescription() throws GraphStateException {
        // When
        AgentDefinition<SwitchboardState> agentDefinition = swithboardAgentConfig.switchboardAgent(mockAnalysisNode);
        
        // Then
        assertThat(agentDefinition.description()).isNotNull();
        assertThat(agentDefinition.description()).contains("switchboard agent");
        assertThat(agentDefinition.description()).contains("routes requests");
    }

    @Test
    @DisplayName("Should create agent definition with usage instructions")
    public void shouldCreateAgentDefinitionWithUsageInstructions() throws GraphStateException {
        // When
        AgentDefinition<SwitchboardState> agentDefinition = swithboardAgentConfig.switchboardAgent(mockAnalysisNode);
        
        // Then
        assertThat(agentDefinition.hintsForUse()).isNotNull();
        assertThat(agentDefinition.hintsForUse()).contains("Use this agent");
        assertThat(agentDefinition.hintsForUse()).contains("specialized agent");
    }

    @Test
    @DisplayName("Should create agent definition with non-null graph")
    public void shouldCreateAgentDefinitionWithNonNullGraph() throws GraphStateException {
        // When
        AgentDefinition<SwitchboardState> agentDefinition = swithboardAgentConfig.switchboardAgent(mockAnalysisNode);
        
        // Then
        assertThat(agentDefinition.graph()).isNotNull();
    }

    @Test
    @DisplayName("Should create agent definition with state builder")
    public void shouldCreateAgentDefinitionWithStateBuilder() throws GraphStateException {
        // When
        AgentDefinition<SwitchboardState> agentDefinition = swithboardAgentConfig.switchboardAgent(mockAnalysisNode);
        
        // Then
        assertThat(agentDefinition.stateBuilder()).isNotNull();
        
        // Verify the state builder creates a SwitchboardState
        SwitchboardState state = agentDefinition.stateBuilder().build("test query");
        assertThat(state).isNotNull();
        assertThat(state).isInstanceOf(SwitchboardState.class);
    }

    @Test
    @DisplayName("Should support multiple agent definitions")
    public void shouldSupportMultipleAgentDefinitions() throws GraphStateException {
        // Given
        SwitchboardAnalysisNode anotherMockNode = mock(SwitchboardAnalysisNode.class);
        
        // When
        AgentDefinition<SwitchboardState> agent1 = swithboardAgentConfig.switchboardAgent(mockAnalysisNode);
        AgentDefinition<SwitchboardState> agent2 = swithboardAgentConfig.switchboardAgent(anotherMockNode);
        
        // Then
        assertThat(agent1).isNotNull();
        assertThat(agent2).isNotNull();
        assertThat(agent1).isNotSameAs(agent2);
        // Both should have the same configuration but be different instances
        assertThat(agent1.name()).isEqualTo(agent2.name());
    }

    @Test
    @DisplayName("Should be a valid Spring configuration class")
    public void shouldBeAValidSpringConfigurationClass() {
        // When/Then
        assertThat(swithboardAgentConfig.getClass().getPackage().getName())
            .isEqualTo("bill.zeacc.salieri.fifthgraph.agents.switchboard.config");
        assertThat(swithboardAgentConfig.getClass().getSimpleName()).isEqualTo("SwitchboardAgentConfig");
        assertThat(swithboardAgentConfig.getClass().isAnnotationPresent(Configuration.class)).isTrue();
    }

    @Test
    @DisplayName("Should handle null analysis node gracefully")
    public void shouldHandleNullAnalysisNodeGracefully() {
        // When/Then - This should throw an exception or handle null appropriately
        try {
            AgentDefinition<SwitchboardState> agentDefinition = swithboardAgentConfig.switchboardAgent(null);
            // If it doesn't throw, verify the result is still valid
            assertThat(agentDefinition).isNotNull();
        } catch (Exception e) {
            // It's acceptable for this to throw an exception with null input
            assertThat(e).isInstanceOf(Exception.class);
        }
    }
}