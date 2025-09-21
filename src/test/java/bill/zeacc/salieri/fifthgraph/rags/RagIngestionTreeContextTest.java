package bill.zeacc.salieri.fifthgraph.rags;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy ;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RagIngestionTreeContext Tests")
public class RagIngestionTreeContextTest {

    private RagIngestionTreeContext context;

    @BeforeEach
    void setUp() {
        context = new RagIngestionTreeContext();
    }

    @Test
    @DisplayName("Should set and get batch root correctly")
    void shouldSetAndGetBatchRootCorrectly() {
        // Given
        Path expectedPath = Paths.get("/test/batch/root");

        // When
        context.setBatchRoot(expectedPath);

        // Then
        assertThat(context.getBatchRoot()).isEqualTo(expectedPath);
    }

    @Test
    @DisplayName("Should handle null batch root")
    void shouldHandleNullBatchRoot() {
        // When
        context.setBatchRoot(null);

        // Then
        assertThat(context.getBatchRoot()).isNull();
    }

    @Test
    @DisplayName("Should set and get properties with correct types")
    void shouldSetAndGetPropertiesWithCorrectTypes() {
        // Given
        String stringValue = "test-value";
        AtomicInteger atomicValue = new AtomicInteger(100);

        // When
        context.setProperty(HybridizableRagIngestionKey.BATCH_ID, stringValue);
        context.setProperty(HybridizableRagIngestionKey.DOC_POSITION, atomicValue);

        // Then
        assertThat(context.getProperty(HybridizableRagIngestionKey.BATCH_ID, String.class))
            .isEqualTo(stringValue);
        assertThat(context.getProperty(HybridizableRagIngestionKey.DOC_POSITION, AtomicInteger.class))
            .isEqualTo(atomicValue);
    }

    @Test
    @DisplayName("Should return null for non-existent properties")
    void shouldReturnNullForNonExistentProperties() {
        // When
        String result = context.getProperty(HybridizableRagIngestionKey.FILE_ID, String.class);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle type mismatches gracefully")
    void shouldHandleTypeMismatchesGracefully() {
        // Given
        context.setProperty(HybridizableRagIngestionKey.BATCH_ID, "string-value");

        // When / Then
        assertThatThrownBy ( ( ) -> context.getProperty(HybridizableRagIngestionKey.BATCH_ID, Integer.class) )
			.isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should overwrite existing properties")
    void shouldOverwriteExistingProperties() {
        // Given
        String initialValue = "initial";
        String newValue = "updated";

        // When
        context.setProperty(HybridizableRagIngestionKey.BATCH_ID, initialValue);
        context.setProperty(HybridizableRagIngestionKey.BATCH_ID, newValue);

        // Then
        assertThat(context.getProperty(HybridizableRagIngestionKey.BATCH_ID, String.class))
            .isEqualTo(newValue);
    }

    @Test
    @DisplayName("Should handle null property values")
    void shouldHandleNullPropertyValues() {
        // When
        context.setProperty(HybridizableRagIngestionKey.BATCH_ID, null);

        // Then
        assertThat(context.getProperty(HybridizableRagIngestionKey.BATCH_ID, String.class))
            .isNull();
    }

    @Test
    @DisplayName("Should maintain independent property storage")
    void shouldMaintainIndependentPropertyStorage() {
        // Given
        String batchId = "batch-123";
        String fileId = "file-456";
        AtomicInteger docPosition = new AtomicInteger(10);

        // When
        context.setProperty(HybridizableRagIngestionKey.BATCH_ID, batchId);
        context.setProperty(HybridizableRagIngestionKey.FILE_ID, fileId);
        context.setProperty(HybridizableRagIngestionKey.DOC_POSITION, docPosition);

        // Then
        assertThat(context.getProperty(HybridizableRagIngestionKey.BATCH_ID, String.class))
            .isEqualTo(batchId);
        assertThat(context.getProperty(HybridizableRagIngestionKey.FILE_ID, String.class))
            .isEqualTo(fileId);
        assertThat(context.getProperty(HybridizableRagIngestionKey.DOC_POSITION, AtomicInteger.class))
            .isEqualTo(docPosition);
    }

    @Test
    @DisplayName("Should handle complex path objects")
    void shouldHandleComplexPathObjects() {
        // Given
        Path complexPath = Paths.get("/home/user/documents/nested/folders/batch");

        // When
        context.setBatchRoot(complexPath);

        // Then
        assertThat(context.getBatchRoot()).isEqualTo(complexPath);
        assertThat(context.getBatchRoot().toString()).contains("nested", "folders", "batch");
    }
}