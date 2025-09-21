package bill.zeacc.salieri.fifthgraph.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("NodeHelper Tests")
public class NodeHelperTest {

    @Test
    @DisplayName("Should convert NodeAction to AsyncNodeAction successfully")
    public void shouldConvertNodeActionToAsyncNodeActionSuccessfully() throws Exception {
        // Given
        @SuppressWarnings("unchecked")
        NodeAction<TestState> mockNodeAction = mock(NodeAction.class);
        TestState inputState = new TestState();
        Map<String, Object> expectedResult = Map.of("key", "value");
        when(mockNodeAction.apply(inputState)).thenReturn(expectedResult);

        // When
        AsyncNodeAction<TestState> asyncAction = NodeHelper.toAsync(mockNodeAction);
        CompletableFuture<Map<String, Object>> future = asyncAction.apply(inputState);
        Map<String, Object> result = future.get();

        // Then
        assertThat(result).isEqualTo(expectedResult);
        verify(mockNodeAction).apply(inputState);
    }

    @Test
    @DisplayName("Should handle RuntimeException from NodeAction in toAsync")
    public void shouldHandleRuntimeExceptionFromNodeActionInToAsync() throws Exception {
        // Given
        @SuppressWarnings("unchecked")
        NodeAction<TestState> mockNodeAction = mock(NodeAction.class);
        TestState inputState = new TestState();
        RuntimeException runtimeException = new RuntimeException("Test runtime exception");
        when(mockNodeAction.apply(inputState)).thenThrow(runtimeException);

        // When
        AsyncNodeAction<TestState> asyncAction = NodeHelper.toAsync(mockNodeAction);
        CompletableFuture<Map<String, Object>> future = asyncAction.apply(inputState);

        // Then
        assertThatThrownBy(() -> future.get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasRootCauseMessage("Test runtime exception");
    }

    @Test
    @DisplayName("Should handle checked Exception from NodeAction in toAsync")
    public void shouldHandleCheckedExceptionFromNodeActionInToAsync() throws Exception {
        // Given
        @SuppressWarnings("unchecked")
        NodeAction<TestState> mockNodeAction = mock(NodeAction.class);
        TestState inputState = new TestState();
        Exception checkedException = new Exception("Test checked exception");
        when(mockNodeAction.apply(inputState)).thenThrow(checkedException);

        // When
        AsyncNodeAction<TestState> asyncAction = NodeHelper.toAsync(mockNodeAction);
        CompletableFuture<Map<String, Object>> future = asyncAction.apply(inputState);

        // Then
        assertThatThrownBy(() -> future.get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasRootCauseMessage("Test checked exception");
    }

    @Test
    @DisplayName("Should handle RuntimeException in rt method")
    public void shouldHandleRuntimeExceptionInRtMethod() throws Exception {
        // Given
        @SuppressWarnings("unchecked")
        NodeAction<TestState> mockNodeAction = mock(NodeAction.class);
        TestState inputState = new TestState();
        RuntimeException runtimeException = new RuntimeException("Runtime exception in rt");
        when(mockNodeAction.apply(inputState)).thenThrow(runtimeException);

        // When
        AsyncNodeAction<TestState> asyncAction = NodeHelper.toAsync(mockNodeAction);
        CompletableFuture<Map<String, Object>> future = asyncAction.apply(inputState);

        // Then
        assertThatThrownBy(() -> future.get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasRootCauseMessage("Runtime exception in rt");
    }

    @Test
    @DisplayName("Should handle checked Exception in rt method")
    public void shouldHandleCheckedExceptionInRtMethod() throws Exception {
        // Given
        @SuppressWarnings("unchecked")
        NodeAction<TestState> mockNodeAction = mock(NodeAction.class);
        TestState inputState = new TestState();
        Exception checkedException = new Exception("Checked exception in rt");
        when(mockNodeAction.apply(inputState)).thenThrow(checkedException);

        // When
        AsyncNodeAction<TestState> asyncAction = NodeHelper.toAsync(mockNodeAction);
        CompletableFuture<Map<String, Object>> future = asyncAction.apply(inputState);

        // Then
        assertThatThrownBy(() -> future.get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasRootCauseMessage("Checked exception in rt");
    }

    @Test
    @DisplayName("Should convert NodeAction with different state types")
    public void shouldConvertNodeActionWithDifferentStateTypes() throws Exception {
        // Given
        @SuppressWarnings("unchecked")
        NodeAction<AgentState> mockNodeAction = mock(NodeAction.class);
        ExtendedTestState inputState = new ExtendedTestState();
        Map<String, Object> expectedResult = Map.of("extended", "value");
        when(mockNodeAction.apply(inputState)).thenReturn(expectedResult);

        // When
        AsyncNodeAction<ExtendedTestState> asyncAction = NodeHelper.toAsync(mockNodeAction);
        CompletableFuture<Map<String, Object>> future = asyncAction.apply(inputState);
        Map<String, Object> result = future.get();

        // Then
        assertThat(result).isEqualTo(expectedResult);
        verify(mockNodeAction).apply(inputState);
    }

    @Test
    @DisplayName("Should handle null result from NodeAction")
    public void shouldHandleNullResultFromNodeAction() throws Exception {
        // Given
        @SuppressWarnings("unchecked")
        NodeAction<TestState> mockNodeAction = mock(NodeAction.class);
        TestState inputState = new TestState();
        when(mockNodeAction.apply(inputState)).thenReturn(null);

        // When
        AsyncNodeAction<TestState> asyncAction = NodeHelper.toAsync(mockNodeAction);
        CompletableFuture<Map<String, Object>> future = asyncAction.apply(inputState);
        Map<String, Object> result = future.get();

        // Then
        assertThat(result).isNull();
        verify(mockNodeAction).apply(inputState);
    }

    @Test
    @DisplayName("Should handle empty result from NodeAction")
    public void shouldHandleEmptyResultFromNodeAction() throws Exception {
        // Given
        @SuppressWarnings("unchecked")
        NodeAction<TestState> mockNodeAction = mock(NodeAction.class);
        TestState inputState = new TestState();
        Map<String, Object> emptyResult = new HashMap<>();
        when(mockNodeAction.apply(inputState)).thenReturn(emptyResult);

        // When
        AsyncNodeAction<TestState> asyncAction = NodeHelper.toAsync(mockNodeAction);
        CompletableFuture<Map<String, Object>> future = asyncAction.apply(inputState);
        Map<String, Object> result = future.get();

        // Then
        assertThat(result).isEqualTo(emptyResult);
        assertThat(result).isEmpty();
        verify(mockNodeAction).apply(inputState);
    }

    @Test
    @DisplayName("Should test NodeHelper constructor")
    public void shouldTestNodeHelperConstructor() {
        // Test the constructor to improve coverage
        NodeHelper nodeHelper = new NodeHelper();
        assertThat(nodeHelper).isNotNull();
    }

    @Test
    @DisplayName("Should test rt method directly")
    public void shouldTestRtMethodDirectly() throws Exception {
        // Given
        @SuppressWarnings("unchecked")
        NodeAction<TestState> mockNodeAction = mock(NodeAction.class);
        TestState inputState = new TestState();
        Map<String, Object> expectedResult = Map.of("direct", "result");
        when(mockNodeAction.apply(inputState)).thenReturn(expectedResult);

        // When
        Map<String, Object> result = NodeHelper.rt(mockNodeAction, inputState);

        // Then
        assertThat(result).isEqualTo(expectedResult);
        verify(mockNodeAction).apply(inputState);
    }

    @Test
    @DisplayName("Should test rt method with exception")
    public void shouldTestRtMethodWithException() throws Exception {
        // Given
        @SuppressWarnings("unchecked")
        NodeAction<TestState> mockNodeAction = mock(NodeAction.class);
        TestState inputState = new TestState();
        Exception testException = new Exception("Test exception in rt");
        when(mockNodeAction.apply(inputState)).thenThrow(testException);

        // When/Then
        assertThatThrownBy(() -> NodeHelper.rt(mockNodeAction, inputState))
            .isInstanceOf(RuntimeException.class)
            .hasRootCauseMessage("Test exception in rt");
    }

    // Test state classes
    private static class TestState extends AgentState {
        public TestState() {
            super(new HashMap<>());
        }
    }

    private static class ExtendedTestState extends TestState {
        public ExtendedTestState() {
            super();
        }
    }
}