package bill.zeacc.salieri.fourthgraph ;

import static org.junit.jupiter.api.Assertions.assertTrue ;

import java.nio.file.Path ;

import org.junit.jupiter.api.Test ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.boot.test.context.SpringBootTest ;
import org.springframework.context.annotation.ComponentScan ;
import org.springframework.test.context.ActiveProfiles ;

import bill.zeacc.salieri.fourthgraph.rag.HybridRagService ;
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

	@Autowired
	private HybridRagService ragSvc ;

	@Test
	public void ingestFile ( ) throws Exception {
		String path1 = "/home/bill/Downloads/fictional_ai_story.pdf" ;
		String path2 = "/home/bill/Downloads/session_summary.pdf" ;
		Path p1 = Path.of ( path1 ) ;
		Path p2 = Path.of ( path2 ) ;
		ragSvc.ingestPath ( p1, "application/pdf" ) ;
		ragSvc.ingestPath ( p2, "application/pdf" ) ;
	}

	/**
	 * Rigorous Test :-)
	 */
	@Test
	public void shouldAnswerWithTrue ( ) {
		assertTrue ( true ) ;
		System.getProperties ( ).forEach ( ( k, v ) -> System.out.println ( k + "=" + v ) ) ;
	}
}

