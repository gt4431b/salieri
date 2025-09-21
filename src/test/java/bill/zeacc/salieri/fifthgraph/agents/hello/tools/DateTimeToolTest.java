package bill.zeacc.salieri.fifthgraph.agents.hello.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import bill.zeacc.salieri.fifthgraph.model.meta.ToolResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("DateTimeTool Tests")
public class DateTimeToolTest {

    private DateTimeTool dateTimeTool;

    @BeforeEach
    void setUp() throws Exception {
        dateTimeTool = new DateTimeTool();
        dateTimeTool.afterPropertiesSet ( ) ;
    }

    @Test
    @DisplayName("Should return correct tool name")
    void shouldReturnCorrectToolName() {
        // When
        String name = dateTimeTool.getName();

        // Then
        assertThat(name).isEqualTo("getDateTime");
    }

    @Test
    @DisplayName("Should return correct tool description")
    void shouldReturnCorrectToolDescription() {
        // When
        String description = dateTimeTool.getDescription();

        // Then
        assertThat(description).isEqualTo("Gets current date and time.  No parameters.");
    }

    @Test
    @DisplayName("Should return correct execution spec")
    void shouldReturnCorrectExecutionSpec() {
        // When
        String spec = dateTimeTool.executionSpec();

        // Then
        assertThat(spec).contains("getDateTime")
                       .contains("args")
                       .contains("[]");
    }

    @Test
    @DisplayName("Should execute and return current date time in RFC format")
    void shouldExecuteAndReturnCurrentDateTime() {
        // Given
        String arguments = "{}";
        ZonedDateTime beforeExecution = ZonedDateTime.now();

        // When
        ToolResponse response = dateTimeTool.execute(arguments);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOutput()).isNotNull();
        assertThat(response.getOutput()).isNotEmpty();
        
        // Verify it's a valid RFC 1123 date format
        ZonedDateTime parsedDate = ZonedDateTime.parse(response.getOutput(), DateTimeFormatter.RFC_1123_DATE_TIME);
        assertThat(parsedDate).isAfter(beforeExecution.minusMinutes(1));
        assertThat(parsedDate).isBefore(ZonedDateTime.now().plusMinutes(1));
    }

    @Test
    @DisplayName("Should handle empty args JSON")
    void shouldHandleEmptyArgsJson() {
        // Given
        String emptyArgs = "{}";

        // When
        ToolResponse response = dateTimeTool.execute(emptyArgs);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOutput()).isNotNull();
        assertThat(response.getOutput()).isNotEmpty();
    }

    @Test
    @DisplayName("Should handle invalid JSON gracefully")
    void shouldHandleInvalidJsonGracefully() {
        // Given
        String invalidJson = "not valid json";

        // When
        ToolResponse response = dateTimeTool.execute(invalidJson);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOutput()).contains("Error:");
    }

    @Test
    @DisplayName("Should be consistent across multiple calls")
    void shouldBeConsistentAcrossMultipleCalls() {
        // Given
        String arguments = "{}";

        // When
        ToolResponse response1 = dateTimeTool.execute(arguments);
        ToolResponse response2 = dateTimeTool.execute(arguments);

        // Then
        assertThat(response1).isNotNull();
        assertThat(response2).isNotNull();
        // Results should be close in time (within same minute typically)
        ZonedDateTime time1 = ZonedDateTime.parse(response1.getOutput(), DateTimeFormatter.RFC_1123_DATE_TIME);
        ZonedDateTime time2 = ZonedDateTime.parse(response2.getOutput(), DateTimeFormatter.RFC_1123_DATE_TIME);
        assertThat(time2).isAfterOrEqualTo(time1);
    }
}