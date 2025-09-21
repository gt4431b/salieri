package bill.zeacc.salieri.fifthgraph.model.meta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("BaseInternalTool Tests - Focusing on Branch Coverage")
public class BaseInternalToolTest {

    private TestBaseInternalTool tool;

    @BeforeEach
    protected void setUp() throws Exception {
        tool = new TestBaseInternalTool();
        tool.afterPropertiesSet();
    }

    @Test
    @DisplayName("Should execute successfully with valid arguments")
    public void shouldExecuteSuccessfullyWithValidArguments() {
        // Given
        String validArgs = "{\"param1\":\"value1\",\"param2\":\"value2\"}";

        // When
        ToolResponse response = tool.execute(validArgs);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToolName()).isEqualTo("testTool");
        assertThat(response.getOutput()).isEqualTo("Success with: {param1=value1, param2=value2}");
        assertThat(response.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should handle JSON parsing exception and return error response")
    public void shouldHandleJsonParsingExceptionAndReturnErrorResponse() {
        // Given
        String invalidJson = "invalid json content";

        // When
        ToolResponse response = tool.execute(invalidJson);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToolName()).isEqualTo("testTool");
        assertThat(response.getOutput()).startsWith("Error:");
        assertThat(response.getOutput()).contains("Unrecognized token");
        assertThat(response.getId()).isEqualTo("unknown");
    }

    @Test
    @DisplayName("Should handle missing required arguments and throw exception")
    public void shouldHandleMissingRequiredArgumentsAndThrowException ( ) throws Exception {
        // Given
    	TestBaseInternalToolWithRequiredArgs tool = new TestBaseInternalToolWithRequiredArgs();
        String argsWithoutRequired = "{\"optional\":\"value\"}";
        tool.afterPropertiesSet ( ) ;

        // When / Then
        assertThatThrownBy(() -> {
			tool.execute(argsWithoutRequired);
		}).isInstanceOf(IllegalArgumentException.class)
		  .hasMessageContaining("Missing required argument: required_param");
    }

    @Test
    @DisplayName("Should handle invalid type conversion and throw exception")
    public void shouldHandleInvalidTypeConversionAndThrowException ( ) throws Exception {
        // Given
    	TestBaseInternalToolWithTypeValidation tool = new TestBaseInternalToolWithTypeValidation();
        String argsWithInvalidType = "{\"string_param\":\"value\",\"number_param\":\"not_a_number\"}";
        tool.afterPropertiesSet ( ) ;

        // When / Then
        assertThatThrownBy(() -> tool.execute(argsWithInvalidType))
        	.isInstanceOf(IllegalArgumentException.class)
        	.hasMessageContaining("Invalid type for argument: number_param") ;
    }

    @Test
    @DisplayName("Should handle IOException in doExecute and return error response")
    public void shouldHandleIOExceptionInDoExecuteAndReturnErrorResponse ( ) throws Exception{
        // Given
    	TestBaseInternalToolWithIOException tool = new TestBaseInternalToolWithIOException();
    	tool.afterPropertiesSet ( ) ;
        String validArgs = "{\"param\":\"value\"}";

        // When
        ToolResponse response = tool.execute(validArgs);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOutput()).startsWith("Error:");
        assertThat(response.getOutput()).contains("Simulated IO exception");
    }

    @Test
    @DisplayName("Should not handle empty arguments map")
    public void shouldHandleEmptyArgumentsMap() {
        // Given
        String emptyArgs = "{}";

        // When / Then
        assertThatThrownBy(() -> tool.execute(emptyArgs))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Missing required argument: param1") ;
    }

    @Test
    @DisplayName("Should handle null execution spec during init")
    public void shouldHandleNullExecutionSpecDuringInit() {
        // Given/When/Then
        assertThatThrownBy(() -> {
            TestBaseInternalToolWithNullSpec tool = new TestBaseInternalToolWithNullSpec();
            tool.afterPropertiesSet();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should handle invalid execution spec JSON during init")
    void shouldHandleInvalidExecutionSpecJsonDuringInit() {
        // Given/When/Then
        assertThatThrownBy(() -> {
            TestBaseInternalToolWithInvalidSpec tool = new TestBaseInternalToolWithInvalidSpec();
            tool.afterPropertiesSet();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should handle null arguments array during init")
    public void shouldHandleNullArgumentsArrayDuringInit() throws Exception {
        // Given
        TestBaseInternalToolWithNullArgs tool = new TestBaseInternalToolWithNullArgs();
        
        // When
        tool.afterPropertiesSet();
        ToolResponse response = tool.execute("{}");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOutput()).isEqualTo("Success with null args: {}");
    }

    @Test
    @DisplayName("Should test ToolArgumentSpec type class conversion")
    public void shouldTestToolArgumentSpecTypeClassConversion() throws ClassNotFoundException {
        // Given
        BaseInternalTool.ToolArgumentSpec spec = new BaseInternalTool.ToolArgumentSpec();
        spec.setType("java.lang.String");

        // When
        Class<?> typeClass = spec.getTypeClass();

        // Then
        assertThat(typeClass).isEqualTo(String.class);
    }

    @Test
    @DisplayName("Should handle ClassNotFoundException in ToolArgumentSpec")
    public void shouldHandleClassNotFoundExceptionInToolArgumentSpec() {
        // Given
        BaseInternalTool.ToolArgumentSpec spec = new BaseInternalTool.ToolArgumentSpec();
        spec.setType("non.existent.Class");

        // When/Then
        assertThatThrownBy(() -> spec.getTypeClass())
            .isInstanceOf(ClassNotFoundException.class);
    }

    @Test
    @DisplayName("Should test required argument validation paths")
    public void shouldTestRequiredArgumentValidationPaths() {
        // Given
        BaseInternalTool.ToolArgumentSpec requiredSpec = new BaseInternalTool.ToolArgumentSpec();
        requiredSpec.setRequired(true);
        
        BaseInternalTool.ToolArgumentSpec optionalSpec = new BaseInternalTool.ToolArgumentSpec();
        optionalSpec.setRequired(false);

        // When/Then
        assertThat(requiredSpec.isRequired()).isTrue();
        assertThat(optionalSpec.isRequired()).isFalse();
    }

    @Test
    @DisplayName("Should test convert method with different types")
    public void shouldTestConvertMethodWithDifferentTypes ( ) throws Exception {
        // Given
        TestBaseInternalTool tool = new TestBaseInternalTool();
        tool.afterPropertiesSet ( ) ;

        // When/Then - String conversion
        Object stringResult = tool.convert("test", "string");
        assertThat(stringResult).isEqualTo("test");

        // When/Then - Unknown type conversion
        Object unknownResult = tool.convert("test", "unknown_type");
        assertThat(unknownResult).isNull();

        // When/Then - Null value conversion
        Object nullResult = tool.convert(null, "string");
        assertThatThrownBy(() -> nullResult.toString())
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should test ToolInvocationSpec getters and setters")
    public void shouldTestToolInvocationSpecGettersAndSetters() {
        // Given
        BaseInternalTool.ToolInvocationSpec spec = new BaseInternalTool.ToolInvocationSpec();
        BaseInternalTool.ToolArgumentSpec[] args = new BaseInternalTool.ToolArgumentSpec[1];

        // When
        spec.setInvocation("test invocation");
        spec.setArgs(args);

        // Then
        assertThat(spec.getInvocation()).isEqualTo("test invocation");
        assertThat(spec.getArgs()).isEqualTo(args);
    }

    @Test
    @DisplayName("Should test all ToolArgumentSpec properties")
    public void shouldTestAllToolArgumentSpecProperties() {
        // Given
        BaseInternalTool.ToolArgumentSpec spec = new BaseInternalTool.ToolArgumentSpec();

        // When
        spec.setArgName("testArg");
        spec.setType("java.lang.String");
        spec.setDescription("Test argument");
        spec.setRequired(true);

        // Then
        assertThat(spec.getArgName()).isEqualTo("testArg");
        assertThat(spec.getType()).isEqualTo("java.lang.String");
        assertThat(spec.getDescription()).isEqualTo("Test argument");
        assertThat(spec.isRequired()).isTrue();
    }

    // Test implementations for different scenarios

    private static class TestBaseInternalTool extends BaseInternalTool {
        @Override
        public String getName() { return "testTool"; }

        @Override
        public String getDescription() { return "Test tool"; }

        @Override
        public String executionSpec() {
            return """
{"invocation":"testTool","args":[
    {"argName": "param1", "type": "string", "description": "Big Description", "required": true},
    {"argName": "param2", "type": "string", "description": "Bigger Description", "required": true}
]}
""";
        }

        @Override
        public String doExecute(String toolExecutionId, Map<String, Object> argsMap) {
            return "Success with: " + argsMap.toString();
        }
    }

    private static class TestBaseInternalToolWithRequiredArgs extends BaseInternalTool {
        @Override
        public String getName() { return "testToolRequired"; }

        @Override
        public String executionSpec() {
            return "{\"invocation\":\"testTool\",\"args\":[" +
                   "{\"argName\":\"required_param\",\"type\":\"string\",\"required\":true}," +
                   "{\"argName\":\"optional_param\",\"type\":\"string\",\"required\":false}" +
                   "]}";
        }

        @Override
        public String doExecute(String toolExecutionId, Map<String, Object> argsMap) {
            return "Success";
        }
    }

    private static class TestBaseInternalToolWithTypeValidation extends BaseInternalTool {
        @Override
        public String getName() { return "testToolTypes"; }

        @Override
        public String executionSpec() {
            return "{\"invocation\":\"testTool\",\"args\":[" +
                   "{\"argName\":\"string_param\",\"type\":\"string\",\"required\":true}," +
                   "{\"argName\":\"number_param\",\"type\":\"number\",\"required\":true}" +
                   "]}";
        }

        @Override
        public String doExecute(String toolExecutionId, Map<String, Object> argsMap) {
            return "Success";
        }
    }

    private static class TestBaseInternalToolWithIOException extends BaseInternalTool {
        @Override
        public String getName() { return "testToolIO"; }

        @Override
        public String executionSpec() {
            return "{\"invocation\":\"testTool\",\"args\":[]}";
        }

        @Override
        public String doExecute(String toolExecutionId, Map<String, Object> argsMap) throws IOException {
            throw new IOException("Simulated IO exception");
        }
    }

    private static class TestBaseInternalToolWithNullSpec extends BaseInternalTool {
        @Override
        public String getName() { return "testToolNull"; }

        @Override
        public String executionSpec() {
            return null;
        }

        @Override
        public String doExecute(String toolExecutionId, Map<String, Object> argsMap) {
            return "Success";
        }
    }

    private static class TestBaseInternalToolWithInvalidSpec extends BaseInternalTool {
        @Override
        public String getName() { return "testToolInvalid"; }

        @Override
        public String executionSpec() {
            return "invalid json spec";
        }

        @Override
        public String doExecute(String toolExecutionId, Map<String, Object> argsMap) {
            return "Success";
        }
    }

    private static class TestBaseInternalToolWithNullArgs extends BaseInternalTool {
        @Override
        public String getName() { return "testToolNullArgs"; }

        @Override
        public String executionSpec() {
            return "{\"invocation\":\"testTool\"}"; // No args property
        }

        @Override
        public String doExecute(String toolExecutionId, Map<String, Object> argsMap) {
            return "Success with null args: " + argsMap.toString();
        }
    }
}