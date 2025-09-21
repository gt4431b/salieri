package bill.zeacc.salieri.fifthgraph.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import bill.zeacc.salieri.fifthgraph.model.states.ResultOrientedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import bill.zeacc.salieri.fifthgraph.service.GraphService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatController Tests")
public class ChatControllerTest {

    @Mock
    private GraphService mockGraphService;

    @Mock
    private ResultOrientedState mockResultState;

    private ChatController chatController;

    @BeforeEach
    void setUp() {
        chatController = new ChatController(mockGraphService);
    }

    @Test
    @DisplayName("Should process chat request successfully")
    void shouldProcessChatRequestSuccessfully() {
        // Given
        String agentName = "test-agent";
        String query = "Hello, how are you?";
        String expectedAnswer = "I'm doing well, thank you!";
        ChatRequest request = new ChatRequest(agentName, query);

        when(mockGraphService.processQuery(eq(agentName), eq(query), eq("temp-session-id")))
            .thenReturn(mockResultState);
        when(mockResultState.value("final_answer"))
            .thenReturn(Optional.of(expectedAnswer));

        // When
        ResponseEntity<ChatResponse> response = chatController.chat(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().response()).isEqualTo(expectedAnswer);
        
        verify(mockGraphService).processQuery(agentName, query, "temp-session-id");
        verify(mockResultState).value("final_answer");
    }

    @Test
    @DisplayName("Should handle missing final_answer gracefully")
    void shouldHandleMissingFinalAnswerGracefully() {
        // Given
        String agentName = "test-agent";
        String query = "Test query";
        ChatRequest request = new ChatRequest(agentName, query);

        when(mockGraphService.processQuery(eq(agentName), eq(query), eq("temp-session-id")))
            .thenReturn(mockResultState);
        when(mockResultState.value("final_answer"))
            .thenReturn(Optional.empty());

        // When
        ResponseEntity<ChatResponse> response = chatController.chat(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().response()).isEqualTo("No answer generated.");
    }

    @Test
    @DisplayName("Should handle null final_answer value gracefully")
    void shouldHandleNullFinalAnswerValueGracefully() {
        // Given
        String agentName = "test-agent";
        String query = "Test query";
        ChatRequest request = new ChatRequest(agentName, query);

        when(mockGraphService.processQuery(eq(agentName), eq(query), eq("temp-session-id")))
            .thenReturn(mockResultState);
        when(mockResultState.value("final_answer"))
            .thenReturn(Optional.ofNullable ( null ) ) ;

        // When
        ResponseEntity<ChatResponse> response = chatController.chat(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().response()).isEqualTo("No answer generated.");
    }

    @Test
    @DisplayName("Should return health check successfully")
    void shouldReturnHealthCheckSuccessfully() {
        // When
        ResponseEntity<String> response = chatController.health();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("OK");
    }

    @Test
    @DisplayName("Should handle different agent names")
    void shouldHandleDifferentAgentNames() {
        // Given
        String agentName = "hello_agent";
        String query = "Say hello";
        String expectedAnswer = "Hello there!";
        ChatRequest request = new ChatRequest(agentName, query);

        when(mockGraphService.processQuery(eq(agentName), eq(query), eq("temp-session-id")))
            .thenReturn(mockResultState);
        when(mockResultState.value("final_answer"))
            .thenReturn(Optional.of(expectedAnswer));

        // When
        ResponseEntity<ChatResponse> response = chatController.chat(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().response()).isEqualTo(expectedAnswer);
        verify(mockGraphService).processQuery(agentName, query, "temp-session-id");
    }

    @Test
    @DisplayName("Should handle empty query")
    void shouldHandleEmptyQuery() {
        // Given
        String agentName = "test-agent";
        String query = "";
        String expectedAnswer = "Empty response";
        ChatRequest request = new ChatRequest(agentName, query);

        when(mockGraphService.processQuery(eq(agentName), eq(query), eq("temp-session-id")))
            .thenReturn(mockResultState);
        when(mockResultState.value("final_answer"))
            .thenReturn(Optional.of(expectedAnswer));

        // When
        ResponseEntity<ChatResponse> response = chatController.chat(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().response()).isEqualTo(expectedAnswer);
        verify(mockGraphService).processQuery(agentName, query, "temp-session-id");
    }
}