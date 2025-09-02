package bill.zeacc.salieri.fourthgraph ;

import static org.junit.jupiter.api.Assertions.assertTrue ;

import java.nio.file.Path ;

import org.junit.jupiter.api.Test ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.boot.test.context.SpringBootTest ;
import org.springframework.context.annotation.ComponentScan ;
import org.springframework.test.context.ActiveProfiles ;

import com.fasterxml.jackson.databind.ObjectMapper ;

import bill.zeacc.salieri.fourthgraph.BaseTool.ToolArgumentSpec ;
import bill.zeacc.salieri.fourthgraph.rag.RagIngestService ;

/**
 * Unit test for simple App.
 */
@SpringBootTest
@ComponentScan ( {
		"bill.zeacc.salieri.fourthgraph.fourthgraph"
} )
//@EnableJpaRepositories ( {
//		"bill.zz.play.repository"
//} )
//@EntityScan ( "bill.zz.model" )
@ActiveProfiles("test")
public class AppTest {

	@Autowired
	private RagIngestService ingestSvc ;

	@Test
	public void ingestFile ( ) throws Exception {
		String path1 = "/home/bill/Downloads/fictional_ai_story.pdf" ;
		String path2 = "/home/bill/Downloads/session_summary.pdf" ;
		Path p1 = Path.of ( path1 ) ;
		Path p2 = Path.of ( path2 ) ;
		ingestSvc.indexPath ( p1 ) ;
		ingestSvc.indexPath ( p2 ) ;
	}

	/**
	 * Rigorous Test :-)
	 */
//	@Test
	public void shouldAnswerWithTrue ( ) {
		assertTrue ( true ) ;
		System.getProperties ( ).forEach ( ( k, v ) -> System.out.println ( k + "=" + v ) ) ;
	}

	ObjectMapper om = new ObjectMapper ( ) ;
//	@Test
	public void anotherTest ( ) throws Exception {
		
		/*
{"fileName":{"type":"java.lang.String", " description":"'Name of the file to read relative to the working directory'", " required":"true"}"}"
		 */
		
//		String foo = "{fileName: {type: java.lang.String, description: 'Name of the file to read relative to the working directory', required: true}}" ;
		String foo = "{name: fileReaderTool, descriptor: {type: java.lang.String, description: 'Name of the file to read relative to the working directory', required: true}}" ;
		foo = quoteFuck ( foo ) ;
		ToolArgumentSpec result = om.readValue ( foo, ToolArgumentSpec.class ) ;
		System.out.println ( result ) ;
	}

	private String quoteFuck ( String s ) {
		final int MODE_NORMAL = 0 ;
		final int MODE_IN_QUOTES = 1 ;
		int mode = MODE_NORMAL ;
		int depth = 0 ;
		boolean preserveSpaces = false ;
		StringBuilder sb = new StringBuilder ( ) ;
		// DOUBLE QUOTES ONLY.  NO SINGLE QUOTES!
		for ( int i = 0 ; i < s.length ( ) ; i ++ ) {
			char c = s.charAt ( i ) ;
			switch ( mode ) {
				case MODE_NORMAL:
					if ( c == '{' || c == '}' ) {
						sb.append ( c ) ;
					} else if ( c != ' ' ) {
						sb.append ( '"' ) ;
						--i ;
						mode = MODE_IN_QUOTES ;
						depth = 1 ;
					}
					break ;
				case MODE_IN_QUOTES:
					if ( c == ':' && depth == 1 ) {
						sb.append ( '"' ) ;
						sb.append ( ':' ) ;
						mode = MODE_NORMAL ;
					} else if ( c == ',' ) {
						sb.append ( '"' ) ;
						sb.append ( ',' ) ;
						sb.append ( ' ' ) ;
						sb.append ( '"' ) ;
						mode = MODE_IN_QUOTES ;
					} else if ( c == '}' ) {
						-- depth ;
						sb.append ( '"' ) ;
						sb.append ( '}' ) ;
						mode = MODE_NORMAL ;
					} else if ( c == '\'' ) {
						preserveSpaces = ! preserveSpaces ;
						// skip the quote
					} else {
						if ( c == ' ' && ! preserveSpaces ) {
							// skip the space
							continue ;
						}
						sb.append ( c ) ;
					}
					break ;
			}
		}
		if ( mode == MODE_IN_QUOTES ) {
			sb.append ( '"' ) ;
		}

		return sb.toString ( ) ;
	}
}

