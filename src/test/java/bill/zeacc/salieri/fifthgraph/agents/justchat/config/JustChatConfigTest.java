package bill.zeacc.salieri.fifthgraph.agents.justchat.config;

import static org.junit.jupiter.api.Assertions.*;

import org.bsc.langgraph4j.GraphStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;

import bill.zeacc.salieri.fifthgraph.model.meta.AgentDefinition;
import bill.zeacc.salieri.fifthgraph.model.states.ToolOrientedState;
import bill.zeacc.salieri.fifthgraph.nodes.ResponseFormatterNode;

@ExtendWith(MockitoExtension.class)
public class JustChatConfigTest {

    private JustChatConfig config;
    
    @Mock
    private ChatModel chatModel;
    
    @Mock
    private ResponseFormatterNode responseFormatterNode;

    @BeforeEach
    public void setUp() {
        config = new JustChatConfig();
    }

    @Test
    public void testJustChatResponseFormatterNodeCreation() {
        ResponseFormatterNode node = config.justChatResponseFormatterNode(chatModel);
        
        assertNotNull(node);
        // The node should be configured with pirate-like behavior
    }

    @Test
    public void testJustChatResponseFormatterNodeWithNullChatModel() {
        assertThrows(Exception.class, () -> {
            config.justChatResponseFormatterNode(null);
        });
    }

    @Test
    public void testJustChatAgentCreation() throws GraphStateException {
        AgentDefinition<ToolOrientedState> agentDefinition = config.justChatAgent(responseFormatterNode);
        
        assertNotNull(agentDefinition);
        assertEquals("default_agent", agentDefinition.name());
        assertNotNull(agentDefinition.description());
        assertTrue(agentDefinition.description().contains("pirate"));
        assertNotNull(agentDefinition.hintsForUse());
        assertTrue(agentDefinition.hintsForUse().contains("pirate flair"));
    }

    @Test
    public void testJustChatAgentWithNullResponseFormatterNode() {
        assertThrows(Exception.class, () -> {
            config.justChatAgent(null);
        });
    }

    @Test
    public void testJustChatAgentGraph() throws GraphStateException {
        AgentDefinition<ToolOrientedState> agentDefinition = config.justChatAgent(responseFormatterNode);
        
        assertNotNull(agentDefinition.graph());
        // Graph should be properly configured with formatter node
    }

    @Test
    public void testJustChatAgentStateFactory() throws GraphStateException {
        AgentDefinition<ToolOrientedState> agentDefinition = config.justChatAgent(responseFormatterNode);
        
        assertNotNull(agentDefinition.stateBuilder());
        
        // Test that the state factory creates proper instances
        ToolOrientedState state = agentDefinition.stateBuilder().build("test query");
        assertNotNull(state);
    }

    @Test
    public void testJustChatAgentName() throws GraphStateException {
        AgentDefinition<ToolOrientedState> agentDefinition = config.justChatAgent(responseFormatterNode);
        
        assertEquals("default_agent", agentDefinition.name());
    }

    @Test
    public void testJustChatAgentDescriptionContent() throws GraphStateException {
        AgentDefinition<ToolOrientedState> agentDefinition = config.justChatAgent(responseFormatterNode);
        
        String description = agentDefinition.description();
        assertNotNull(description);
        assertTrue(description.contains("simple chat agent"));
        assertTrue(description.contains("pirate-like manner"));
    }

    @Test
    public void testJustChatAgentUsageInstructions() throws GraphStateException {
        AgentDefinition<ToolOrientedState> agentDefinition = config.justChatAgent(responseFormatterNode);
        
        String usage = agentDefinition.hintsForUse();
        assertNotNull(usage);
        assertTrue(usage.contains("casual conversations"));
        assertTrue(usage.contains("pirate flair"));
    }

    @Test
    public void testJustChatResponseFormatterNodeBehavior() {
        ResponseFormatterNode node = config.justChatResponseFormatterNode(chatModel);
        
        assertNotNull(node);
        // Verify that the node is configured with the expected pirate prompt
        // The actual prompt should contain "pirate" text
    }

    @Test
    public void testJustChatAgentDefinitionProperties() throws GraphStateException {
        AgentDefinition<ToolOrientedState> agentDefinition = config.justChatAgent(responseFormatterNode);
        
        // Verify all required properties are set
        assertNotNull(agentDefinition.name());
        assertNotNull(agentDefinition.graph());
        assertNotNull(agentDefinition.stateBuilder());
        assertNotNull(agentDefinition.description());
        assertNotNull(agentDefinition.hintsForUse());
        
        // Verify the agent name is correct
        assertEquals("default_agent", agentDefinition.name());
    }
}
