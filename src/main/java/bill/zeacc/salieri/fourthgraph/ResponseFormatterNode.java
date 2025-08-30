package bill.zeacc.salieri.fourthgraph ;

import org.bsc.langgraph4j.action.NodeAction ;
import org.springframework.ai.chat.messages.AssistantMessage ;
import org.springframework.ai.chat.messages.Message ;
import org.springframework.ai.chat.messages.SystemMessage ;
import org.springframework.ai.chat.messages.ToolResponseMessage ;
import org.springframework.ai.chat.messages.UserMessage ;
import org.springframework.ai.chat.model.ChatModel ;
import org.springframework.ai.chat.model.ChatResponse ;
import org.springframework.ai.chat.prompt.Prompt ;
import org.springframework.stereotype.Component ;

import lombok.extern.slf4j.Slf4j ;
import java.util.* ;

@Component
@Slf4j
public class ResponseFormatterNode implements NodeAction <GraphState> {

	private final ChatModel chatModel ;

	public ResponseFormatterNode ( ChatModel chatModel ) {
		this.chatModel = chatModel ;
	}

	@Override
	public Map <String, Object> apply ( GraphState state ) {
		List <ChatMsg> msgs = state.getMessages ( ) ;
		List <Message> springMessages = new ArrayList<>();

		// optional system prompt
		springMessages.add ( new SystemMessage ( "You are a helpful assistant but also kind of a snarky little shit. Keep answers concise.  Be natural and conversational." ) ) ;

		for ( ChatMsg m : msgs ) {
			switch ( m.role ( ) ) {
				case USER      -> springMessages.add(new UserMessage(m.content()));
                case ASSISTANT -> springMessages.add(new AssistantMessage(m.content()));
                case SYSTEM    -> springMessages.add(new SystemMessage(m.content()));
			}
		}

//		String query = state.getQuery ( ) ;
		List <ToolResponse> toolResults = state.getToolResults ( ) ;

		if ( ! toolResults.isEmpty ( ) ) {
			toolResults.forEach ( tr -> springMessages.add ( new ToolResponseMessage ( List.of ( new org.springframework.ai.chat.messages.ToolResponseMessage.ToolResponse ( null, tr.toolName ( ), tr.output ( ) ) ) ) ) ) ;
		}
//		springMessages.add(new AssistantMessage( "Answer this query conversationally: %s".formatted ( query ) ) ) ;

		log.info("LLM history this turn:\n{}",
			    state.getMessages().stream()
			        .map(m -> m.role() + ": " + m.content())
			        .collect(java.util.stream.Collectors.joining("\n")));
		ChatResponse resp = chatModel.call ( new Prompt ( springMessages ) ) ;
		String answer = resp.getResult().getOutput().getText();

		return Map.of(
	            GraphState.FINAL_ANSWER_KEY, answer,
	            GraphState.MESSAGES_KEY, List.of(new ChatMsg(ChatMsg.Role.ASSISTANT, answer))
	        );
	}
}
