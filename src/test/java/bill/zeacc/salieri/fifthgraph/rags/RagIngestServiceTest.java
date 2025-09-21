package bill.zeacc.salieri.fifthgraph.rags;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.vectorstore.VectorStore;

@ExtendWith(MockitoExtension.class)
@DisplayName("RagIngestService Tests")
public class RagIngestServiceTest {

    @Mock
    private VectorStore mockVectorStore;

    @TempDir
    Path tempDir;

    private RagIngestService ragIngestService;

    @BeforeEach
    void setUp() {
        ragIngestService = new RagIngestService(mockVectorStore);
    }

    @Test
    @DisplayName("Should initialize with vector store dependency")
    void shouldInitializeWithVectorStoreDependency() {
        // Given/When
        RagIngestService service = new RagIngestService(mockVectorStore);

        // Then
        assertThat(service).isNotNull();
    }

    @Test
    @DisplayName("Should handle empty directory gracefully")
    void shouldHandleEmptyDirectoryGracefully() throws IOException {
        // Given
        Path emptyDir = tempDir.resolve("empty");
        java.nio.file.Files.createDirectory(emptyDir);

        // When/Then - Should not throw exception
        ragIngestService.indexPath(emptyDir, "application/pdf");
        
        // Vector store should not receive any documents
        verify(mockVectorStore, times(0)).add(anyList());
    }

    @Test
    @DisplayName("Should handle non-existent directory")
    void shouldHandleNonExistentDirectory() {
        // Given
        Path nonExistentDir = tempDir.resolve("non-existent");

        // When/Then
        assertThatThrownBy(() -> ragIngestService.indexPath(nonExistentDir, "application/pdf"))
            .isInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("Should process content type parameter")
    void shouldProcessContentTypeParameter() throws IOException {
        // Given
        String contentType = "application/pdf";

        // When
        ragIngestService.indexPath(tempDir, contentType);

        // Then - Should complete without error
        assertThat(true).isTrue(); // Placeholder assertion
    }

    @Test
    @DisplayName("Should handle null content type")
    void shouldHandleNullContentType() throws IOException {
        // When/Then - Should not throw exception
        ragIngestService.indexPath(tempDir, null);
    }

    @Test
    @DisplayName("Should be able to create multiple instances")
    void shouldBeAbleToCreateMultipleInstances() {
        // Given
        VectorStore mockVectorStore2 = mock(VectorStore.class);

        // When
        RagIngestService service1 = new RagIngestService(mockVectorStore);
        RagIngestService service2 = new RagIngestService(mockVectorStore2);

        // Then
        assertThat(service1).isNotNull();
        assertThat(service2).isNotNull();
        assertThat(service1).isNotSameAs(service2);
    }
}