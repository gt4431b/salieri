package bill.zeacc.salieri.fifthgraph.agents.hello.tools;

import java.io.IOException ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.nio.file.Paths ;
import java.util.Map ;

import org.springframework.beans.factory.InitializingBean ;
import org.springframework.stereotype.Component ;

import bill.zeacc.salieri.fifthgraph.agents.hello.config.HelloTool ;
import bill.zeacc.salieri.fifthgraph.model.meta.BaseInternalTool ;
import bill.zeacc.salieri.fifthgraph.model.meta.InternalTool ;

@Component
@HelloTool
public class FileReaderTool extends BaseInternalTool implements InternalTool, InitializingBean {

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
