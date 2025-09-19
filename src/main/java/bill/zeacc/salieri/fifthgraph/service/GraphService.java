package bill.zeacc.salieri.fifthgraph.service ;

//GraphService.java
import org.bsc.langgraph4j.* ;
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver ;
import org.bsc.langgraph4j.state.AgentState ;
import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.stereotype.Service ;

import bill.zeacc.salieri.fifthgraph.model.meta.AgentDefinition ;
import bill.zeacc.salieri.fifthgraph.model.meta.AgentDescriptor ;
import bill.zeacc.salieri.fifthgraph.model.meta.ChatMsg ;
import bill.zeacc.salieri.fifthgraph.model.states.ResultOrientedState ;
import lombok.extern.slf4j.Slf4j ;
import java.util.* ;

@Service
@Slf4j
public class GraphService {

	private Map <String, AgentDefinition <? extends AgentState>> agentDefs = new HashMap <> ( ) ;
	private Map <String, CompiledGraph <? extends AgentState>> agents = new HashMap <> ( ) ;
	private BaseCheckpointSaver continuityStrategy ;

	@Autowired
	public GraphService ( BaseCheckpointSaver continuityStrategy, List <AgentDefinition <? extends AgentState>> agentDefinitions ) throws GraphStateException {
		this.continuityStrategy = continuityStrategy ;
		for ( AgentDefinition <? extends AgentState> agentDef : agentDefinitions ) {
			agentDefs.put ( agentDef.name ( ), agentDef ) ;

			CompileConfig compileConfig = CompileConfig.builder()
		            .checkpointSaver(this.continuityStrategy)
		            .build() ;
			CompiledGraph <? extends AgentState> compiledGraph = agentDef.graph ( ).compile ( compileConfig ) ;
			agents.put ( agentDef.name ( ), compiledGraph ) ;
			log.info ( "Registered agent: {}", agentDef.name ( ) ) ;
		}
	}

	public List <AgentDescriptor> listAgents ( ) {
		return agentDefs.values ( ).stream ( )
				.map ( AgentDefinition::toDescriptor )
				.toList ( ) ;
	}

	public ResultOrientedState processQuery ( String agentName, String query, String sessionId ) {
		CompiledGraph <? extends AgentState> agent = agents.get ( agentName ) ;
		if ( agent == null ) {
			throw new IllegalArgumentException ( "No agent found with name: " + agentName ) ;
		}
		GraphInput input = new GraphArgs ( Map.of ( "messages", List.of ( new ChatMsg ( ChatMsg.Role.USER, query ) ), "query", query ) ) ;

		ResultOrientedState finalState = null ;
		RunnableConfig runCfg = RunnableConfig.builder()
				.threadId ( sessionId )        // <-- reuse this for the same conversation
				.build ( ) ;
		try {
			for ( NodeOutput <? extends AgentState> output : agent.stream ( input, runCfg ) ) {
				log.debug ( "Node: {}, Messages: {}", output.node ( ), output.state ( ).value ( "messages" ) ) ;
				finalState = ( ResultOrientedState ) output.state ( ) ;
			}
		} catch ( Exception e ) {
			log.error ( "Error processing query", e ) ;
			throw new RuntimeException ( "Error processing query: " + e.getMessage ( ), e ) ;
		}

		if ( finalState != null ) {
			return finalState ;
		}

		throw new RuntimeException ( "No response generated" ) ;
	}
}
