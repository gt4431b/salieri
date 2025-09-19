package bill.zeacc.salieri.fifthgraph.config;

import org.springframework.context.annotation.Configuration ;

@Configuration
public class AgentConfig {

	/*
	 * As we're defining "Agent" as a graph, this class should return Graph definitions.  Graph service should then be able to
	 * choose which Agent to use by name.
	 * 
	 * Ideally there should be at least one Agent in here that should be able to pick a subsequent Agent based on user input.
	 *
	 * On second thought let's keep agent config in their own subpackage, but we can have some
	 * "central" agents here, for example an agent to decide which other agent to use.
	 */

}
