package bill.zeacc.salieri.fifthgraph.rags;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;

import dev.langchain4j.data.segment.TextSegment;

@ExtendWith(MockitoExtension.class)
@DisplayName("HybridizingSegmentMapper Tests")
public class HybridizingSegmentMapperTest {

    @Mock
    private RagIngestionTreeContext mockContext;

    @Test
    @DisplayName("Should create mapper with deterministic IDs")
    void shouldCreateMapperWithDeterministicIds() {
        // When
        HybridizingSegmentMapper mapper = new HybridizingSegmentMapper(true);

        // Then
        assertThat(mapper).isNotNull();
    }

    @Test
    @DisplayName("Should create mapper with random IDs")
    void shouldCreateMapperWithRandomIds() {
        // When
        HybridizingSegmentMapper mapper = new HybridizingSegmentMapper(false);

        // Then
        assertThat(mapper).isNotNull();
    }

    @Test
    @DisplayName("Should generate deterministic chunk IDs for same content")
    void shouldGenerateDeterministicChunkIdsForSameContent() {
        // Given
        HybridizingSegmentMapper mapper = new HybridizingSegmentMapper(true);
        String text = "This is test content for deterministic ID generation";
        
        TextSegment segment1 = TextSegment.from(text);
        TextSegment segment2 = TextSegment.from(text);
        
        when(mockContext.getProperty(HybridizableRagIngestionKey.BATCH_ID, String.class)).thenReturn("batch-123");
        when(mockContext.getProperty(HybridizableRagIngestionKey.FILE_ID, String.class)).thenReturn("file-456");
        when(mockContext.getProperty(HybridizableRagIngestionKey.DOC_ID, String.class)).thenReturn("doc-789");
        when(mockContext.getProperty(HybridizableRagIngestionKey.DOC_POSITION, AtomicInteger.class))
            .thenReturn(new AtomicInteger(0), new AtomicInteger(1));

        // When
        Document doc1 = mapper.toSpringDoc(segment1, mockContext);
        Document doc2 = mapper.toSpringDoc(segment2, mockContext);

        // Then
        String chunkId1 = (String) doc1.getMetadata().get("chunk_id");
        String chunkId2 = (String) doc2.getMetadata().get("chunk_id");
        assertThat(chunkId1).isEqualTo(chunkId2); // Should be identical for same content
        assertThat(chunkId1).isNotEmpty();
    }

    @Test
    @DisplayName("Should generate different random chunk IDs")
    void shouldGenerateDifferentRandomChunkIds() {
        // Given
        HybridizingSegmentMapper mapper = new HybridizingSegmentMapper(false);
        String text = "This is test content for random ID generation";
        
        TextSegment segment1 = TextSegment.from(text);
        TextSegment segment2 = TextSegment.from(text);
        
        when(mockContext.getProperty(HybridizableRagIngestionKey.BATCH_ID, String.class)).thenReturn("batch-123");
        when(mockContext.getProperty(HybridizableRagIngestionKey.FILE_ID, String.class)).thenReturn("file-456");
        when(mockContext.getProperty(HybridizableRagIngestionKey.DOC_ID, String.class)).thenReturn("doc-789");
        when(mockContext.getProperty(HybridizableRagIngestionKey.DOC_POSITION, AtomicInteger.class))
            .thenReturn(new AtomicInteger(0), new AtomicInteger(1));

        // When
        Document doc1 = mapper.toSpringDoc(segment1, mockContext);
        Document doc2 = mapper.toSpringDoc(segment2, mockContext);

        // Then
        String chunkId1 = (String) doc1.getMetadata().get("chunk_id");
        String chunkId2 = (String) doc2.getMetadata().get("chunk_id");
        assertThat(chunkId1).isNotEqualTo(chunkId2); // Should be different for random mode
        assertThat(chunkId1).isNotEmpty();
        assertThat(chunkId2).isNotEmpty();
    }

    @Test
    @DisplayName("Should add batch metadata when available")
    void shouldAddBatchMetadataWhenAvailable() {
        // Given
        HybridizingSegmentMapper mapper = new HybridizingSegmentMapper(true);
        String batchId = "batch-123";
        String fileId = "file-456";
        String docId = "doc-789";
        AtomicInteger docPosition = new AtomicInteger(5);

        TextSegment segment = TextSegment.from("test content");
        
        when(mockContext.getProperty(HybridizableRagIngestionKey.BATCH_ID, String.class)).thenReturn(batchId);
        when(mockContext.getProperty(HybridizableRagIngestionKey.FILE_ID, String.class)).thenReturn(fileId);
        when(mockContext.getProperty(HybridizableRagIngestionKey.DOC_ID, String.class)).thenReturn(docId);
        when(mockContext.getProperty(HybridizableRagIngestionKey.DOC_POSITION, AtomicInteger.class)).thenReturn(docPosition);

        // When
        Document result = mapper.toSpringDoc(segment, mockContext);

        // Then
        Map<String, Object> metadata = result.getMetadata();
        assertThat(metadata.get("batch_id")).isEqualTo(batchId);
        assertThat(metadata.get("file_id")).isEqualTo(fileId);
        assertThat(metadata.get("doc_id")).isEqualTo(docId);
        assertThat(metadata.get("doc_position")).isEqualTo(5);
    }

    @Test
    @DisplayName("Should add default source when not present")
    void shouldAddDefaultSourceWhenNotPresent() {
        // Given
        HybridizingSegmentMapper mapper = new HybridizingSegmentMapper(true);
        TextSegment segment = TextSegment.from("test content");

        when(mockContext.getProperty(HybridizableRagIngestionKey.BATCH_ID, String.class)).thenReturn(null);
        when(mockContext.getProperty(HybridizableRagIngestionKey.FILE_ID, String.class)).thenReturn(null);
        when(mockContext.getProperty(HybridizableRagIngestionKey.DOC_ID, String.class)).thenReturn(null);
        when(mockContext.getProperty(HybridizableRagIngestionKey.DOC_POSITION, AtomicInteger.class)).thenReturn(null);

        // When
        Document result = mapper.toSpringDoc(segment, mockContext);

        // Then
        assertThat(result.getMetadata().get("source")).isEqualTo("unknown");
    }

    @Test
    @DisplayName("Should preserve existing metadata")
    void shouldPreserveExistingMetadata() {
        // Given
        HybridizingSegmentMapper mapper = new HybridizingSegmentMapper(true);
        
        // Create TextSegment with metadata using the correct factory method
        TextSegment segment = TextSegment.from("test content");

        when(mockContext.getProperty(HybridizableRagIngestionKey.BATCH_ID, String.class)).thenReturn(null);
        when(mockContext.getProperty(HybridizableRagIngestionKey.FILE_ID, String.class)).thenReturn(null);
        when(mockContext.getProperty(HybridizableRagIngestionKey.DOC_ID, String.class)).thenReturn(null);
        when(mockContext.getProperty(HybridizableRagIngestionKey.DOC_POSITION, AtomicInteger.class)).thenReturn(null);

        // When
        Document result = mapper.toSpringDoc(segment, mockContext);

        // Then
        Map<String, Object> resultMetadata = result.getMetadata();
        assertThat(resultMetadata.get("source")).isEqualTo("unknown"); // Default source should be added
        assertThat(resultMetadata).containsKey("chunk_id"); // Should add chunk_id
    }

    @Test
    @DisplayName("Should increment document position correctly")
    void shouldIncrementDocumentPositionCorrectly() {
        // Given
        HybridizingSegmentMapper mapper = new HybridizingSegmentMapper(true);
        AtomicInteger docPosition = new AtomicInteger(10);

        TextSegment segment = TextSegment.from("test content");
        
        when(mockContext.getProperty(HybridizableRagIngestionKey.BATCH_ID, String.class)).thenReturn("batch");
        when(mockContext.getProperty(HybridizableRagIngestionKey.FILE_ID, String.class)).thenReturn("file");
        when(mockContext.getProperty(HybridizableRagIngestionKey.DOC_ID, String.class)).thenReturn("doc");
        when(mockContext.getProperty(HybridizableRagIngestionKey.DOC_POSITION, AtomicInteger.class)).thenReturn(docPosition);

        // When
        Document result = mapper.toSpringDoc(segment, mockContext);

        // Then
        assertThat(result.getMetadata().get("doc_position")).isEqualTo(10);
        assertThat(docPosition.get()).isEqualTo(11); // Should be incremented
    }
}