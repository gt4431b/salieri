package bill.zeacc.salieri.fifthgraph.agents.switchboard.nodes;

import java.util.List ;
import java.util.Map ;

import org.bsc.langgraph4j.action.NodeAction ;
import org.springframework.ai.chat.messages.Message ;
import org.springframework.ai.chat.messages.SystemMessage ;
import org.springframework.ai.chat.messages.UserMessage ;
import org.springframework.ai.chat.model.ChatModel ;
import org.springframework.ai.chat.prompt.Prompt ;
import org.springframework.beans.BeansException ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.context.ApplicationContext ;
import org.springframework.context.ApplicationContextAware ;
import org.springframework.stereotype.Component ;

import com.fasterxml.jackson.core.JsonProcessingException ;
import com.fasterxml.jackson.databind.ObjectMapper ;

import bill.zeacc.salieri.fifthgraph.agents.switchboard.SwitchboardState ;
import bill.zeacc.salieri.fifthgraph.agents.switchboard.model.SwitchboardResult ;
import bill.zeacc.salieri.fifthgraph.model.meta.AgentDescriptor ;
import bill.zeacc.salieri.fifthgraph.service.GraphService ;
import lombok.extern.slf4j.Slf4j ;

@Component
@Slf4j
public class SwitchboardAnalysisNode implements NodeAction <SwitchboardState>, ApplicationContextAware {

	@Autowired
	private ChatModel chatModel ;
	@Autowired
	private ObjectMapper om ;

	private SystemMessage systemPrompt ;
	private ApplicationContext applicationContext ;

	@Override
	public Map <String, Object> apply ( SwitchboardState state ) {
		String query = state.getQuery ( ) ;
		log.info ( "Analyzing query to determine best agent: {}", query ) ;

		String strResponse = getChatModel().call ( new Prompt ( getSystemPrompt ( ), new UserMessage ( query ) ) ).getResult ( ).getOutput ( ).getText ( ) ;
		try {
			SwitchboardResult analysisResult = om.readValue ( strResponse, SwitchboardResult.class ) ;
			log.debug ( "Analyzer response: {}", strResponse ) ;
			return Map.of ( SwitchboardState.RECOMMENDED_AGENT_KEY, analysisResult.recommendedAgent ( ),
					SwitchboardState.CONFIDENCE_KEY, analysisResult.confidence ( ) ) ;
		} catch ( JsonProcessingException e ) {
			throw new RuntimeException ( "Failed to parse analysis response: " + strResponse, e ) ;
		}
	}

	// Second workaround for stupid spring circular dependency thing
	private Message getSystemPrompt ( ) {
		if ( systemPrompt == null ) {
			try {
				systemPrompt = buildSystemPrompt ( ) ;
			} catch ( JsonProcessingException e ) {
				throw new RuntimeException ( "Failed to build system prompt", e ) ;
			}
		}
		return systemPrompt ;
	}

	private SystemMessage buildSystemPrompt ( ) throws JsonProcessingException {
		String prompt = """
Here's a list of available agents, with some hints on when to use them, in json format.

%s

Determine which one to use.  If none seem to fit well, use default_agent.

Use ONLY the provided agent names.
Respond ONLY in JSON format:

{"recommendedAgent": "<name of agent from above list>", "confidence": <integer confidence from 0 - 100>}				
				""" ;

		List <AgentDescriptor> agents = graphSvc ( ).listAgents ( ) ;
		String jsonAgents = om.writeValueAsString ( agents ) ;

		prompt = prompt.formatted ( jsonAgents ) ;
		return new SystemMessage ( prompt ) ;
	}

	private GraphService graphSvc ( ) {
		// Workaround for stupid spring circular dependency thing
		return applicationContext.getBean ( GraphService.class ) ;
	}

	@Override
	public void setApplicationContext ( ApplicationContext applicationContext ) throws BeansException {
		this.applicationContext = applicationContext ;
	}

	public void setObjectMapper ( ObjectMapper objectMapper ) {
		this.om = objectMapper ;
	}

	public ChatModel getChatModel ( ) {
		return chatModel;
	}

	public void setChatModel ( ChatModel chatModel ) {
		this.chatModel = chatModel;
	}
}
