package bill.zeacc.salieri.fourthgraph.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import java.io.File;
import java.io.FileInputStream ;
import java.io.IOException;
import java.util.List;

public class PdfBoxCorpusParser implements CorpusParser {

    private final ApachePdfBoxDocumentParser parser;

    public PdfBoxCorpusParser() {
        this.parser = new ApachePdfBoxDocumentParser(true); // include pdf metadata
    }

    @Override
    public List <Document> parse(File file, RagIngestionContext ctx) throws IOException {
    	try ( FileInputStream fis = new FileInputStream ( file ) ) {
    		return List.of ( parser.parse ( fis ) ) ;
    	}
    }

    @Override
    public boolean supports(File file, RagIngestionContext ctx ) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".pdf");
    }
}
