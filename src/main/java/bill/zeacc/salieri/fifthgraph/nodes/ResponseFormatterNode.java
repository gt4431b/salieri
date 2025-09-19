package bill.zeacc.salieri.fifthgraph.nodes ;

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

import bill.zeacc.salieri.fifthgraph.model.meta.ChatMsg ;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolCall ;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolResponse ;
import bill.zeacc.salieri.fifthgraph.model.states.ResultOrientedState ;
import bill.zeacc.salieri.fifthgraph.model.states.ToolOrientedState ;
import lombok.extern.slf4j.Slf4j ;
import java.util.* ;

@Component
@Slf4j
public abstract class ResponseFormatterNode implements NodeAction <ToolOrientedState> {

	private final ChatModel chatModel ;
	private String systemPrompt ;

	public ResponseFormatterNode ( ChatModel chatModel, String systemPrompt ) {
		this.chatModel = chatModel ;
		this.systemPrompt = systemPrompt;
	}

	@Override
	public Map <String, Object> apply ( ToolOrientedState state ) {
		List <ChatMsg> msgs = state.getMessages ( ) ;
		List <Message> springMessages = new ArrayList<>();

		// optional system prompt
		springMessages.add ( new SystemMessage ( systemPrompt ) ) ;

		for ( ChatMsg m : msgs ) {
			switch ( m.role ( ) ) {
				case USER      -> springMessages.add(new UserMessage(m.content()));
                case ASSISTANT -> springMessages.add(new AssistantMessage(m.content()));
                case SYSTEM    -> springMessages.add(new SystemMessage(m.content()));
			}
		}

//		String query = state.getQuery ( ) ;
		List <ToolCall> toolCalls = state.getToolCalls ( ) ;
		if ( toolCalls != null && ! toolCalls.isEmpty ( ) ) {
			springMessages.add ( convertToAssistantMessage ( toolCalls ) ) ;
		}

		List <ToolResponse> toolResults = state.getToolResults ( ) ;
		if ( ! toolResults.isEmpty ( ) ) {
			toolResults.forEach ( tr -> springMessages.add ( new ToolResponseMessage ( List.of ( new org.springframework.ai.chat.messages.ToolResponseMessage.ToolResponse ( tr.getId ( ), tr.getToolName ( ), tr.getOutput ( ) ) ) ) ) ) ;
		}
//		springMessages.add(new AssistantMessage( "Answer this query conversationally: %s".formatted ( query ) ) ) ;

		log.info("LLM history this turn:\n{}",
			    state.getMessages().stream()
			        .map(m -> m.role() + ": " + m.content())
			        .collect(java.util.stream.Collectors.joining("\n")));
		ChatResponse resp = chatModel.call ( new Prompt ( springMessages ) ) ;
		String answer = resp.getResult().getOutput().getText();

		return Map.of(
				ResultOrientedState.FINAL_ANSWER_KEY, answer,
				ResultOrientedState.MESSAGES_KEY, List.of(new ChatMsg(ChatMsg.Role.ASSISTANT, answer))
	        ) ;
	}

	private Message convertToAssistantMessage ( List <ToolCall> calls ) {
		List <org.springframework.ai.chat.messages.AssistantMessage.ToolCall> toolCalls = new ArrayList<> ( ) ;
		for ( ToolCall call : calls ) {
			toolCalls.add ( new org.springframework.ai.chat.messages.AssistantMessage.ToolCall ( call.getId ( ), "function", call.getName ( ), call.getArguments ( ) ) ) ;
		}
		AssistantMessage am = new AssistantMessage ( "", Map.of ( ), toolCalls ) ;
		return am ;
	}
}
