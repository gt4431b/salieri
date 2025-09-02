package bill.zeacc.salieri.fourthgraph ;

//LangGraphCliApplication.java - Main application class
import org.springframework.boot.CommandLineRunner ;
import org.springframework.boot.SpringApplication ;
import org.springframework.boot.autoconfigure.SpringBootApplication ;
import org.springframework.boot.context.properties.EnableConfigurationProperties ;
import org.springframework.context.annotation.Bean ;
import org.springframework.context.annotation.Profile ;

import lombok.extern.slf4j.Slf4j ;

@SpringBootApplication
@EnableConfigurationProperties ( LLMProperties.class )
@Slf4j
public class LangGraphCliApplication {

	public static void main ( String [ ] args ) {
		SpringApplication.run ( LangGraphCliApplication.class, args ) ;
	}

	@Bean
	@Profile("!test")
	public CommandLineRunner commandLineRunner ( GraphService graphService ) {
		return args -> {
			DebouncedStdInBlocks.CliContext ctx = new DebouncedStdInBlocks.CliContext ( ) ;
			DebouncedStdInBlocks reader = new DebouncedStdInBlocks ( ctx, System.in, 2000, (input, done) -> {
	            try {
					input = input.trim ( ) ;
					if ( input.equalsIgnoreCase ( "exit" ) || input.equalsIgnoreCase ( "quit" ) ) {
						System.out.println ( "\nGoodbye!" ) ;
						ctx.stop ( ) ;
					}

					if ( input.trim ( ).isEmpty ( ) ) {
						return ;
					}

					try {
						String response = graphService.processQuery ( input ) ;
						System.out.println ( "\nAssistant: " + response + "\n" ) ;
					} catch ( Exception e ) {
						log.error ( "Error processing query", e ) ;
						System.err.println ( "Error: " + e.getMessage ( ) + "\n" ) ;
					}
	            } finally {
	                done.run(); // IMPORTANT: signal ready for the next block
	            }
	        });
			reader.start();
	        try { Thread.currentThread().join(); } catch (InterruptedException ignored) {}
		} ;
	}
}
