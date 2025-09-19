package bill.zeacc.salieri.fifthgraph.nodes ;

import org.bsc.langgraph4j.action.NodeAction ;
import org.springframework.ai.chat.messages.SystemMessage ;
import org.springframework.ai.chat.messages.UserMessage ;
//ToolAnalyzerNode.java
import org.springframework.ai.chat.model.ChatModel ;
import org.springframework.ai.chat.prompt.Prompt ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.beans.factory.annotation.Qualifier ;

import com.fasterxml.jackson.core.JsonProcessingException ;
import com.fasterxml.jackson.databind.ObjectMapper ;

import bill.zeacc.salieri.fifthgraph.model.meta.InternalTool ;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolCall ;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolChooser ;
import bill.zeacc.salieri.fifthgraph.model.states.ToolOrientedState ;
import lombok.extern.slf4j.Slf4j ;
import java.util.* ;

@Slf4j
public abstract class ToolAnalyzerNode implements NodeAction <ToolOrientedState> {

	private final ChatModel chatModel ;
	private List <InternalTool> availableTools ;
	@Autowired
	private ObjectMapper om ;

	protected ToolAnalyzerNode ( ChatModel chatModel, @Qualifier ( "helloTools" ) ToolChooser toolProvider ) {
		this.chatModel = chatModel ;
		this.availableTools = toolProvider.get ( ) ;
	}

	@Override
	public Map <String, Object> apply ( ToolOrientedState state ) {
		String query = state.getQuery ( ) ;
		log.info ( "Analyzing query: {}", query ) ;

		SystemMessage sm = buildSystemPrompt ( query ) ;

		String strResponse = chatModel.call ( new Prompt ( sm, new UserMessage ( query ) ) ).getResult ( ).getOutput ( ).getText ( ) ;
		log.debug ( "Analyzer response: {}", strResponse ) ;

		Map <String, Object> updates = new HashMap <> ( ) ;
		updates.put ( ToolOrientedState.ANALYSIS_KEY, strResponse ) ;

		AnalysisResult analysisResult ;
		try {
			analysisResult = om.readValue ( strResponse, AnalysisResult.class ) ;
		} catch ( JsonProcessingException e ) {
			e.printStackTrace();
			throw new RuntimeException ( "Failed to parse analysis response: " + strResponse, e ) ;
		}

		List <ToolCall> toolCalls = new ArrayList <> ( ) ;
		if ( analysisResult.needsTools ( ) ) {
			for ( Invocation tool : analysisResult.tools ( ) ) {
				toolCalls.add ( parseToolCall ( UUID.randomUUID ( ).toString ( ), tool ) ) ;
			}
		}
		updates.put ( ToolOrientedState.TOOL_CALLS_KEY, toolCalls ) ;
		updates.put ( ToolOrientedState.TOOL_RESULTS_KEY, new ArrayList <> ( ) ) ;

		return updates ;
	}

	private ToolCall parseToolCall ( String invocationId, Invocation tool ) {
		String toolName = tool.invocation ( ) ;
		Optional <InternalTool> matchingTool = availableTools.stream ( ).filter ( t -> t.getName ( ).equals ( toolName ) ).findFirst ( ) ;
		if ( matchingTool.isEmpty ( ) ) {
			throw new IllegalArgumentException ( "Requested tool not found: " + toolName ) ;
		}
		Map <String, Object> argsMap = new HashMap <> ( ) ;
		for ( InvocationArg arg : tool.args ( ) ) {
			Object value ;
			switch ( arg.type ( ).toLowerCase ( ) ) {
				case "string"  -> value = arg.stringValue ( ) ;
				case "number"  -> value = Double.parseDouble ( arg.stringValue ( ) ) ;
				case "boolean" -> value = Boolean.parseBoolean ( arg.stringValue ( ) ) ;
				default        -> throw new IllegalArgumentException ( "Unsupported argument type: " + arg.type ( ) ) ;
			}
			argsMap.put ( arg.argName ( ), value ) ;
		}
		String argsJson ;
		try {
			argsJson = om.writeValueAsString ( argsMap ) ;
		} catch ( JsonProcessingException e ) {
			throw new RuntimeException ( "Failed to serialize tool arguments for tool: " + toolName, e ) ;
		}
		return new ToolCall ( invocationId, toolName, argsJson ) ;
	}

	private SystemMessage buildSystemPrompt ( String query ) {
		String metaQ = """
You are a Tool-Need Analyzer. Decide whether the user’s message requires calling any of the allowed tools. Output valid JSON only (no prose, no markdown) that matches the schema below. Be conservative: if the answer can be produced without tools, return no tools.  DO NOT ASK FOR CONFIRMATION.

Allowed tools (injected dynamically):

%s


(Your runtime code injects the full list with each tool’s invocation spec and arg definitions.)

Output schema (JSON only):

{
  "needsTools": true | false,
  "tools": [
    {
      "invocation": "<toolName exactly as listed>",
      "args": [
        {
          "argName": "<param name exactly as listed>",
          "type": "String | Number | Boolean | <fully-qualified type>",
          "stringValue": "<string value if type is a string>"
        }
      ],
      "justification": "One short sentence explaining why this tool is required."
    }
  ]
}


Rules:

If no tool is required:
{"needsTools": false, "tools": []}

Use only tools from the injected list. Do not invent tools or parameters.

If a tool has no args, set "args": [].

If a tool has required args, you must supply them with exact argName and a valid value. Strings must be quoted (valid JSON).

No chaining/nesting; list separate invocations if multiple tools are truly needed.

If essential arguments are missing/ambiguous in the user message, prefer no tools.

Greetings, small talk, opinions, generic reasoning → no tools.

Pattern examples (tool-agnostic)

(These examples reference abstract tool types; the model should map them to the injected tools that match the pattern.)

E1 — No tools (greeting/small talk)
Input: What’s up, friend?
Output:

{"needsTools": false, "tools": []}


E2 — One tool, no args (maps to any allowed tool with zero required args)
Input: What time is it right now?
Output:

{
  "needsTools": true,
  "tools": [
    {
      "invocation": "<ANY_ZERO_ARG_TOOL>",
      "args": [],
      "justification": "The request requires external state not in the conversation."
    }
  ]
}


E3 — One tool with a required string arg (maps to any tool that requires exactly one string parameter)
Input: Open README.md and summarize it.
_Output:*

{
  "needsTools": true,
  "tools": [
    {
      "invocation": "<ANY_TOOL_REQUIRING_1_STRING_ARG>",
      "args": [
        { "argName": "<exactParamNameFromSpec>", "type": "java.lang.String", "stringValue": "README.md" }
      ],
      "justification": "The user requested the contents of a specific resource."
    }
  ]
}


E4 — Multiple independent needs
Input: Tell me the current time and also show my environment details.
_Output:*

{
  "needsTools": true,
  "tools": [
    { "invocation": "<ANY_ZERO_ARG_TOOL_FOR_TIME>", "args": [], "justification": "Current time requested." },
    { "invocation": "<ANY_ZERO_ARG_TOOL_FOR_ENV>", "args": [], "justification": "Environment/system info requested." }
  ]
}


E5 — Ambiguous/missing required arg → no tools
Input: Open the file and read it.
_Output:*

{"needsTools": false, "tools": []}
						""" ;

		return new SystemMessage ( metaQ.formatted ( buildToolList ( ) ) ) ;
	}

	private String buildToolList ( ) {
		StringBuilder sb = new StringBuilder ( ) ;
		for ( InternalTool tool : availableTools ) {
			sb.append ( "Tool: " ).append ( tool.getName ( ) ).append ( " - " ).append ( tool.getDescription ( ) ).append ( " - invocation spec: " ).append ( tool.executionSpec ( ) ).append ( System.lineSeparator ( ) ) ;
		}
		return sb.toString ( ) ;
	}

	private record AnalysisResult ( boolean needsTools, List <Invocation> tools ) { ; }

	private record Invocation ( String invocation, List <InvocationArg> args, String justification ) { ; }

	private record InvocationArg ( String argName, String type, String stringValue ) { ; }
}
