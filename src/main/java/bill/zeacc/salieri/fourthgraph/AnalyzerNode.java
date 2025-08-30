package bill.zeacc.salieri.fourthgraph ;

import org.bsc.langgraph4j.action.NodeAction ;
//AnalyzerNode.java
import org.springframework.ai.chat.model.ChatModel ;
import org.springframework.ai.chat.prompt.Prompt ;
import org.springframework.stereotype.Component ;
import lombok.extern.slf4j.Slf4j ;
import java.util.* ;

@Component
@Slf4j
public class AnalyzerNode implements NodeAction <GraphState> {

	private final ChatModel chatModel ;

	public AnalyzerNode ( ChatModel chatModel ) {
		this.chatModel = chatModel ;
	}

	@Override
	public Map <String, Object> apply ( GraphState state ) {
		String query = state.getQuery ( ) ;
		log.info ( "Analyzing query: {}", query ) ;

		String prompt = String.format ( "Analyze this query and determine if tools are needed: %s\n" + "Available tools: getSystemInfo, getDateTime\n"
				+ "Respond with JSON only: {\"needsTools\": true/false, \"tools\": [\"tool1\", \"tool2\"]}", query ) ;

		String response = chatModel.call ( new Prompt ( prompt ) ).getResult ( ).getOutput ( ).getText ( ) ;
		log.debug ( "Analyzer response: {}", response ) ;

		Map <String, Object> updates = new HashMap <> ( ) ;
		updates.put ( GraphState.ANALYSIS_KEY, response ) ;

		// Simple parsing for tool needs
		if ( response.contains ( "\"needsTools\":true" ) || response.contains ( "\"needsTools\": true" ) ) {
			List <ToolCall> toolCalls = new ArrayList <> ( ) ;
			if ( response.contains ( "getSystemInfo" ) ) {
				toolCalls.add ( new ToolCall ( "getSystemInfo", "{}" ) ) ;
			}
			if ( response.contains ( "getDateTime" ) ) {
				toolCalls.add ( new ToolCall ( "getDateTime", "{}" ) ) ;
			}
			if ( ! toolCalls.isEmpty ( ) ) {
				updates.put ( GraphState.TOOL_CALLS_KEY, toolCalls ) ;
			}
		}

		return updates ;
	}
}
