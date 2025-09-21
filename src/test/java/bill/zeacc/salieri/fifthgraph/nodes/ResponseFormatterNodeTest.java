package bill.zeacc.salieri.fifthgraph.nodes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import bill.zeacc.salieri.fifthgraph.model.meta.ChatMsg;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolCall;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolResponse;
import bill.zeacc.salieri.fifthgraph.model.states.ToolOrientedState;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResponseFormatterNode Tests")
public class ResponseFormatterNodeTest {

    @Mock
    private ChatModel mockChatModel;

    @Mock
    private ToolOrientedState mockState;

    @Mock
    private ChatResponse mockChatResponse;

    @Mock
    private Generation mockGeneration;

    private TestResponseFormatterNode responseFormatterNode;
    private String systemPrompt;

    @BeforeEach
    void setUp() {
        systemPrompt = "You are a helpful assistant. Format responses clearly and concisely.";
        responseFormatterNode = new TestResponseFormatterNode(mockChatModel, systemPrompt);
    }

    @Test
    @DisplayName("Should initialize with chat model and system prompt")
    void shouldInitializeWithChatModelAndSystemPrompt() {
        // When
        TestResponseFormatterNode node = new TestResponseFormatterNode(mockChatModel, "test prompt");

        // Then
        assertThat(node).isNotNull();
    }

    @Test
    @DisplayName("Should format response with conversation history")
    void shouldFormatResponseWithConversationHistory() {
        // Given
        String expectedResponse = "Here's your formatted response based on the conversation.";
        List<ChatMsg> messages = List.of(
            new ChatMsg(ChatMsg.Role.USER, "Hello"),
            new ChatMsg(ChatMsg.Role.ASSISTANT, "Hi there!")
        );

        when(mockState.getMessages()).thenReturn(messages);
        when(mockState.getToolCalls()).thenReturn(List.of());
        when(mockState.getToolResults()).thenReturn(List.of());
        when(mockChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(new org.springframework.ai.chat.messages.AssistantMessage(expectedResponse));

        // When
        Map<String, Object> result = responseFormatterNode.apply(mockState);

        // Then
        assertThat(result).containsKey("final_answer");
        assertThat(result.get("final_answer")).isEqualTo(expectedResponse);
        verify(mockChatModel).call(any(Prompt.class));
    }

    @Test
    @DisplayName("Should include tool results in conversation context")
    void shouldIncludeToolResultsInConversationContext() {
        // Given
        String expectedResponse = "Based on the tool results, here's your answer.";
        List<ChatMsg> messages = List.of(new ChatMsg(ChatMsg.Role.USER, "What time is it?"));
        List<ToolCall> toolCalls = List.of(new ToolCall("getTime", "call-1", "{}"));
        List<ToolResponse> toolResults = List.of(new ToolResponse("call-1", "getTime", "2025-09-19T10:30:00Z"));

        when(mockState.getMessages()).thenReturn(messages);
        when(mockState.getToolCalls()).thenReturn(toolCalls);
        when(mockState.getToolResults()).thenReturn(toolResults);
        when(mockChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(new org.springframework.ai.chat.messages.AssistantMessage(expectedResponse));

        // When
        Map<String, Object> result = responseFormatterNode.apply(mockState);

        // Then
        assertThat(result).containsKey("final_answer");
        assertThat(result.get("final_answer")).isEqualTo(expectedResponse);
        verify(mockChatModel).call(any(Prompt.class));
    }

    @Test
    @DisplayName("Should handle empty message history")
    void shouldHandleEmptyMessageHistory() {
        // Given
        String expectedResponse = "I can help you with your request.";
        
        when(mockState.getMessages()).thenReturn(List.of());
        when(mockState.getToolCalls()).thenReturn(List.of());
        when(mockState.getToolResults()).thenReturn(List.of());
        when(mockChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(new org.springframework.ai.chat.messages.AssistantMessage(expectedResponse));

        // When
        Map<String, Object> result = responseFormatterNode.apply(mockState);

        // Then
        assertThat(result).containsKey("final_answer");
        assertThat(result.get("final_answer")).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("Should handle chat model exceptions")
    void shouldHandleChatModelExceptions() {
        // Given
        when(mockState.getMessages()).thenReturn(List.of());
        when(mockState.getToolCalls()).thenReturn(List.of());
        when(mockState.getToolResults()).thenReturn(List.of());
        when(mockChatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("Chat model error"));

        // When/Then
        assertThatThrownBy(() -> responseFormatterNode.apply(mockState))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Chat model error");
    }

    @Test
    @DisplayName("Should handle null state gracefully")
    void shouldHandleNullStateGracefully() {
        // When/Then
        assertThatThrownBy(() -> responseFormatterNode.apply(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should handle tool calls without results")
    void shouldHandleToolCallsWithoutResults() {
        // Given
        String expectedResponse = "I need to use some tools to answer your question.";
        List<ToolCall> toolCalls = List.of(new ToolCall("getTool", "call-1", "{}"));
        
        when(mockState.getMessages()).thenReturn(List.of());
        when(mockState.getToolCalls()).thenReturn(toolCalls);
        when(mockState.getToolResults()).thenReturn(List.of()); // No results yet
        when(mockChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(new org.springframework.ai.chat.messages.AssistantMessage(expectedResponse));

        // When
        Map<String, Object> result = responseFormatterNode.apply(mockState);

        // Then
        assertThat(result).containsKey("final_answer");
        assertThat(result.get("final_answer")).isEqualTo(expectedResponse);
    }

    // Concrete test implementation of the abstract class
    private static class TestResponseFormatterNode extends ResponseFormatterNode {
        public TestResponseFormatterNode(ChatModel chatModel, String systemPrompt) {
            super(chatModel, systemPrompt);
        }
    }
}