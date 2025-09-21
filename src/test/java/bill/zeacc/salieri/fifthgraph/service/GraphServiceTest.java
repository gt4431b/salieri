package bill.zeacc.salieri.fifthgraph.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast ;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphInput ;
import org.bsc.async.AsyncGenerator ;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput ;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver;
import org.bsc.langgraph4j.state.AgentState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import bill.zeacc.salieri.fifthgraph.model.meta.AgentDefinition;
import bill.zeacc.salieri.fifthgraph.model.meta.AgentDescriptor;
import bill.zeacc.salieri.fifthgraph.model.states.ResultOrientedState;

@ExtendWith(MockitoExtension.class)
@DisplayName("GraphService Tests")
public class GraphServiceTest {

    @Mock
    private BaseCheckpointSaver mockCheckpointSaver;

    @Mock
    private AgentDefinition<TestAgentState> mockAgentDefinition;

    @Mock
    private StateGraph<TestAgentState> mockStateGraph;

    @Mock
    private CompiledGraph<TestAgentState> mockCompiledGraph;

    @Mock
    private TestAgentState mockResultState;

    private GraphService graphService;

    @BeforeEach
    public void setUp() throws GraphStateException {
        // Setup mock agent definition
        when(mockAgentDefinition.name()).thenReturn("test-agent");
        when(mockAgentDefinition.graph()).thenReturn(mockStateGraph);
/** /
        when(mockAgentDefinition.toDescriptor()).thenReturn(
            new AgentDescriptor("test-agent", "Test Agent", "A test agent for unit testing"));
/**/
        // Setup mock graph compilation
        when(mockStateGraph.compile(any(CompileConfig.class))).thenReturn(mockCompiledGraph);

        // Initialize service with mock dependencies
        List<AgentDefinition<? extends AgentState>> agentDefinitions = List.of(mockAgentDefinition);
        graphService = new GraphService(mockCheckpointSaver, agentDefinitions);
    }

    @Test
    @DisplayName("Should initialize with agent definitions successfully")
    public void shouldInitializeWithAgentDefinitionsSuccessfully() throws GraphStateException {
        // When/Then - Service should be initialized without exceptions
        assertThat(graphService).isNotNull();
        verify(mockAgentDefinition, atLeast ( 1 )).name();
        verify(mockAgentDefinition).graph();
        verify(mockStateGraph).compile(any(CompileConfig.class));
    }

    @Test
    @DisplayName("Should list agents correctly")
    public void shouldListAgentsCorrectly() {
        when(mockAgentDefinition.toDescriptor()).thenReturn(
                new AgentDescriptor("test-agent", "Test Agent", "A test agent for unit testing"));
        // When
        List<AgentDescriptor> agents = graphService.listAgents();

        // Then
        assertThat(agents).hasSize(1);
        assertThat(agents.get(0).name()).isEqualTo("test-agent");
        verify(mockAgentDefinition).toDescriptor();
    }

	@Test
    @DisplayName("Should process query with existing agent")
    public void shouldProcessQueryWithExistingAgent() {
        // Given
        String agentName = "test-agent";
        String query = "Test query";
        String sessionId = "test-session";

        TestAgentState mockResultState = mock(TestAgentState.class);
        when(mockCompiledGraph.stream(any(GraphInput.class), any())).thenReturn(new AsyncGenerator <NodeOutput<TestAgentState>> ( ) {

        	private List <NodeOutput <TestAgentState>> r = List.of(
        			new NodeOutput<TestAgentState>("node-1", mockResultState)
        		) ;
        	private int index = 0 ;
			@Override
			public Data <NodeOutput <TestAgentState>> next ( ) {
				return index < r.size ( ) ? Data.of ( r.get ( index++ ) ) : Data.done ( ) ;
			} } );

        // When
        ResultOrientedState result = graphService.processQuery(agentName, query, sessionId);

        // Then
        assertThat(result).isNotNull();
        verify(mockCompiledGraph).stream(any(GraphInput.class), any());
    }

    @Test
    @DisplayName("Should throw exception for unknown agent")
    public void shouldThrowExceptionForUnknownAgent() {
        // Given
        String unknownAgentName = "unknown-agent";
        String query = "Test query";
        String sessionId = "test-session";

        // When/Then
        assertThatThrownBy(() -> graphService.processQuery(unknownAgentName, query, sessionId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No agent found with name: unknown-agent");
    }

    @Test
    @DisplayName("Should handle empty agent definitions list")
    public void shouldHandleEmptyAgentDefinitionsList() throws GraphStateException {
        // Given
        List<AgentDefinition<? extends AgentState>> emptyList = List.of();

        // When
        GraphService emptyService = new GraphService(mockCheckpointSaver, emptyList);
        List<AgentDescriptor> agents = emptyService.listAgents();

        // Then
        assertThat(agents).isEmpty();
    }

    @SuppressWarnings ( "unchecked" )
	@Test
    @DisplayName("Should handle multiple agent definitions")
    public void shouldHandleMultipleAgentDefinitions() throws GraphStateException {
        // Given
        AgentDefinition<TestAgentState> mockAgentDef2 = mock(AgentDefinition.class);
        StateGraph<TestAgentState> mockGraph2 = mock(StateGraph.class);
        CompiledGraph<TestAgentState> mockCompiled2 = mock(CompiledGraph.class);

        when(mockAgentDefinition.toDescriptor()).thenReturn(
                new AgentDescriptor("test-agent", "Test Agent", "A test agent for unit testing"));
        when(mockAgentDef2.name()).thenReturn("agent-2");
        when(mockAgentDef2.graph()).thenReturn(mockGraph2);
        when(mockAgentDef2.toDescriptor()).thenReturn(
            new AgentDescriptor("agent-2", "Agent 2", "Second test agent"));
        when(mockGraph2.compile(any(CompileConfig.class))).thenReturn(mockCompiled2);

        List<AgentDefinition<? extends AgentState>> multipleAgents = List.of(mockAgentDefinition, mockAgentDef2);

        // When
        GraphService multiAgentService = new GraphService(mockCheckpointSaver, multipleAgents);
        List<AgentDescriptor> agents = multiAgentService.listAgents();

        // Then
        assertThat(agents).hasSize(2);
        assertThat(agents).extracting(AgentDescriptor::name)
                         .containsExactlyInAnyOrder("test-agent", "agent-2");
    }

    @SuppressWarnings ( "unchecked" )
	@Test
    @DisplayName("Should handle graph compilation failure")
    public void shouldHandleGraphCompilationFailure() throws GraphStateException {
        // Given
        AgentDefinition<TestAgentState> failingAgentDef = mock(AgentDefinition.class);
        StateGraph<TestAgentState> failingGraph = mock(StateGraph.class);

        when(failingAgentDef.name()).thenReturn("failing-agent");
        when(failingAgentDef.graph()).thenReturn(failingGraph);
        when(failingGraph.compile(any(CompileConfig.class)))
            .thenThrow(new GraphStateException("Compilation failed"));

        List<AgentDefinition<? extends AgentState>> agentDefs = List.of(failingAgentDef);

        // When/Then
        assertThatThrownBy(() -> new GraphService(mockCheckpointSaver, agentDefs))
            .isInstanceOf(GraphStateException.class)
            .hasMessage("Compilation failed");
    }

    // Simple test state class
    private static class TestAgentState extends ResultOrientedState {
        @SuppressWarnings ( "unused" )
		public TestAgentState() {
            super(Map.of());
        }
    }
}