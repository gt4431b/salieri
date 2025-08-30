package bill.zeacc.salieri.fourthgraph ;

//LangGraphCliApplication.java - Main application class
import org.springframework.boot.CommandLineRunner ;
import org.springframework.boot.SpringApplication ;
import org.springframework.boot.autoconfigure.SpringBootApplication ;
import org.springframework.boot.context.properties.EnableConfigurationProperties ;
import org.springframework.context.annotation.Bean ;
import java.util.Scanner ;
import lombok.extern.slf4j.Slf4j ;

@SpringBootApplication
@EnableConfigurationProperties ( LLMProperties.class )
@Slf4j
public class LangGraphCliApplication {

	public static void main ( String [ ] args ) {
		SpringApplication.run ( LangGraphCliApplication.class, args ) ;
	}

	@Bean
	public CommandLineRunner commandLineRunner ( GraphService graphService ) {
		return args -> {
			Scanner scanner = new Scanner ( System.in ) ;

			System.out.println ( "=================================" ) ;
			System.out.println ( "LangGraph CLI Chat" ) ;
			System.out.println ( "Type 'exit' or 'quit' to end" ) ;
			System.out.println ( "=================================\n" ) ;

			while ( true ) {
				System.out.print ( "You: " ) ;
				String input = scanner.nextLine ( ) ;

				if ( input.equalsIgnoreCase ( "exit" ) || input.equalsIgnoreCase ( "quit" ) ) {
					System.out.println ( "\nGoodbye!" ) ;
					break ;
				}

				if ( input.trim ( ).isEmpty ( ) ) {
					continue ;
				}

				try {
					String response = graphService.processQuery ( input ) ;
					System.out.println ( "\nAssistant: " + response + "\n" ) ;
				} catch ( Exception e ) {
					log.error ( "Error processing query", e ) ;
					System.err.println ( "Error: " + e.getMessage ( ) + "\n" ) ;
				}
			}

			scanner.close ( ) ;
			System.exit ( 0 ) ;
		} ;
	}
}
