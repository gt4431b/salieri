package bill.zeacc.salieri.fourthgraph ;

//SystemInfoTool.java
import org.springframework.stereotype.Component ;
import lombok.extern.slf4j.Slf4j ;

import java.io.IOException ;
import java.util.HashMap ;
import java.util.Map ;

@Component
@Slf4j
public class PwdTool extends BaseTool implements SpringTool {

	@Override
	public String getName ( ) {
		return "pwd" ;
	}

	@Override
	public String getDescription ( ) {
		return "Gets current working directory." ;
	}

	@Override
	public String executionSpec ( ) {
		return """
		{"invocation": "pwd", "args": []}
			""" ;
	}

	@Override
	protected String doExecute ( String toolExecutionId, Map <String, Object> argsMap ) throws IOException {
		Map <String, String> info = new HashMap <> ( ) ;
		info.put ( "pwd", System.getProperty ( "user.dir" ) ) ;
		return info.toString ( ) ;
	}
}
