package bill.zeacc.salieri.fourthgraph.rag ;

import dev.langchain4j.data.document.Document ;
import java.io.File ;
import java.io.IOException ;
import java.util.List ;

public interface CorpusParser {

	public List <Document> parse ( File file, RagIngestionContext ctx ) throws IOException ;

	public boolean supports ( File file, RagIngestionContext ctx ) ;
}
