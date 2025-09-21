package bill.zeacc.salieri.fifthgraph.model.states;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.bsc.langgraph4j.state.AgentState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import bill.zeacc.salieri.fifthgraph.model.codeir.Codebase;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolCall;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("EngineeringState Tests")
public class EngineeringStateTest {

    private EngineeringState engineeringState;

    @BeforeEach
    void setUp() {
        engineeringState = new EngineeringState();
    }

    @Test
    @DisplayName("Should initialize with default constructor")
    void shouldInitializeWithDefaultConstructor() {
        // Given
        EngineeringState state = new EngineeringState();

        // Then
        assertThat(state.getCodebase()).isNotNull();
        assertThat(state.getSandbox()).isEmpty();
        assertThat(state.getToolCalls()).isEmpty();
        assertThat(state.getToolResults()).isEmpty();
        assertThat(state.getQuery()).isEmpty();
        assertThat(state.getMessages()).isEmpty();
    }

    @Test
    @DisplayName("Should initialize with init data")
    void shouldInitializeWithInitData() {
        // Given
        Codebase expectedCodebase = new Codebase();
        expectedCodebase.setSandboxRootPath("/test/sandbox");
        
        Map<String, String> expectedSandbox = Map.of(
            "file1.txt", "content1",
            "file2.txt", "content2"
        );

        Map<String, Object> initData = new HashMap<>();
        initData.put(EngineeringState.CODEBASE_KEY, expectedCodebase);
        initData.put(EngineeringState.SANDBOX_KEY, expectedSandbox);
        initData.put(ResultOrientedState.QUERY_KEY, "engineering query");

        // When
        EngineeringState state = new EngineeringState(initData);

        // Then
        assertThat(state.getCodebase()).isEqualTo(expectedCodebase);
        assertThat(state.getSandbox()).isEqualTo(expectedSandbox);
        assertThat(state.getQuery()).isEqualTo("engineering query");
    }

    @Test
    @DisplayName("Should have correct schema keys including inherited ones")
    void shouldHaveCorrectSchemaKeysIncludingInheritedOnes() {
        // Then
        assertThat(EngineeringState.SCHEMA).containsKeys(
            EngineeringState.CODEBASE_KEY,
            EngineeringState.SANDBOX_KEY,
            // Inherited from ResultOrientedState
            ResultOrientedState.QUERY_KEY,
            ResultOrientedState.FINAL_ANSWER_KEY,
            ResultOrientedState.MESSAGES_KEY
        );
        
        assertThat(EngineeringState.CODEBASE_KEY).isEqualTo("codebase");
        assertThat(EngineeringState.SANDBOX_KEY).isEqualTo("sandbox");
    }

    @Test
    @DisplayName("Should inherit from ToolOrientedState")
    void shouldInheritFromToolOrientedState() {
        // Then
        assertThat(engineeringState).isInstanceOf(ToolOrientedState.class);
        assertThat(engineeringState).isInstanceOf(ResultOrientedState.class);
    }

    @Test
    @DisplayName("Should return default codebase when not set")
    void shouldReturnDefaultCodebaseWhenNotSet() {
        // When
        Codebase codebase = engineeringState.getCodebase();

        // Then
        assertThat(codebase).isNotNull();
        assertThat(codebase).isInstanceOf(Codebase.class);
    }

    @Test
    @DisplayName("Should return empty sandbox when not set")
    void shouldReturnEmptySandboxWhenNotSet() {
        // When
        Map<String, String> sandbox = engineeringState.getSandbox();

        // Then
        assertThat(sandbox).isNotNull();
        assertThat(sandbox).isEmpty();
    }

    @Test
    @DisplayName("Should handle null codebase gracefully")
    void shouldHandleNullCodebaseGracefully() {
        // Given
        Map<String, Object> initData = new HashMap<>();
        initData.put(EngineeringState.CODEBASE_KEY, null);

        // When
        EngineeringState state = new EngineeringState(initData);

        // Then
        assertThat(state.getCodebase()).isNotNull();
        assertThat(state.getCodebase()).isInstanceOf(Codebase.class);
    }

    @Test
    @DisplayName("Should handle null sandbox gracefully")
    void shouldHandleNullSandboxGracefully() {
        // Given
        Map<String, Object> initData = new HashMap<>();
        initData.put(EngineeringState.SANDBOX_KEY, null);

        // When
        EngineeringState state = new EngineeringState(initData);

        // Then
        assertThat(state.getSandbox()).isNotNull();
        assertThat(state.getSandbox()).isEmpty();
    }

    @Test
    @DisplayName("Should update codebase correctly")
    void shouldUpdateCodebaseCorrectly() {
        // Given
        Codebase newCodebase = new Codebase();
        newCodebase.setSandboxRootPath("/updated/sandbox");
        
        Map<String, Object> updateData = new HashMap<>();
        updateData.put(EngineeringState.CODEBASE_KEY, newCodebase);

        // When
        EngineeringState newState = new EngineeringState ( AgentState.updateState(engineeringState, updateData, EngineeringState.SCHEMA) ) ;

        // Then
        assertThat(newState.getCodebase()).isEqualTo(newCodebase);
        assertThat(newState.getCodebase().getSandboxRootPath()).isEqualTo("/updated/sandbox");
    }

    @Test
    @DisplayName("Should update sandbox correctly")
    void shouldUpdateSandboxCorrectly() {
        // Given
        Map<String, String> newSandbox = Map.of(
            "config.json", "{}",
            "script.py", "print('hello')"
        );
        
        Map<String, Object> updateData = new HashMap<>();
        updateData.put(EngineeringState.SANDBOX_KEY, newSandbox);

        // When
        EngineeringState newState = new EngineeringState ( AgentState.updateState(engineeringState, updateData, EngineeringState.SCHEMA) ) ;

        // Then
        assertThat(newState.getSandbox()).isEqualTo(newSandbox);
        assertThat(newState.getSandbox()).containsKeys("config.json", "script.py");
    }

    @Test
    @DisplayName("Should maintain tool-oriented functionality")
    void shouldMaintainToolOrientedFunctionality() {
        // Given
        ToolCall toolCall = new ToolCall("call1", "test_tool", "{}");
        ToolResponse toolResponse = new ToolResponse("call1", "test_tool", "success");
        
        Map<String, Object> updateData = new HashMap<>();
        updateData.put(ToolOrientedState.TOOL_CALLS_KEY, java.util.List.of(toolCall));
        updateData.put(ToolOrientedState.TOOL_RESULTS_KEY, java.util.List.of(toolResponse));

        // When
        EngineeringState newState = new EngineeringState ( AgentState.updateState(engineeringState, updateData, EngineeringState.SCHEMA) ) ;

        // Then
        assertThat(newState.getToolCalls()).hasSize(1);
        assertThat(newState.getToolResults()).hasSize(1);
        assertThat(newState.getToolCalls().get(0)).isEqualTo(toolCall);
        assertThat(newState.getToolResults().get(0)).isEqualTo(toolResponse);
    }

    @Test
    @DisplayName("Should preserve all state data across operations")
    void shouldPreserveAllStateDataAcrossOperations() {
        // Given - Initial state with all types of data
        Codebase initialCodebase = new Codebase();
        initialCodebase.setSandboxRootPath("/initial");
        
        Map<String, String> initialSandbox = Map.of("initial.txt", "content");
        ToolCall initialCall = new ToolCall("call1", "tool1", "{}");
        
        Map<String, Object> initData = new HashMap<>();
        initData.put(EngineeringState.CODEBASE_KEY, initialCodebase);
        initData.put(EngineeringState.SANDBOX_KEY, initialSandbox);
        initData.put(ToolOrientedState.TOOL_CALLS_KEY, java.util.List.of(initialCall));
        initData.put(ResultOrientedState.QUERY_KEY, "initial query");
        
        EngineeringState state = new EngineeringState(initData);

        // When - Update only some fields
        Map<String, String> newSandbox = Map.of("updated.txt", "new content");
        Map<String, Object> updateData = new HashMap<>();
        updateData.put(EngineeringState.SANDBOX_KEY, newSandbox);
        updateData.put(ResultOrientedState.FINAL_ANSWER_KEY, "final answer");
        
        EngineeringState newState = new EngineeringState ( AgentState.updateState(state, updateData, EngineeringState.SCHEMA) ) ;

        // Then - Updated fields changed, others preserved
        assertThat(newState.getCodebase()).isEqualTo(initialCodebase); // Preserved
        assertThat(newState.getSandbox()).isEqualTo(newSandbox); // Updated
        assertThat(newState.getToolCalls()).hasSize(1); // Preserved
        assertThat(newState.getToolCalls().get(0)).isEqualTo(initialCall); // Preserved
        assertThat(newState.getQuery()).isEqualTo("initial query"); // Preserved
        assertThat(newState.getFinalAnswer()).isEqualTo("final answer"); // Updated
    }

    @Test
    @DisplayName("Should handle complex codebase operations")
    void shouldHandleComplexCodebaseOperations() {
        // Given
        Codebase complexCodebase = new Codebase();
        complexCodebase.setSandboxRootPath("/complex/project");
        
        Map<String, Object> initData = new HashMap<>();
        initData.put(EngineeringState.CODEBASE_KEY, complexCodebase);

        EngineeringState state = new EngineeringState(initData);

        // When
        Codebase retrievedCodebase = state.getCodebase();

        // Then
        assertThat(retrievedCodebase).isNotNull();
        assertThat(retrievedCodebase.getSandboxRootPath()).isEqualTo("/complex/project");
    }

    @Test
    @DisplayName("Should handle empty and populated sandbox scenarios")
    void shouldHandleEmptyAndPopulatedSandboxScenarios() {
        // Given - Empty sandbox
        Map<String, Object> emptyData = new HashMap<>();
        emptyData.put(EngineeringState.SANDBOX_KEY, new HashMap<String, String>());
        
        EngineeringState emptyState = new EngineeringState(emptyData);

        // Then - Empty sandbox
        assertThat(emptyState.getSandbox()).isEmpty();

        // Given - Populated sandbox
        Map<String, String> populatedSandbox = Map.of(
            "README.md", "# Project",
            "src/main.py", "def main(): pass",
            "config/settings.json", "{\"debug\": true}"
        );
        
        Map<String, Object> populatedData = new HashMap<>();
        populatedData.put(EngineeringState.SANDBOX_KEY, populatedSandbox);
        
        EngineeringState populatedState = new EngineeringState(populatedData);

        // Then - Populated sandbox
        assertThat(populatedState.getSandbox()).hasSize(3);
        assertThat(populatedState.getSandbox()).containsAllEntriesOf(populatedSandbox);
    }
}