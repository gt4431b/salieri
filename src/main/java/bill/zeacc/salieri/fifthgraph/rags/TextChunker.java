package bill.zeacc.salieri.fifthgraph.rags ;

import dev.langchain4j.data.document.Document ;
import dev.langchain4j.data.segment.TextSegment ;
import java.util.List ;

public interface TextChunker {

	public List <TextSegment> split ( Document document, RagIngestionTreeContext ctx  ) ;
}
