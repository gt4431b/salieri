package bill.zeacc.salieri.fourthgraph.rag ;

import dev.langchain4j.data.document.Document ;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser ;
import java.io.FileInputStream ;
import java.io.IOException ;
import java.util.List ;

import org.springframework.core.io.InputStreamSource ;

public class PdfBoxCorpusParser implements CorpusParser {

	private final ApachePdfBoxDocumentParser parser ;

	public PdfBoxCorpusParser ( ) {
		this.parser = new ApachePdfBoxDocumentParser ( true ) ; // include pdf metadata
	}

	@Override
    public List <Document> parse(InputStreamSource in, RagIngestionTreeContext ctx) throws IOException {
		try ( FileInputStream fis = (FileInputStream) in.getInputStream ( ) ) {
			return List.of ( parser.parse ( fis ) ) ;
		}
    }

	@Override
	public boolean supports ( String contentType, RagIngestionTreeContext ctx ) {
		return "application/pdf".equals ( contentType ) ;
	}
}
