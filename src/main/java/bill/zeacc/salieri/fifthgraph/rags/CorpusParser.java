package bill.zeacc.salieri.fifthgraph.rags ;

import dev.langchain4j.data.document.Document ;
import java.io.IOException ;
import java.util.List ;

import org.springframework.core.io.InputStreamSource ;

public interface CorpusParser {

	public List <Document> parse ( InputStreamSource in, RagIngestionTreeContext ctx ) throws IOException ;

	public boolean supports ( String contentType, RagIngestionTreeContext ctx ) ;
}
