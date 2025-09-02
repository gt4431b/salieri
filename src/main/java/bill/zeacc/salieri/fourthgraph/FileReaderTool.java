package bill.zeacc.salieri.fourthgraph;

import java.io.IOException ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.nio.file.Paths ;
import java.util.Map ;

import org.springframework.beans.factory.InitializingBean ;
import org.springframework.stereotype.Component ;

@Component
public class FileReaderTool extends BaseTool implements SpringTool, InitializingBean {

	@Override
	public String getName() {
		return "readFile" ;
	}

	@Override
	public String getDescription() {
		return "Reads the content of a file.  One required parameter: fileName" ;
	}

	@Override
	public String executionSpec ( ) {
		return """
				{"invocation": "readFile", "args": [{"argName": "fileName", "type": "string", "description": "Name of the file to read relative to the working directory", "required": true}]}
				""" ;
	}

	@Override
	protected String getToolExecutionId (Map <String, Object> argsMap ) {
		return argsMap.get ( "fileName" ).toString ( ) ;
	}

	@Override
	protected String doExecute ( String toolExecutionId, Map <String, Object> argsMap ) throws IOException {
		String fileName = (String) argsMap.get ( "fileName" ) ;
		Path path = Paths.get ( fileName ) ;
		return Files.readString ( path ) ;
	}

	@Override
	public void afterPropertiesSet ( ) throws Exception {
		init ( ) ;
	}
}
