package bill.zeacc.salieri.fifthgraph;

//Salieri.java - Main application class
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import com.fasterxml.jackson.databind.ObjectMapper;

import bill.zeacc.salieri.fifthgraph.config.LLMProperties;
import bill.zeacc.salieri.fifthgraph.service.InputHandler;
import bill.zeacc.salieri.fifthgraph.service.ResponseFormatter;
import bill.zeacc.salieri.fifthgraph.service.SessionManager;
import bill.zeacc.salieri.fifthgraph.util.DebouncedStdInBlocks;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableConfigurationProperties(LLMProperties.class)
@Slf4j
public class Salieri {

    public static void main(String[] args) {
        SpringApplication.run(Salieri.class, args);
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner commandLineRunner(
            InputHandler inputHandler,
            ResponseFormatter responseFormatter,
            SessionManager sessionManager,
            ObjectMapper om) {
        
        // Create session and display welcome message
        String sessionId = sessionManager.createSession();
        System.out.println(responseFormatter.formatWelcome());
        
        return args -> {
            DebouncedStdInBlocks.CliContext ctx = new DebouncedStdInBlocks.CliContext();
            DebouncedStdInBlocks reader = new DebouncedStdInBlocks(ctx, System.in, 2000, (input, done) -> {
                try {
                    inputHandler.handleUserInput(input, sessionId, ctx);
                } finally {
                    done.run(); // IMPORTANT: signal ready for the next block
                }
            });
            reader.start();
            try { 
                Thread.currentThread().join(); 
            } catch (InterruptedException ignored) {
                sessionManager.terminateSession(sessionId);
            }
        };
    }
}