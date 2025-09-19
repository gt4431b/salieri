package bill.zeacc.salieri.fifthgraph.agents.hello.config;

import java.util.List ;

import org.springframework.context.annotation.Bean ;
import org.springframework.context.annotation.Configuration ;

import bill.zeacc.salieri.fifthgraph.model.meta.InternalTool ;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolChooser ;

@Configuration
public class HelloToolsConfig {

	@Bean
	public ToolChooser helloTools ( List <InternalTool> allTools ) {
		return ( ) -> allTools.stream ( )
				.filter ( t -> t.getClass ( ).isAnnotationPresent ( HelloTool.class ) )
				.toList ( ) ;
	}
}
