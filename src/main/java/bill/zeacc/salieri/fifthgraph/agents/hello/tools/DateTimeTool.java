package bill.zeacc.salieri.fifthgraph.agents.hello.tools ;

//DateTimeTool.java
import org.springframework.stereotype.Component ;

import bill.zeacc.salieri.fifthgraph.agents.hello.config.HelloTool ;
import bill.zeacc.salieri.fifthgraph.model.meta.BaseInternalTool ;
import bill.zeacc.salieri.fifthgraph.model.meta.InternalTool ;
import lombok.extern.slf4j.Slf4j ;
import java.time.ZonedDateTime ;
import java.io.IOException ;
import java.time.ZoneId ;
import java.time.format.DateTimeFormatter ;
import java.util.Map ;

@Component
@Slf4j
@HelloTool
public class DateTimeTool extends BaseInternalTool implements InternalTool {

	@Override
	public String getName ( ) {
		return "getDateTime" ;
	}

	@Override
	public String getDescription ( ) {
		return "Gets current date and time.  No parameters." ;
	}

	@Override
	public String executionSpec ( ) {
		return """
		{"invocation": "getDateTime", "args": []}
			""" ;
	}

	@Override
	public String doExecute ( String toolExecutionId, Map <String, Object> argsMap ) throws IOException {
		ZonedDateTime now = ZonedDateTime.now ( ZoneId.systemDefault ( ) ) ;
		return now.format ( DateTimeFormatter.RFC_1123_DATE_TIME ) ;
	}
}
