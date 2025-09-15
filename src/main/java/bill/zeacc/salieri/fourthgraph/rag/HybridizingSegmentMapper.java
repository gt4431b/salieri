package bill.zeacc.salieri.fourthgraph.rag ;

import dev.langchain4j.data.segment.TextSegment;
import org.springframework.ai.document.Document;

import java.nio.charset.StandardCharsets ;
import java.security.MessageDigest ;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID ;
import java.util.concurrent.atomic.AtomicInteger ;

public class HybridizingSegmentMapper implements SegmentMapper {

	private final boolean deterministicIds;

    public HybridizingSegmentMapper(boolean deterministicIds) {
        this.deterministicIds = deterministicIds;
    }

    @Override
    public Document toSpringDoc(TextSegment segment, RagIngestionTreeContext ctx) {
        Map<String, Object> meta = new HashMap<>(segment.metadata().toMap());

        String batchId = ctx.getProperty ( HybridizableRagIngestionKey.BATCH_ID, String.class ) ;
        String fileId = ctx.getProperty ( HybridizableRagIngestionKey.FILE_ID, String.class ) ;
        String docId = ctx.getProperty ( HybridizableRagIngestionKey.DOC_ID, String.class ) ;
        AtomicInteger docPos = ctx.getProperty ( HybridizableRagIngestionKey.DOC_POSITION, AtomicInteger.class ) ;
        // Add/normalize fields you care about
        if (!meta.containsKey("source")) {
            meta.put("source", "unknown");
        }
        if ( docId != null ) {
			meta.put ( "batch_id", batchId ) ;
		}
        if ( docId != null ) {
			meta.put ( "file_id", fileId ) ;
		}
        if ( docId != null ) {
			meta.put ( "doc_id", docId ) ; // This is the part that makes hybridizing with radius possible at all, along with the doc_position
		}
        if ( docPos != null ) {
        	meta.put ( "doc_position", docPos.getAndIncrement ( ) ) ;
        }
        // Generate a chunk_id (choose deterministic or random)
        String chunkId = deterministicIds
                ? sha256Hex(segment.text())
                : UUID.randomUUID().toString();
        meta.put("chunk_id", chunkId);

        // Optional: attach a document-level id if your parser provided one
        // meta.put("doc_id", meta.getOrDefault("doc_id", chunkId));

        return new Document(segment.text(), meta);
    }

    private static String sha256Hex(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(dig.length * 2);
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            // Fallback if SHA-256 not available for some reason
            return UUID.randomUUID().toString();
        }
    }
}
