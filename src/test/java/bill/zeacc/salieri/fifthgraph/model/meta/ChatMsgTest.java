package bill.zeacc.salieri.fifthgraph.model.meta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMsg Tests - Branch Coverage Focus")
public class ChatMsgTest {

    @Test
    @DisplayName("Should create ChatMsg with USER role")
    void shouldCreateChatMsgWithUserRole() {
        // Given
        ChatMsg.Role role = ChatMsg.Role.USER;
        String content = "Hello, how are you?";

        // When
        ChatMsg chatMsg = new ChatMsg(role, content);

        // Then
        assertThat(chatMsg.role()).isEqualTo(ChatMsg.Role.USER);
        assertThat(chatMsg.content()).isEqualTo(content);
    }

    @Test
    @DisplayName("Should create ChatMsg with ASSISTANT role")
    void shouldCreateChatMsgWithAssistantRole() {
        // Given
        ChatMsg.Role role = ChatMsg.Role.ASSISTANT;
        String content = "I'm doing well, thank you!";

        // When
        ChatMsg chatMsg = new ChatMsg(role, content);

        // Then
        assertThat(chatMsg.role()).isEqualTo(ChatMsg.Role.ASSISTANT);
        assertThat(chatMsg.content()).isEqualTo(content);
    }

    @Test
    @DisplayName("Should create ChatMsg with SYSTEM role")
    void shouldCreateChatMsgWithSystemRole() {
        // Given
        ChatMsg.Role role = ChatMsg.Role.SYSTEM;
        String content = "You are a helpful assistant.";

        // When
        ChatMsg chatMsg = new ChatMsg(role, content);

        // Then
        assertThat(chatMsg.role()).isEqualTo(ChatMsg.Role.SYSTEM);
        assertThat(chatMsg.content()).isEqualTo(content);
    }

    @Test
    @DisplayName("Should handle null content")
    void shouldHandleNullContent() {
        // Given
        ChatMsg.Role role = ChatMsg.Role.USER;
        String content = null;

        // When
        ChatMsg chatMsg = new ChatMsg(role, content);

        // Then
        assertThat(chatMsg.role()).isEqualTo(ChatMsg.Role.USER);
        assertThat(chatMsg.content()).isNull();
    }

    @Test
    @DisplayName("Should handle null role")
    void shouldHandleNullRole() {
        // Given
        ChatMsg.Role role = null;
        String content = "Test content";

        // When
        ChatMsg chatMsg = new ChatMsg(role, content);

        // Then
        assertThat(chatMsg.role()).isNull();
        assertThat(chatMsg.content()).isEqualTo(content);
    }

    @Test
    @DisplayName("Should handle empty content")
    void shouldHandleEmptyContent() {
        // Given
        ChatMsg.Role role = ChatMsg.Role.ASSISTANT;
        String content = "";

        // When
        ChatMsg chatMsg = new ChatMsg(role, content);

        // Then
        assertThat(chatMsg.role()).isEqualTo(ChatMsg.Role.ASSISTANT);
        assertThat(chatMsg.content()).isEmpty();
    }

    @Test
    @DisplayName("Should handle very long content")
    void shouldHandleVeryLongContent() {
        // Given
        ChatMsg.Role role = ChatMsg.Role.USER;
        String content = "Very long content ".repeat(1000);

        // When
        ChatMsg chatMsg = new ChatMsg(role, content);

        // Then
        assertThat(chatMsg.role()).isEqualTo(ChatMsg.Role.USER);
        assertThat(chatMsg.content()).isEqualTo(content);
        assertThat(chatMsg.content().length()).isGreaterThan(10000);
    }

    @Test
    @DisplayName("Should handle special characters and unicode")
    void shouldHandleSpecialCharactersAndUnicode() {
        // Given
        ChatMsg.Role role = ChatMsg.Role.SYSTEM;
        String content = "Special chars: áéíóú, symbols: ∑∆π, emojis: 🤖📝, newlines:\nLine 2";

        // When
        ChatMsg chatMsg = new ChatMsg(role, content);

        // Then
        assertThat(chatMsg.role()).isEqualTo(ChatMsg.Role.SYSTEM);
        assertThat(chatMsg.content()).isEqualTo(content);
        assertThat(chatMsg.content()).contains("áéíóú", "∑∆π", "🤖", "\n");
    }

    @Test
    @DisplayName("Should be serializable")
    void shouldBeSerializable() {
        // Given
        ChatMsg chatMsg = new ChatMsg(ChatMsg.Role.USER, "Test message");

        // Then
        assertThat(chatMsg).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    @DisplayName("Should test all Role enum values")
    void shouldTestAllRoleEnumValues() {
        // Given/When/Then
        ChatMsg.Role[] roles = ChatMsg.Role.values();
        
        assertThat(roles).hasSize(3);
        assertThat(roles).contains(ChatMsg.Role.SYSTEM, ChatMsg.Role.USER, ChatMsg.Role.ASSISTANT);
        
        // Test enum valueOf
        assertThat(ChatMsg.Role.valueOf("SYSTEM")).isEqualTo(ChatMsg.Role.SYSTEM);
        assertThat(ChatMsg.Role.valueOf("USER")).isEqualTo(ChatMsg.Role.USER);
        assertThat(ChatMsg.Role.valueOf("ASSISTANT")).isEqualTo(ChatMsg.Role.ASSISTANT);
    }

    @Test
    @DisplayName("Should handle invalid enum value")
    void shouldHandleInvalidEnumValue() {
        // Given/When/Then
        assertThatThrownBy(() -> ChatMsg.Role.valueOf("INVALID_ROLE"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should handle null enum value")
    void shouldHandleNullEnumValue() {
        // Given/When/Then
        assertThatThrownBy(() -> ChatMsg.Role.valueOf(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should test equals and hashCode behavior")
    void shouldTestEqualsAndHashCodeBehavior() {
        // Given
        ChatMsg msg1 = new ChatMsg(ChatMsg.Role.USER, "Hello");
        ChatMsg msg2 = new ChatMsg(ChatMsg.Role.USER, "Hello");
        ChatMsg msg3 = new ChatMsg(ChatMsg.Role.ASSISTANT, "Hello");
        ChatMsg msg4 = new ChatMsg(ChatMsg.Role.USER, "Hi");

        // Then - test equals
        assertThat(msg1).isNotEqualTo(msg2); // Different instances
        assertThat(msg1).isNotEqualTo(msg3); // Different role
        assertThat(msg1).isNotEqualTo(msg4); // Different content
        assertThat(msg1).isNotEqualTo(null); // Null comparison
        assertThat(msg1).isEqualTo(msg1); // Self comparison
    }
}