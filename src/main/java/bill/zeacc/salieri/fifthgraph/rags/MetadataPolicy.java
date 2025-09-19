package bill.zeacc.salieri.fifthgraph.rags ;

import dev.langchain4j.data.document.Document ;
import dev.langchain4j.data.segment.TextSegment ;
import java.util.Map ;

public interface MetadataPolicy {

	public Map <String, Object> build ( Document document, TextSegment segment ) ;
}
