package bill.zeacc.salieri.fifthgraph.config;

import org.springframework.context.annotation.Configuration ;

@Configuration
public class NodeConfig {


	/*
	 * Implementation note: We have two different kinds of node configurations possible.  Either
	 * we can wire them up using @Component if they are specific enough to warrant a singleton,
	 * or else we can implement them from abstract, highly configurable base nodes and wire them
	 * up here using @Bean methods.  In either case we should use these definitions in conjunction
	 * with @Qualifier annotations in the GraphConfig class to pick the right nodes for each graph.
	 */
}
