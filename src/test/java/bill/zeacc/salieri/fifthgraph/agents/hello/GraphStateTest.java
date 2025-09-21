package bill.zeacc.salieri.fifthgraph.agents.hello;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals ;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional ;

import org.bsc.langgraph4j.state.AgentState ;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import bill.zeacc.salieri.fifthgraph.model.states.ToolOrientedState;

@ExtendWith(MockitoExtension.class)
@DisplayName("GraphState Tests")
public class GraphStateTest {

    private GraphState graphState;

    @BeforeEach
    void setUp() {
        graphState = new GraphState();
    }

    @Test
    @DisplayName("Should extend ToolOrientedState")
    void shouldExtendToolOrientedState() {
        // Then
        assertThat(graphState).isInstanceOf(ToolOrientedState.class);
    }

    @Test
    @DisplayName("Should initialize with default constructor")
    void shouldInitializeWithDefaultConstructor() {
        // Given
        GraphState state = new GraphState();

        // Then
        assertThat(state).isNotNull();
        assertThat(state.getToolCalls()).isEmpty();
        assertThat(state.getToolResults()).isEmpty();
    }

    @Test
    @DisplayName("Should initialize with init data")
    void shouldInitializeWithInitData() {
        // Given
        Map<String, Object> initData = new HashMap<>();
        initData.put("test_key", "test_value");
        initData.put(GraphState.ANALYSIS_KEY, "analysis_value");

        // When
        GraphState state = new GraphState(initData);

        // Then
        Optional <Object> testVal = state.value("test_key") ;
        assertEquals ( testVal.get ( ), "test_value", "Value in for test_key should be test_value" ) ;
        assertThat(state.value(GraphState.ANALYSIS_KEY)).hasValue("analysis_value");
    }

    @Test
    @DisplayName("Should have correct analysis key constant")
    void shouldHaveCorrectAnalysisKeyConstant() {
        // Then
        assertThat(GraphState.ANALYSIS_KEY).isEqualTo("analysis");
    }

    @Test
    @DisplayName("Should contain analysis key in schema")
    void shouldContainAnalysisKeyInSchema() {
        // Then
        assertThat(GraphState.SCHEMA).containsKey(GraphState.ANALYSIS_KEY);
    }

    @Test
    @DisplayName("Should inherit all ToolOrientedState schema keys")
    void shouldInheritAllToolOrientedStateSchemaKeys() {
        // Then
        assertThat(GraphState.SCHEMA).containsKeys(
            ToolOrientedState.TOOL_CALLS_KEY,
            ToolOrientedState.TOOL_RESULTS_KEY
        );
    }

    @SuppressWarnings ( "serial" )
	@Test
    @DisplayName("Should store and retrieve analysis data")
    void shouldStoreAndRetrieveAnalysisData() {
        // Given
        String analysisData = "This is the analysis result";

        // When
        GraphState newState = new GraphState ( AgentState.updateState ( graphState, new HashMap <> ( ) { { put ( GraphState.ANALYSIS_KEY, analysisData ) ; }}, GraphState.SCHEMA ) ) ;

        // Then
        assertThat(newState.value(GraphState.ANALYSIS_KEY)).hasValue(analysisData);
    }

    @SuppressWarnings ( "serial" )
	@Test
    @DisplayName("Should handle null analysis data")
    void shouldHandleNullAnalysisData() {
        // When
        AgentState.updateState ( graphState, new HashMap <> ( ) { { put ( GraphState.ANALYSIS_KEY, null ) ; }}, GraphState.SCHEMA ) ;

        // Then
        Optional <Object> value = graphState.value(GraphState.ANALYSIS_KEY) ;
        assertThat ( value ).isNotNull ( ) ;
        assertThat ( value ).actual ( ).isEmpty ( ) ;
    }

    @Test
    @DisplayName("Should preserve all state data across operations")
    void shouldPreserveAllStateDataAcrossOperations() {
        // Given
        Map<String, Object> initData = new HashMap<>();
        initData.put("custom_key", "custom_value");
        initData.put(GraphState.ANALYSIS_KEY, "initial_analysis");
        GraphState state = new GraphState(initData);

        // When
        Map <String, Object> newData = Map.of ( "new_key", "new_value", GraphState.ANALYSIS_KEY, "updated_analysis" ) ;
        state = new GraphState ( AgentState.updateState ( state, newData, GraphState.SCHEMA ) ) ;

        // Then
        assertThat(state.value("custom_key")).hasValue("custom_value") ;
        assertThat(state.value("new_key")).hasValue("new_value") ;
        assertThat(state.value(GraphState.ANALYSIS_KEY)).hasValue("updated_analysis") ;
    }
}