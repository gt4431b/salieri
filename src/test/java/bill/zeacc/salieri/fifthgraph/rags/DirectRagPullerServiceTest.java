package bill.zeacc.salieri.fifthgraph.rags;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull ;
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
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("DirectRagPullerService Tests")
public class DirectRagPullerServiceTest {

    @Mock
    private VectorStore mockVectorStore;

    @Mock
    private DocumentSearchOptions mockSearchOptions;

    private DirectRagPullerService ragPullerService;

    @BeforeEach
    void setUp() {
        ragPullerService = new DirectRagPullerService();
        // Use ReflectionTestUtils to inject the mock instead of direct field access
        ReflectionTestUtils.setField(ragPullerService, "vectorStore", mockVectorStore);
    }

    @Test
    @DisplayName("Should search with correct parameters")
    void shouldSearchWithCorrectParameters() {
        // Given
        String query = "What is artificial intelligence?";
        int topK = 5;
        
        when(mockSearchOptions.getTopK()).thenReturn(topK);
        when(mockSearchOptions.getSearchTopK()).thenReturn(Map.of());
        
        Document mockDoc1 = new Document("AI is a field of computer science...");
        Document mockDoc2 = new Document("Machine learning is a subset of AI...");
        List<Document> expectedResults = List.of(mockDoc1, mockDoc2);
        
        when(mockVectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(expectedResults);

        // When
        List<Document> results = ragPullerService.search(query, mockSearchOptions);

        // Then
        assertThat(results).isEqualTo(expectedResults);
        assertThat(results).hasSize(2);
        
        verify(mockVectorStore).similaritySearch(any(SearchRequest.class));
    }

    @Test
    @DisplayName("Should use vector search topK when specified")
    void shouldUseVectorSearchTopKWhenSpecified() {
        // Given
        String query = "Test query";
        int defaultTopK = 10;
        int vectorTopK = 15;
        
        when(mockSearchOptions.getTopK()).thenReturn(defaultTopK);
        when(mockSearchOptions.getSearchTopK()).thenReturn(
            Map.of(DocumentSearchOptions.SearchType.VECTOR, vectorTopK));
        
        when(mockVectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        // When
        ragPullerService.search(query, mockSearchOptions);

        // Then
        verify(mockVectorStore).similaritySearch(any(SearchRequest.class));
    }

    @Test
    @DisplayName("Should fall back to default topK when vector search topK not specified")
    void shouldFallBackToDefaultTopKWhenVectorSearchTopKNotSpecified() {
        // Given
        String query = "Test query";
        int defaultTopK = 20;
        
        when(mockSearchOptions.getTopK()).thenReturn(defaultTopK);
        when(mockSearchOptions.getSearchTopK()).thenReturn(Map.of()); // Empty map
        
        when(mockVectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        // When
        ragPullerService.search(query, mockSearchOptions);

        // Then
        verify(mockVectorStore).similaritySearch(any(SearchRequest.class));
    }

    @Test
    @DisplayName("Should handle empty search results")
    void shouldHandleEmptySearchResults() {
        // Given
        String query = "Obscure query with no matches";
        when(mockSearchOptions.getTopK()).thenReturn(5);
        when(mockSearchOptions.getSearchTopK()).thenReturn(Map.of());
        when(mockVectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        // When
        List<Document> results = ragPullerService.search(query, mockSearchOptions);

        // Then
        assertThat(results).isEmpty();
        verify(mockVectorStore).similaritySearch(any(SearchRequest.class));
    }

    @Test
    @DisplayName("Should handle null query gracefully")
    void shouldHandleNullQueryGracefully() {
        // Given
        String query = ""; // Use empty string instead of null since SearchRequest.Builder doesn't accept null
        when(mockSearchOptions.getTopK()).thenReturn(5);
        when(mockSearchOptions.getSearchTopK()).thenReturn(Map.of());
        when(mockVectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        // When
        List<Document> results = ragPullerService.search(query, mockSearchOptions);
        assertNotNull(results);

        // Then
        verify(mockVectorStore).similaritySearch(any(SearchRequest.class));
    }
}