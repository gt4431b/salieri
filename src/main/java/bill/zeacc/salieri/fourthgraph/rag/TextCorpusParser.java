package bill.zeacc.salieri.fourthgraph.rag ;

import java.io.BufferedReader ;
import java.io.IOException ;
import java.io.InputStreamReader ;
import java.nio.charset.StandardCharsets ;
import java.util.List ;

import org.springframework.core.io.InputStreamSource ;

import dev.langchain4j.data.document.Document ;
import dev.langchain4j.data.document.Metadata ;

public class TextCorpusParser implements CorpusParser {

	@Override
	public List <Document> parse ( InputStreamSource in, RagIngestionTreeContext ctx ) throws IOException {
		try ( BufferedReader reader = new BufferedReader ( new InputStreamReader ( in.getInputStream ( ), StandardCharsets.UTF_8 ) ) ) {
			StringBuilder content = new StringBuilder ( ) ;
			String line ;
			while ( ( line = reader.readLine ( ) ) != null ) {
				content.append ( line ).append ( "\n" ) ;
			}
			Metadata metadata = new Metadata ( ) ;
			metadata.put ( "source", ctx.getCurrentFile ( ) != null ? ctx.getCurrentFile ( ).getName ( ) : "unknown" ) ;
			Document doc = Document.from ( content.toString ( ), metadata ) ;
			return List.of ( doc ) ;
		}
	}

	@Override
	public boolean supports ( String contentType, RagIngestionTreeContext ctx ) {
		return "text/plain".equals ( contentType ) || "text/csv".equals ( contentType ) || "application/json".equals ( contentType ) ;
	}
}
