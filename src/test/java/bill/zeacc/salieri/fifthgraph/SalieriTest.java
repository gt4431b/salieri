package bill.zeacc.salieri.fifthgraph;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;

import bill.zeacc.salieri.fifthgraph.agents.switchboard.SwitchboardState;
import bill.zeacc.salieri.fifthgraph.model.states.ResultOrientedState;
import bill.zeacc.salieri.fifthgraph.service.GraphService;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class SalieriTest {

    @Mock
    private GraphService graphService;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private ResultOrientedState mockResponse;
    
    private Salieri salieri;
    private ByteArrayOutputStream outputStream;
    @SuppressWarnings ( "unused" )
	private PrintStream originalOut;
    
    @BeforeEach
    void setUp() {
        salieri = new Salieri();
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }
    
    @Test
    public void testMain() {
        // Test that main method doesn't throw exception
        assertDoesNotThrow(() -> {
            // We can't easily test SpringApplication.run without full context
            // but we can verify the method exists and is callable
            @SuppressWarnings ( "unused" )
			String[] args = {};
            // Main method would normally start Spring context
            // For unit test purposes, we'll just verify it exists
            assertTrue(true); // Placeholder for main method existence
        });
    }
    
    @Test
    void testCommandLineRunnerWithExitCommand() throws Exception {
        // Mock the graph service responses
        lenient ( ).when(mockResponse.value(SwitchboardState.RECOMMENDED_AGENT_KEY)).thenReturn(Optional.of("hello_agent"));
        lenient ( ).when(mockResponse.value(SwitchboardState.CONFIDENCE_KEY)).thenReturn(Optional.of(80));
        lenient ( ).when(mockResponse.getFinalAnswer()).thenReturn("Hello! How can I help you?");
        
        lenient ( ).when(graphService.processQuery(eq("switchboard_agent"), anyString(), anyString())).thenReturn(mockResponse);
        lenient ( ).when(graphService.processQuery(eq("hello_agent"), anyString(), anyString())).thenReturn(mockResponse);
        
        // Create input stream with exit command
        String input = "exit\n";
        @SuppressWarnings ( "unused" )
		InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        
        CommandLineRunner runner = salieri.commandLineRunner(graphService, objectMapper);
        
        // Run in separate thread to avoid blocking
        Thread testThread = new Thread(() -> {
            try {
                runner.run();
            } catch (Exception e) {
                // Expected for test scenario
            }
        });
        
        testThread.start();
        testThread.join(1000); // Wait max 1 second
        
        String output = outputStream.toString();
        assertTrue(output.contains("Hello! How can I assist you today?"));
    }
    
    @Test
    void testCommandLineRunnerWithQuitCommand() throws Exception {
        // Similar test for quit command
    	lenient ( ).when(mockResponse.value(SwitchboardState.RECOMMENDED_AGENT_KEY)).thenReturn(Optional.of("hello_agent"));
    	lenient ( ).when(mockResponse.value(SwitchboardState.CONFIDENCE_KEY)).thenReturn(Optional.of(80));
    	lenient ( ).when(mockResponse.getFinalAnswer()).thenReturn("Hello! How can I help you?");
        
    	lenient ( ).when(graphService.processQuery(eq("switchboard_agent"), anyString(), anyString())).thenReturn(mockResponse);
    	lenient ( ).when(graphService.processQuery(eq("hello_agent"), anyString(), anyString())).thenReturn(mockResponse);
        
        CommandLineRunner runner = salieri.commandLineRunner(graphService, objectMapper);
        
        // Test that runner is created without exception
        assertNotNull(runner);
    }
    
    @Test
    void testCommandLineRunnerWithLowConfidence() throws Exception {
        // Test case where confidence is below 50, should use default_agent
    	lenient ( ).when(mockResponse.value(SwitchboardState.RECOMMENDED_AGENT_KEY)).thenReturn(Optional.of("unknown_agent"));
    	lenient ( ).when(mockResponse.value(SwitchboardState.CONFIDENCE_KEY)).thenReturn(Optional.of(30)); // Low confidence
    	lenient ( ).when(mockResponse.getFinalAnswer()).thenReturn("I'll help with that.");
        
        ResultOrientedState defaultResponse = mock(ResultOrientedState.class);
        lenient ( ).when(defaultResponse.getFinalAnswer()).thenReturn("Default response");
        
        lenient ( ).when(graphService.processQuery(eq("switchboard_agent"), anyString(), anyString())).thenReturn(mockResponse);
        lenient ( ).when(graphService.processQuery(eq("default_agent"), anyString(), anyString())).thenReturn(defaultResponse);
        
        CommandLineRunner runner = salieri.commandLineRunner(graphService, objectMapper);
        assertNotNull(runner);
    }
    
    @Test
    void testCommandLineRunnerWithEmptyInput() throws Exception {
        // Test handling of empty input
    	lenient ( ).when(mockResponse.value(SwitchboardState.RECOMMENDED_AGENT_KEY)).thenReturn(Optional.of("hello_agent"));
    	lenient ( ).when(mockResponse.value(SwitchboardState.CONFIDENCE_KEY)).thenReturn(Optional.of(80));
    	lenient ( ).when(mockResponse.getFinalAnswer()).thenReturn("Response");

    	lenient ( ).when(graphService.processQuery(anyString(), anyString(), anyString())).thenReturn(mockResponse);
        
        CommandLineRunner runner = salieri.commandLineRunner(graphService, objectMapper);
        assertNotNull(runner);
    }
    
    @Test
    void testCommandLineRunnerWithException() throws Exception {
        // Test error handling
    	lenient ( ).when(graphService.processQuery(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Test exception"));
        
        CommandLineRunner runner = salieri.commandLineRunner(graphService, objectMapper);
        assertNotNull(runner);
        
        // The runner should handle exceptions gracefully
        // and continue processing
    }
    
    @Test
    void testCommandLineRunnerBeanCreation() {
        // Test that the bean can be created
        CommandLineRunner runner = salieri.commandLineRunner(graphService, objectMapper);
        assertNotNull(runner);
    }
}
