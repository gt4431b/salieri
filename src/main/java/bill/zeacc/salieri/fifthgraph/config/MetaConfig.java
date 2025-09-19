package bill.zeacc.salieri.fifthgraph.config;

import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver ;
import org.bsc.langgraph4j.checkpoint.MemorySaver ;
import org.springframework.context.annotation.Bean ;
import org.springframework.context.annotation.Configuration ;

@Configuration
public class MetaConfig {

	@Bean
	public BaseCheckpointSaver continuityStrategy ( ) {
		return new MemorySaver ( ) ;

	}
}
