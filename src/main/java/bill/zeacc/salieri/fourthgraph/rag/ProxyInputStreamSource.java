package bill.zeacc.salieri.fourthgraph.rag;

import java.io.IOException ;
import java.io.InputStream ;
import java.util.HashMap ;
import java.util.Map ;

import org.springframework.core.io.InputStreamSource ;


public class ProxyInputStreamSource implements AnnotatedInputStreamSource {

	private InputStreamSource src ;
	private Map <String, Object> metadata ;

	public ProxyInputStreamSource ( InputStreamSource src ) {
		this ( src, new HashMap <String, Object> ( ) ) ;
	}

	public ProxyInputStreamSource ( InputStreamSource src, Map <String, Object> hashMap ) {
		this.src = src ;
		this.metadata = hashMap ;
	}

	@Override
	public InputStream getInputStream ( ) throws IOException {
		return src.getInputStream ( ) ;
	}

	@Override
	public Map <String, Object> getMetadata ( ) {
		return metadata ;
	}
}
