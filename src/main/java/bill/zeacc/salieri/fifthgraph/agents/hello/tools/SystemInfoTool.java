package bill.zeacc.salieri.fifthgraph.agents.hello.tools ;

//SystemInfoTool.java
import org.springframework.stereotype.Component ;

import bill.zeacc.salieri.fifthgraph.agents.hello.config.HelloTool ;
import bill.zeacc.salieri.fifthgraph.model.meta.BaseInternalTool ;
import bill.zeacc.salieri.fifthgraph.model.meta.InternalTool ;
import lombok.extern.slf4j.Slf4j ;

import java.io.IOException ;
import java.net.InetAddress ;
import java.util.HashMap ;
import java.util.Map ;

@Component
@Slf4j
@HelloTool
public class SystemInfoTool extends BaseInternalTool implements InternalTool {

	@Override
	public String getName ( ) {
		return "getSystemInfo" ;
	}

	@Override
	public String getDescription ( ) {
		return "Gets system information like hostname, OS, and Java version.  No parameters." ;
	}

	@Override
	public String executionSpec ( ) {
		return """
		{"invocation": "getSystemInfo", "args": []}
			""" ;
	}

	@Override
	protected String doExecute ( String toolExecutionId, Map <String, Object> argsMap ) throws IOException {
		Map <String, String> info = new HashMap <> ( ) ;
		info.put ( "hostname", InetAddress.getLocalHost ( ).getHostName ( ) ) ;
		info.put ( "os", System.getProperty ( "os.name" ) ) ;
		info.put ( "java", System.getProperty ( "java.version" ) ) ;
		info.put ( "user", System.getProperty ( "user.name" ) ) ;
		return info.toString ( ) ;
	}
}
