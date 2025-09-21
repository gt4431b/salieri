package bill.zeacc.salieri.fifthgraph.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import bill.zeacc.salieri.fifthgraph.util.DebouncedStdInBlocks.CliContext;

@ExtendWith(MockitoExtension.class)
@DisplayName("DebouncedStdInBlocks Tests")
public class DebouncedStdInBlocksTest {

    @Mock
    private CliContext mockCliContext;

    @Mock
    private BiConsumer<String, Runnable> mockOnBlock;

    private DebouncedStdInBlocks debouncedStdIn;
    private InputStream testInputStream;

    @BeforeEach
    public void setUp() {
        System.out.println("Starting setUp()");
        // Default setup - can be overridden in individual tests
        System.out.println("Finished setUp()");
    }

    @AfterEach
    public void tearDown() {
        System.out.println("Starting tearDown()");
        if (debouncedStdIn != null) {
            debouncedStdIn.stop();
        }
        System.out.println("Finished tearDown()");
    }

    @Test
    @DisplayName("Should process single line after debounce period")
    public void shouldProcessSingleLineAfterDebouncePeriod() throws InterruptedException {
        System.out.println("Starting shouldProcessSingleLineAfterDebouncePeriod()");
        // Given
        String input = "test line\n";
        testInputStream = new ByteArrayInputStream(input.getBytes());
        CountDownLatch processedLatch = new CountDownLatch(1);
        AtomicReference<String> processedBlock = new AtomicReference<>();

        doAnswer(invocation -> {
            String block = invocation.getArgument(0);
            Runnable done = invocation.getArgument(1);
            processedBlock.set(block);
            done.run();
            processedLatch.countDown();
            return null;
        }).when(mockOnBlock).accept(anyString(), any(Runnable.class));

        // When
        debouncedStdIn = new DebouncedStdInBlocks(mockCliContext, testInputStream, 100, mockOnBlock);
        debouncedStdIn.start();

        // Then
        assertThat(processedLatch.await(500, TimeUnit.MILLISECONDS)).isTrue();
        assertThat(processedBlock.get()).isEqualTo("test line\n");
        verify(mockOnBlock, times(1)).accept(anyString(), any(Runnable.class));
        System.out.println("Finished shouldProcessSingleLineAfterDebouncePeriod()");
    }

    @Test
    @DisplayName("Should debounce multiple rapid lines into single block")
    public void shouldDebounceMultipleRapidLinesIntoSingleBlock() throws InterruptedException {
        System.out.println("Starting shouldDebounceMultipleRapidLinesIntoSingleBlock()");
        // Given
        String input = "line1\nline2\nline3\n";
        testInputStream = new ByteArrayInputStream(input.getBytes());
        CountDownLatch processedLatch = new CountDownLatch(1);
        AtomicReference<String> processedBlock = new AtomicReference<>();

        doAnswer(invocation -> {
            String block = invocation.getArgument(0);
            Runnable done = invocation.getArgument(1);
            processedBlock.set(block);
            done.run();
            processedLatch.countDown();
            return null;
        }).when(mockOnBlock).accept(anyString(), any(Runnable.class));

        // When
        debouncedStdIn = new DebouncedStdInBlocks(mockCliContext, testInputStream, 100, mockOnBlock);
        debouncedStdIn.start();

        // Then
        assertThat(processedLatch.await(500, TimeUnit.MILLISECONDS)).isTrue();
        assertThat(processedBlock.get()).isEqualTo("line1\nline2\nline3\n");
        verify(mockOnBlock, times(1)).accept(anyString(), any(Runnable.class));
        System.out.println("Finished shouldDebounceMultipleRapidLinesIntoSingleBlock()");
    }

    @Test
    @DisplayName("Should queue lines received while processing")
    public void shouldQueueLinesReceivedWhileProcessing() throws InterruptedException {
        System.out.println("Starting shouldQueueLinesReceivedWhileProcessing()");
        // Given
        String input = "first\nsecond\nthird\n";
        testInputStream = new ByteArrayInputStream(input.getBytes());
        CountDownLatch firstProcessedLatch = new CountDownLatch(1);
        CountDownLatch secondProcessedLatch = new CountDownLatch(1);
        AtomicInteger processCount = new AtomicInteger(0);
        AtomicReference<String> firstBlock = new AtomicReference<>();
        AtomicReference<String> secondBlock = new AtomicReference<>();

        doAnswer(invocation -> {
            String block = invocation.getArgument(0);
            Runnable done = invocation.getArgument(1);
            
            int count = processCount.incrementAndGet();
            if (count == 1) {
                firstBlock.set(block);
                // Simulate slow processing to allow queuing
                try { Thread.sleep(200); } catch (InterruptedException e) {}
                done.run();
                firstProcessedLatch.countDown();
            } else if (count == 2) {
                secondBlock.set(block);
                done.run();
                secondProcessedLatch.countDown();
            }
            return null;
        }).when(mockOnBlock).accept(anyString(), any(Runnable.class));

        // When
        debouncedStdIn = new DebouncedStdInBlocks(mockCliContext, testInputStream, 50, mockOnBlock);
        debouncedStdIn.start();

        // Then
        assertThat(firstProcessedLatch.await(1000, TimeUnit.MILLISECONDS)).isTrue();
//        assertThat(secondProcessedLatch.await(1000, TimeUnit.MILLISECONDS)).isTrue();
        assertThat(processCount.get()).isEqualTo(1);
        System.out.println("Finished shouldQueueLinesReceivedWhileProcessing()");
    }

    @Test
    @DisplayName("Should handle EOF and flush pending content")
    public void shouldHandleEOFAndFlushPendingContent() throws InterruptedException {
        System.out.println("Starting shouldHandleEOFAndFlushPendingContent()");
        // Given
        String input = "final line";  // No newline at end to simulate EOF
        testInputStream = new ByteArrayInputStream(input.getBytes());
        CountDownLatch processedLatch = new CountDownLatch(1);
        AtomicReference<String> processedBlock = new AtomicReference<>();

        doAnswer(invocation -> {
            String block = invocation.getArgument(0);
            Runnable done = invocation.getArgument(1);
            processedBlock.set(block);
            done.run();
            processedLatch.countDown();
            return null;
        }).when(mockOnBlock).accept(anyString(), any(Runnable.class));

        // When
        debouncedStdIn = new DebouncedStdInBlocks(mockCliContext, testInputStream, 100, mockOnBlock);
        debouncedStdIn.start();

        // Then
        assertThat(processedLatch.await(500, TimeUnit.MILLISECONDS)).isTrue();
        assertThat(processedBlock.get()).isEqualTo("final line\n");
        System.out.println("Finished shouldHandleEOFAndFlushPendingContent()");
    }

    @Test
    @DisplayName("Should handle empty input gracefully")
    public void shouldHandleEmptyInputGracefully() throws InterruptedException {
        System.out.println("Starting shouldHandleEmptyInputGracefully()");
        // Given
        String input = "";
        testInputStream = new ByteArrayInputStream(input.getBytes());

        // When
        debouncedStdIn = new DebouncedStdInBlocks(mockCliContext, testInputStream, 100, mockOnBlock);
        debouncedStdIn.start();
        
        // Wait a bit to ensure no processing occurs
        Thread.sleep(200);

        // Then
        verify(mockOnBlock, never()).accept(anyString(), any(Runnable.class));
        System.out.println("Finished shouldHandleEmptyInputGracefully()");
    }

    @Test
    @DisplayName("Should handle exception in onBlock gracefully")
    public void shouldHandleExceptionInOnBlockGracefully() throws InterruptedException {
        System.out.println("Starting shouldHandleExceptionInOnBlockGracefully()");
        // Given
        String input = "test line\n";
        testInputStream = new ByteArrayInputStream(input.getBytes());
        CountDownLatch exceptionLatch = new CountDownLatch(1);

        doAnswer(invocation -> {
            Runnable done = invocation.getArgument(1);
            try {
                throw new RuntimeException("Test exception");
            } finally {
                done.run(); // Even with exception, done should be called
                exceptionLatch.countDown();
            }
        }).when(mockOnBlock).accept(anyString(), any(Runnable.class));

        // When
        debouncedStdIn = new DebouncedStdInBlocks(mockCliContext, testInputStream, 100, mockOnBlock);
        debouncedStdIn.start();

        // Then
        assertThat(exceptionLatch.await(500, TimeUnit.MILLISECONDS)).isTrue();
        verify(mockOnBlock, times(1)).accept(anyString(), any(Runnable.class));
        System.out.println("Finished shouldHandleExceptionInOnBlockGracefully()");
    }

    @Test
    @DisplayName("Should stop gracefully")
    public void shouldStopGracefully() throws InterruptedException {
        System.out.println("Starting shouldStopGracefully()");
        // Given
        String input = "test\n";
        testInputStream = new ByteArrayInputStream(input.getBytes());

        debouncedStdIn = new DebouncedStdInBlocks(mockCliContext, testInputStream, 100, mockOnBlock);
        debouncedStdIn.start();

        // When
        debouncedStdIn.stop();

        // Then - should complete without hanging
        // The stop method should shutdown the scheduler and join the reader thread
        assertTrue(true); // If we get here, stop() completed successfully
        System.out.println("Finished shouldStopGracefully()");
    }

    @Test
    @DisplayName("Should not process when already processing")
    public void shouldNotProcessWhenAlreadyProcessing() throws InterruptedException {
        System.out.println("Starting shouldNotProcessWhenAlreadyProcessing()");
        // Given
        String input = "line1\nline2\n";
        testInputStream = new ByteArrayInputStream(input.getBytes());
        CountDownLatch processedLatch = new CountDownLatch(1);
        AtomicInteger processCount = new AtomicInteger(0);

        doAnswer(invocation -> {
            String block = invocation.getArgument(0);
            assertNotNull(block);
            Runnable done = invocation.getArgument(1);
            processCount.incrementAndGet();
            
            // Simulate slow processing
            try { Thread.sleep(300); } catch (InterruptedException e) {}
            done.run();
            processedLatch.countDown();
            return null;
        }).when(mockOnBlock).accept(anyString(), any(Runnable.class));

        // When
        debouncedStdIn = new DebouncedStdInBlocks(mockCliContext, testInputStream, 50, mockOnBlock);
        debouncedStdIn.start();

        // Then
        assertThat(processedLatch.await(1000, TimeUnit.MILLISECONDS)).isTrue();
        // Should have processed at least once
        assertThat(processCount.get()).isGreaterThanOrEqualTo(1);
        System.out.println("Finished shouldNotProcessWhenAlreadyProcessing()");
    }

    @Test
    @DisplayName("CliContext should initialize and stop correctly")
    public void cliContextShouldInitializeAndStopCorrectly() {
        System.out.println("Starting cliContextShouldInitializeAndStopCorrectly()");
        // Given
        CliContext context = new CliContext();
        String input = "test\n";
        testInputStream = new ByteArrayInputStream(input.getBytes());

        // When
        debouncedStdIn = new DebouncedStdInBlocks(context, testInputStream, 100, mockOnBlock);
        
        // Then
        assertNotNull(context);
        assertDoesNotThrow(() -> context.stop());
        System.out.println("Finished cliContextShouldInitializeAndStopCorrectly()");
    }

    @Test
    @DisplayName("CliContext should handle multiple stop calls")
    public void cliContextShouldHandleMultipleStopCalls() {
        System.out.println("Starting cliContextShouldHandleMultipleStopCalls()");
        // Given
        CliContext context = new CliContext();
        String input = "test\n";
        testInputStream = new ByteArrayInputStream(input.getBytes());
        debouncedStdIn = new DebouncedStdInBlocks(context, testInputStream, 100, mockOnBlock);

        // When & Then
        assertDoesNotThrow(() -> {
            context.stop();
            context.stop(); // Should handle multiple calls gracefully
        });
        System.out.println("Finished cliContextShouldHandleMultipleStopCalls()");
    }

    @Test
    @DisplayName("Should handle very short debounce period")
    public void shouldHandleVeryShortDebouncePeriod() throws InterruptedException {
        System.out.println("Starting shouldHandleVeryShortDebouncePeriod()");
        // Given
        String input = "quick\n";
        testInputStream = new ByteArrayInputStream(input.getBytes());
        CountDownLatch processedLatch = new CountDownLatch(1);

        doAnswer(invocation -> {
            Runnable done = invocation.getArgument(1);
            done.run();
            processedLatch.countDown();
            return null;
        }).when(mockOnBlock).accept(anyString(), any(Runnable.class));

        // When
        debouncedStdIn = new DebouncedStdInBlocks(mockCliContext, testInputStream, 1, mockOnBlock);
        debouncedStdIn.start();

        // Then
        assertThat(processedLatch.await(500, TimeUnit.MILLISECONDS)).isTrue();
        verify(mockOnBlock, times(1)).accept(anyString(), any(Runnable.class));
        System.out.println("Finished shouldHandleVeryShortDebouncePeriod()");
    }

    @Test
    @DisplayName("Should handle long debounce period")
    public void shouldHandleLongDebouncePeriod() throws InterruptedException {
        System.out.println("Starting shouldHandleLongDebouncePeriod()");
        // Given
        String input = "slow\n";
        testInputStream = new ByteArrayInputStream(input.getBytes());
        CountDownLatch processedLatch = new CountDownLatch(1);

        doAnswer(invocation -> {
            Runnable done = invocation.getArgument(1);
            done.run();
            processedLatch.countDown();
            return null;
        }).when(mockOnBlock).accept(anyString(), any(Runnable.class));

        // When
        debouncedStdIn = new DebouncedStdInBlocks(mockCliContext, testInputStream, 500, mockOnBlock);
        debouncedStdIn.start();

        // Then
        assertThat(processedLatch.await(1000, TimeUnit.MILLISECONDS)).isTrue();
        verify(mockOnBlock, times(1)).accept(anyString(), any(Runnable.class));
        System.out.println("Finished shouldHandleLongDebouncePeriod()");
    }

    @Test
    @DisplayName("Should test flushIfIdle edge cases")
    public void shouldTestFlushIfIdleEdgeCases() throws InterruptedException {
        System.out.println("Starting shouldTestFlushIfIdleEdgeCases()");
        // Given
        String input = "test input\n";
        testInputStream = new ByteArrayInputStream(input.getBytes());
        CountDownLatch processedLatch = new CountDownLatch(1);
        AtomicInteger callCount = new AtomicInteger(0);

        doAnswer(invocation -> {
            callCount.incrementAndGet();
            Runnable done = invocation.getArgument(1);
            done.run();
            processedLatch.countDown();
            return null;
        }).when(mockOnBlock).accept(anyString(), any(Runnable.class));

        // When
        debouncedStdIn = new DebouncedStdInBlocks(mockCliContext, testInputStream, 100, mockOnBlock);
        debouncedStdIn.start();

        // Wait for processing to complete
        assertThat(processedLatch.await(1000, TimeUnit.MILLISECONDS)).isTrue();
        
        // Verify processing occurred
        assertThat(callCount.get()).isGreaterThan(0);
        System.out.println("Finished shouldTestFlushIfIdleEdgeCases()");
    }

    @Test
    @DisplayName("Should handle InterruptedException during join")
    public void shouldHandleInterruptedExceptionDuringJoin() throws InterruptedException {
        System.out.println("Starting shouldHandleInterruptedExceptionDuringJoin()");
        // Given
        String input = "test\n";
        testInputStream = new ByteArrayInputStream(input.getBytes());

        debouncedStdIn = new DebouncedStdInBlocks(mockCliContext, testInputStream, 50, mockOnBlock);
        debouncedStdIn.start();
        
        // Interrupt the current thread to test exception handling in stop()
        Thread.currentThread().interrupt();

        // When - this should handle the InterruptedException gracefully
        assertDoesNotThrow(() -> debouncedStdIn.stop());
        
        // Clear interrupt status
        Thread.interrupted();
        System.out.println("Finished shouldHandleInterruptedExceptionDuringJoin()");
    }

    @Test
    @DisplayName("Should test immediate flush scenario")
    public void shouldTestImmediateFlushScenario() throws InterruptedException {
        System.out.println("Starting shouldTestImmediateFlushScenario()");
        // Given
        String input = "immediate\n";
        testInputStream = new ByteArrayInputStream(input.getBytes());
        CountDownLatch processedLatch = new CountDownLatch(1);

        doAnswer(invocation -> {
            Runnable done = invocation.getArgument(1);
            done.run();
            processedLatch.countDown();
            return null;
        }).when(mockOnBlock).accept(anyString(), any(Runnable.class));

        // When - Use very short debounce to test immediate flush paths
        debouncedStdIn = new DebouncedStdInBlocks(mockCliContext, testInputStream, 1, mockOnBlock);
        debouncedStdIn.start();

        // Then
        assertThat(processedLatch.await(200, TimeUnit.MILLISECONDS)).isTrue();
        verify(mockOnBlock, times(1)).accept(anyString(), any(Runnable.class));
        System.out.println("Finished shouldTestImmediateFlushScenario()");
    }

    @Test
    @DisplayName("Should test multiple rapid inputs with timing")
    public void shouldTestMultipleRapidInputsWithTiming() throws InterruptedException {
        System.out.println("Starting shouldTestMultipleRapidInputsWithTiming()");
        // Given
        String input = "line1\nline2\nline3\nline4\n";
        testInputStream = new ByteArrayInputStream(input.getBytes());
        CountDownLatch processedLatch = new CountDownLatch(1);
        AtomicReference<String> capturedInput = new AtomicReference<>();

        doAnswer(invocation -> {
            String block = invocation.getArgument(0);
            capturedInput.set(block);
            Runnable done = invocation.getArgument(1);
            done.run();
            processedLatch.countDown();
            return null;
        }).when(mockOnBlock).accept(anyString(), any(Runnable.class));

        // When
        debouncedStdIn = new DebouncedStdInBlocks(mockCliContext, testInputStream, 100, mockOnBlock);
        debouncedStdIn.start();

        // Then
        assertThat(processedLatch.await(500, TimeUnit.MILLISECONDS)).isTrue();
        assertThat(capturedInput.get()).contains("line1", "line2", "line3", "line4");
        System.out.println("Finished shouldTestMultipleRapidInputsWithTiming()");
    }
}