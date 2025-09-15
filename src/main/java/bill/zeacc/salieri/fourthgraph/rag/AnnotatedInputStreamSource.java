package bill.zeacc.salieri.fourthgraph.rag;

import java.util.Map ;

import org.springframework.core.io.InputStreamSource ;

public interface AnnotatedInputStreamSource extends InputStreamSource {

	public Map <String, Object> getMetadata ( ) ;
}
