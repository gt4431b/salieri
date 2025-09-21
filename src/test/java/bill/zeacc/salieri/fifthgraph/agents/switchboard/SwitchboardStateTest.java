package bill.zeacc.salieri.fifthgraph.agents.switchboard;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.bsc.langgraph4j.state.AgentState ;
import org.bsc.langgraph4j.state.Channel;
import org.junit.jupiter.api.Test;

import bill.zeacc.salieri.fifthgraph.model.states.ToolOrientedState;

class SwitchboardStateTest {

    @Test
    void testDefaultConstructor() {
        SwitchboardState state = new SwitchboardState();
        
        assertNotNull(state);
        assertNotNull(state.data());
    }

    @Test
    void testConstructorWithInitData() {
        Map<String, Object> initData = new HashMap<>();
        initData.put("test_key", "test_value");
        initData.put(SwitchboardState.RECOMMENDED_AGENT_KEY, "hello_agent");
        initData.put(SwitchboardState.CONFIDENCE_KEY, 85);
        
        SwitchboardState state = new SwitchboardState(initData);
        
        assertNotNull(state);
        assertEquals("test_value", state.data().get("test_key"));
        assertEquals("hello_agent", state.data().get(SwitchboardState.RECOMMENDED_AGENT_KEY));
        assertEquals(85, state.data().get(SwitchboardState.CONFIDENCE_KEY));
    }

    @Test
    void testSchemaConstants() {
        assertEquals("recommended_agent", SwitchboardState.RECOMMENDED_AGENT_KEY);
        assertEquals("confidence", SwitchboardState.CONFIDENCE_KEY);
    }

    @Test
    void testSchemaContainsExpectedKeys() {
        Map<String, Channel<?>> schema = SwitchboardState.SCHEMA;
        
        assertNotNull(schema);
        assertTrue(schema.containsKey(SwitchboardState.RECOMMENDED_AGENT_KEY));
        assertTrue(schema.containsKey(SwitchboardState.CONFIDENCE_KEY));
        
        // Should also contain all keys from ToolOrientedState.SCHEMA
        for (String key : ToolOrientedState.SCHEMA.keySet()) {
            assertTrue(schema.containsKey(key), "Schema should contain key from ToolOrientedState: " + key);
        }
    }

    @Test
    void testSchemaChannels() {
        Map<String, Channel<?>> schema = SwitchboardState.SCHEMA;
        
        Channel<?> recommendedAgentChannel = schema.get(SwitchboardState.RECOMMENDED_AGENT_KEY);
        Channel<?> confidenceChannel = schema.get(SwitchboardState.CONFIDENCE_KEY);
        
        assertNotNull(recommendedAgentChannel);
        assertNotNull(confidenceChannel);
    }

    @Test
    void testInheritanceFromResultOrientedState() {
        SwitchboardState state = new SwitchboardState();

        // Should inherit all methods from ResultOrientedState
        assertNotNull(state.data());

        // Test setting and getting values using inherited methods
        Map <String, Object> replaceData = new HashMap<>();
        replaceData.put("test_key", "test_value");
        state = new SwitchboardState ( AgentState.updateState(state, replaceData, SwitchboardState.SCHEMA) ) ;
        assertEquals("test_value", state.value("test_key").orElse(null));
    }

    @Test
    public void testSetAndGetRecommendedAgent() {
        SwitchboardState state = new SwitchboardState();

        Map <String, Object> replaceData = new HashMap<>();
        replaceData.put(SwitchboardState.RECOMMENDED_AGENT_KEY, "hello_agent");
        state = new SwitchboardState ( AgentState.updateState(state, replaceData, SwitchboardState.SCHEMA) ) ;
        assertEquals("hello_agent", state.value(SwitchboardState.RECOMMENDED_AGENT_KEY).orElse(null));
    }

    @Test
    public void testSetAndGetConfidence() {
        SwitchboardState state = new SwitchboardState();

        Map <String, Object> replaceData = new HashMap<>();
        replaceData.put(SwitchboardState.CONFIDENCE_KEY, 75);
        state = new SwitchboardState ( AgentState.updateState(state, replaceData, SwitchboardState.SCHEMA) ) ;
        assertEquals(75, state.value(SwitchboardState.CONFIDENCE_KEY).orElse(null));
    }

    @Test
    public void testStateWithMultipleValues() throws Exception {
        SwitchboardState state = new SwitchboardState();

        Map <String, Object> replaceData = new HashMap<>();
        replaceData.put(SwitchboardState.RECOMMENDED_AGENT_KEY, "switchboard_agent");
        replaceData.put(SwitchboardState.CONFIDENCE_KEY, 95);
        replaceData.put("query", "What is the weather?");
        replaceData.put("session_id", "123456");
        state = new SwitchboardState ( AgentState.updateState(state, replaceData, SwitchboardState.SCHEMA) ) ;

        assertEquals("switchboard_agent", state.value(SwitchboardState.RECOMMENDED_AGENT_KEY).orElse(null));
        assertEquals(95, state.value(SwitchboardState.CONFIDENCE_KEY).orElse(null));
        assertEquals("What is the weather?", state.value("query").orElse(null));
        assertEquals("123456", state.value("session_id").orElse(null));
    }

    @Test
    void testConstructorWithEmptyInitData() {
        Map<String, Object> emptyData = new HashMap<>();
        SwitchboardState state = new SwitchboardState(emptyData);
        
        assertNotNull(state);
        assertNotNull(state.data());
    }
}