package bill.zeacc.salieri.fifthgraph.rags ;

import java.io.BufferedReader ;
import java.io.IOException ;
import java.io.InputStreamReader ;
import java.nio.charset.StandardCharsets ;
import java.util.List ;
import java.util.Set ;

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
			metadata.put ( "source", getFileName ( ctx ) ) ;
			Document doc = Document.from ( content.toString ( ), metadata ) ;
			return List.of ( doc ) ;
		}
	}

	private String getFileName ( RagIngestionTreeContext ctx ) {
		String name = ctx.getProperty ( HybridizableRagIngestionKey.FILE_NAME, String.class ) ;
		return name == null ? "unknown" : name ;
	}

	private static final Set <String> SUPPORTED_CONTENT_TYPES = Set.of ( "text/plain", "text/csv", "application/json", "text/markdown", "text/html" ) ;
	@Override
	public boolean supports ( String contentType, RagIngestionTreeContext ctx ) {
		if ( contentType == null ) {
			return false ;
		}
		return SUPPORTED_CONTENT_TYPES.contains ( contentType ) ;
	}
}
