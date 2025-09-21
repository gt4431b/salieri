package bill.zeacc.salieri.fifthgraph.model.states;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bsc.langgraph4j.state.AgentState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import bill.zeacc.salieri.fifthgraph.model.meta.ChatMsg;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResultOrientedState Tests")
public class ResultOrientedStateTest {

    private ResultOrientedState resultOrientedState;

    @BeforeEach
    void setUp() {
        resultOrientedState = new ResultOrientedState();
    }

    @Test
    @DisplayName("Should initialize with default constructor")
    void shouldInitializeWithDefaultConstructor() {
        // Given
        ResultOrientedState state = new ResultOrientedState();

        // Then
        assertThat(state.getQuery()).isEmpty();
        assertThat(state.getMessages()).isEmpty();
        assertThat(state.getFinalAnswer()).isEmpty();
    }

    @Test
    @DisplayName("Should initialize with init data")
    void shouldInitializeWithInitData() {
        // Given
        String expectedQuery = "test query";
        String expectedAnswer = "test answer";
        List<ChatMsg> expectedMessages = List.of(
            new ChatMsg(ChatMsg.Role.USER, "Hello"),
            new ChatMsg(ChatMsg.Role.ASSISTANT, "Hi there!")
        );

        Map<String, Object> initData = new HashMap<>();
        initData.put(ResultOrientedState.QUERY_KEY, expectedQuery);
        initData.put(ResultOrientedState.FINAL_ANSWER_KEY, expectedAnswer);
        initData.put(ResultOrientedState.MESSAGES_KEY, expectedMessages);

        // When
        ResultOrientedState state = new ResultOrientedState(initData);

        // Then
        assertThat(state.getQuery()).isEqualTo(expectedQuery);
        assertThat(state.getFinalAnswer()).isEqualTo(expectedAnswer);
        assertThat(state.getMessages()).isEqualTo(expectedMessages);
    }

    @Test
    @DisplayName("Should have correct schema keys")
    void shouldHaveCorrectSchemaKeys() {
        // Then
        assertThat(ResultOrientedState.SCHEMA).containsKeys(
            ResultOrientedState.QUERY_KEY,
            ResultOrientedState.FINAL_ANSWER_KEY,
            ResultOrientedState.MESSAGES_KEY
        );
        assertThat(ResultOrientedState.QUERY_KEY).isEqualTo("query");
        assertThat(ResultOrientedState.FINAL_ANSWER_KEY).isEqualTo("final_answer");
        assertThat(ResultOrientedState.MESSAGES_KEY).isEqualTo("messages");
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValuesGracefully() {
        // Given
        Map<String, Object> initData = new HashMap<>();
        initData.put(ResultOrientedState.QUERY_KEY, null);
        initData.put(ResultOrientedState.FINAL_ANSWER_KEY, null);
        initData.put(ResultOrientedState.MESSAGES_KEY, null);

        // When
        ResultOrientedState state = new ResultOrientedState(initData);

        // Then
        assertThat(state.getQuery()).isEmpty();
        assertThat(state.getFinalAnswer()).isEmpty();
        assertThat(state.getMessages()).isEmpty();
    }

    @Test
    @DisplayName("Should return empty defaults when keys not present")
    void shouldReturnEmptyDefaultsWhenKeysNotPresent() {
        // Given
        Map<String, Object> emptyInitData = new HashMap<>();

        // When
        ResultOrientedState state = new ResultOrientedState(emptyInitData);

        // Then
        assertThat(state.getQuery()).isEmpty();
        assertThat(state.getFinalAnswer()).isEmpty();
        assertThat(state.getMessages()).isEmpty();
    }

    @Test
    @DisplayName("Should update state correctly")
    void shouldUpdateStateCorrectly() {
        // Given
        String newQuery = "updated query";
        String newAnswer = "updated answer";
        List<ChatMsg> newMessages = List.of(new ChatMsg(ChatMsg.Role.USER, "New message"));

        Map<String, Object> updateData = new HashMap<>();
        updateData.put(ResultOrientedState.QUERY_KEY, newQuery);
        updateData.put(ResultOrientedState.FINAL_ANSWER_KEY, newAnswer);
        updateData.put(ResultOrientedState.MESSAGES_KEY, newMessages);

        // When
        ResultOrientedState newState = new ResultOrientedState ( AgentState.updateState(resultOrientedState, updateData, ResultOrientedState.SCHEMA) ) ;

        // Then
        assertThat(newState.getQuery()).isEqualTo(newQuery);
        assertThat(newState.getFinalAnswer()).isEqualTo(newAnswer);
        assertThat(newState.getMessages()).isEqualTo(newMessages);
    }

    @Test
    @DisplayName("Should append messages correctly")
    void shouldAppendMessagesCorrectly() {
        // Given - Initial state with some messages
        List<ChatMsg> initialMessages = List.of(new ChatMsg(ChatMsg.Role.USER, "Initial message"));
        Map<String, Object> initData = new HashMap<>();
        initData.put(ResultOrientedState.MESSAGES_KEY, initialMessages);
        ResultOrientedState state = new ResultOrientedState(initData);

        // When - Add more messages
        List<ChatMsg> additionalMessages = List.of(
            new ChatMsg(ChatMsg.Role.ASSISTANT, "Response 1"),
            new ChatMsg(ChatMsg.Role.USER, "Follow up")
        );
        Map<String, Object> updateData = new HashMap<>();
        updateData.put(ResultOrientedState.MESSAGES_KEY, additionalMessages);
        ResultOrientedState newState = new ResultOrientedState ( AgentState.updateState(state, updateData, ResultOrientedState.SCHEMA) ) ;

        // Then - Messages should be appended (due to Channels.appender in schema)
        List<ChatMsg> allMessages = newState.getMessages();
        assertThat(allMessages).hasSize(3);
        assertThat(allMessages.get(0).content()).isEqualTo("Initial message");
        assertThat(allMessages.get(1).content()).isEqualTo("Response 1");
        assertThat(allMessages.get(2).content()).isEqualTo("Follow up");
    }

    @Test
    @DisplayName("Should handle mixed data types correctly")
    void shouldHandleMixedDataTypesCorrectly() {
        // Given
        Map<String, Object> initData = new HashMap<>();
        initData.put(ResultOrientedState.QUERY_KEY, "valid query");
        initData.put("custom_key", "custom_value");
        initData.put("numeric_key", 42);

        // When
        ResultOrientedState state = new ResultOrientedState(initData);

        // Then
        assertThat(state.getQuery()).isEqualTo("valid query");
        assertThat(state.value("custom_key")).hasValue("custom_value");
        assertThat(state.value("numeric_key")).hasValue(42);
    }

    @Test
    @DisplayName("Should preserve custom state data")
    void shouldPreserveCustomStateData() {
        // Given
        Map<String, Object> initData = new HashMap<>();
        initData.put("custom_field1", "value1");
        initData.put("custom_field2", List.of("item1", "item2"));
        initData.put(ResultOrientedState.QUERY_KEY, "test query");

        // When
        ResultOrientedState state = new ResultOrientedState(initData);

        // Then
        assertThat(state.getQuery()).isEqualTo("test query");
        assertThat(state.value("custom_field1")).hasValue("value1");
        assertThat(state.value("custom_field2")).hasValue(List.of("item1", "item2"));
    }

    @Test
    @DisplayName("Should handle state updates with partial data")
    void shouldHandleStateUpdatesWithPartialData() {
        // Given - Initial state
        Map<String, Object> initData = new HashMap<>();
        initData.put(ResultOrientedState.QUERY_KEY, "initial query");
        initData.put(ResultOrientedState.FINAL_ANSWER_KEY, "initial answer");
        ResultOrientedState state = new ResultOrientedState(initData);

        // When - Update only query
        Map<String, Object> updateData = new HashMap<>();
        updateData.put(ResultOrientedState.QUERY_KEY, "updated query");
        ResultOrientedState newState = new ResultOrientedState ( AgentState.updateState(state, updateData, ResultOrientedState.SCHEMA) ) ;

        // Then - Only query should be updated, answer should remain
        assertThat(newState.getQuery()).isEqualTo("updated query");
        assertThat(newState.getFinalAnswer()).isEqualTo("initial answer");
    }

    @Test
    @DisplayName("Should handle empty string values correctly")
    void shouldHandleEmptyStringValuesCorrectly() {
        // Given
        Map<String, Object> initData = new HashMap<>();
        initData.put(ResultOrientedState.QUERY_KEY, "");
        initData.put(ResultOrientedState.FINAL_ANSWER_KEY, "");

        // When
        ResultOrientedState state = new ResultOrientedState(initData);

        // Then
        assertThat(state.getQuery()).isEmpty();
        assertThat(state.getFinalAnswer()).isEmpty();
    }
}