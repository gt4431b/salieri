package bill.zeacc.salieri.fifthgraph.rags;

import java.util.Map ;

import org.springframework.core.io.InputStreamSource ;

public interface AnnotatedInputStreamSource extends InputStreamSource {

	public Map <String, Object> getMetadata ( ) ;
}
