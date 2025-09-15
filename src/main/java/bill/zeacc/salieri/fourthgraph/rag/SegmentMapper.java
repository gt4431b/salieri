package bill.zeacc.salieri.fourthgraph.rag ;

import org.springframework.ai.document.Document ;
import dev.langchain4j.data.segment.TextSegment ;

public interface SegmentMapper {

	public Document toSpringDoc ( TextSegment segment, RagIngestionTreeContext ctx ) ;
}
