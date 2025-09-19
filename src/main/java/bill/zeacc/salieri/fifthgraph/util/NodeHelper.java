package bill.zeacc.salieri.fifthgraph.util;

import java.util.concurrent.CompletableFuture ;

import org.bsc.langgraph4j.action.AsyncNodeAction ;
import org.bsc.langgraph4j.action.NodeAction ;
import org.bsc.langgraph4j.state.AgentState ;

public class NodeHelper {
	public static <T extends AgentState, S extends T> AsyncNodeAction <S> toAsync ( NodeAction <T> action ) {
		return ( state ) -> {
			try {
				return CompletableFuture.completedFuture ( action.apply ( state ) ) ;
			} catch ( Exception e ) {
				e.printStackTrace();
				throw new RuntimeException ( e ) ;
			}
		} ;
	}
}
