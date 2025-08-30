package bill.zeacc.salieri.secondgraph ;

import dev.langchain4j.agent.tool.P ;
import dev.langchain4j.agent.tool.Tool ;

public class TestTool {

	@Tool ( "tool for test AI agent executor" )
	public String execTest ( @P ( "test message" ) String message ) {
		return String.format ( "test tool ('%s') executed with result 'OK'", message ) ;
	}

	@Tool ( "return current number of system thread allocated by application" )
	public int threadCount ( ) {
		return Thread.getAllStackTraces ( ).size ( ) ;
	}

}
