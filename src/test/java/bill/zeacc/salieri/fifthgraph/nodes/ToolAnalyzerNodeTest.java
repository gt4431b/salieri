package bill.zeacc.salieri.fifthgraph.nodes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.* ;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import com.fasterxml.jackson.core.JsonProcessingException ;
import com.fasterxml.jackson.databind.ObjectMapper;

import bill.zeacc.salieri.fifthgraph.model.meta.BaseInternalTool ;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolChooser;
import bill.zeacc.salieri.fifthgraph.model.states.ToolOrientedState;

@ExtendWith(MockitoExtension.class)
@DisplayName("ToolAnalyzerNode Tests")
public class ToolAnalyzerNodeTest {

    @Mock
    private ChatModel mockChatModel;

    @Mock
    private ToolChooser mockToolChooser;

    @Mock
    private ToolOrientedState mockState;

    @Mock
    private ChatResponse mockChatResponse;

    @Mock
    private Generation mockGeneration;

    @Mock
    private BaseInternalTool mockTool;

    private TestToolAnalyzerNode toolAnalyzerNode;
    private ObjectMapper objectMapper;

    @BeforeEach
    protected void setUp() throws Exception {
        objectMapper = new ObjectMapper();
		lenient ( ).when(mockTool.getName()).thenReturn("getDateTime");
		lenient ( ).when(mockTool.getDescription()).thenReturn("Gets the current date and time");
		lenient ( ).when ( mockTool.executionSpec ( ) ).thenReturn ( "{\"invocation\": \"getDateTime\", \"args\": []}" ) ;
        when(mockToolChooser.get()).thenReturn(List.of(mockTool));
        toolAnalyzerNode = new TestToolAnalyzerNode(mockChatModel, mockToolChooser);
        toolAnalyzerNode.setObjectMapper ( objectMapper ); // Inject ObjectMapper
	}

    @Test
    @DisplayName("Should initialize with chat model and tool chooser")
    public void shouldInitializeWithChatModelAndToolChooser() {
        // When
        TestToolAnalyzerNode node = new TestToolAnalyzerNode(mockChatModel, mockToolChooser);

        // Then
        assertThat(node).isNotNull();
        verify(mockToolChooser, atLeast ( 1 ) ).get();
    }

    @Test
    @DisplayName("Should analyze query and return analysis result")
    public void shouldAnalyzeQueryAndReturnAnalysisResult() throws Exception {
        // Given
        String query = "What's the current time?";
        String analysisResponse = "{\"needsTools\": true, \"tools\": [{\"invocation\": \"getDateTime\", \"args\": [], \"justification\": \"Cuz I sed so\"}]}";

        when(mockState.getQuery()).thenReturn(query);
        when(mockChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(new org.springframework.ai.chat.messages.AssistantMessage(analysisResponse));

        // When
        Map<String, Object> result = toolAnalyzerNode.apply(mockState);

        // Then
        assertThat(result).containsKey(ToolOrientedState.ANALYSIS_KEY);
        assertThat(result.get(ToolOrientedState.ANALYSIS_KEY)).isEqualTo(analysisResponse);
        assertThat(result).containsKey(ToolOrientedState.TOOL_CALLS_KEY);
        assertThat(result).containsKey(ToolOrientedState.TOOL_RESULTS_KEY);
    }

    @Test
    @DisplayName("Should parse tool call with string arguments correctly")
    public void shouldParseToolCallWithStringArgumentsCorrectly() throws Exception {
        // Given
        String query = "Read the README.md file";
        String analysisResponse = "{\"needsTools\": true, \"tools\": [{\"invocation\": \"readFile\", \"args\": [{\"argName\": \"filename\", \"type\": \"string\", \"stringValue\": \"README.md\"}], \"justification\": \"Need to read file\"}]}";

        // Setup mock tool with string argument
        BaseInternalTool fileReaderTool = mock(BaseInternalTool.class);
        when(fileReaderTool.getName()).thenReturn("readFile");
        when(fileReaderTool.getDescription()).thenReturn("Reads a file");
        when(fileReaderTool.executionSpec()).thenReturn("{\"invocation\": \"readFile\", \"args\": [{\"name\": \"filename\", \"type\": \"string\"}]}");
        when(mockToolChooser.get()).thenReturn(List.of(fileReaderTool));

        // Recreate node with new tool
        toolAnalyzerNode = new TestToolAnalyzerNode(mockChatModel, mockToolChooser);
        toolAnalyzerNode.setObjectMapper(objectMapper);

        when(mockState.getQuery()).thenReturn(query);
        when(mockChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(new org.springframework.ai.chat.messages.AssistantMessage(analysisResponse));

        // When
        Map<String, Object> result = toolAnalyzerNode.apply(mockState);

        // Then
        assertThat(result).containsKey(ToolOrientedState.TOOL_CALLS_KEY);
        @SuppressWarnings("unchecked")
        List<Object> toolCalls = (List<Object>) result.get(ToolOrientedState.TOOL_CALLS_KEY);
        assertThat(toolCalls).hasSize(1);
    }

    @Test
    @DisplayName("Should parse tool call with number arguments correctly")
    public void shouldParseToolCallWithNumberArgumentsCorrectly() throws Exception {
        // Given
        String query = "Calculate something with number 42.5";
        String analysisResponse = "{\"needsTools\": true, \"tools\": [{\"invocation\": \"calculate\", \"args\": [{\"argName\": \"value\", \"type\": \"number\", \"stringValue\": \"42.5\"}], \"justification\": \"Need to calculate\"}]}";

        // Setup mock tool with number argument
        BaseInternalTool calculatorTool = mock(BaseInternalTool.class);
        when(calculatorTool.getName()).thenReturn("calculate");
        when(calculatorTool.getDescription()).thenReturn("Performs calculation");
        when(calculatorTool.executionSpec()).thenReturn("{\"invocation\": \"calculate\", \"args\": [{\"name\": \"value\", \"type\": \"number\"}]}");
        when(mockToolChooser.get()).thenReturn(List.of(calculatorTool));

        // Recreate node with new tool
        toolAnalyzerNode = new TestToolAnalyzerNode(mockChatModel, mockToolChooser);
        toolAnalyzerNode.setObjectMapper(objectMapper);

        when(mockState.getQuery()).thenReturn(query);
        when(mockChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(new org.springframework.ai.chat.messages.AssistantMessage(analysisResponse));

        // When
        Map<String, Object> result = toolAnalyzerNode.apply(mockState);

        // Then
        assertThat(result).containsKey(ToolOrientedState.TOOL_CALLS_KEY);
        @SuppressWarnings("unchecked")
        List<Object> toolCalls = (List<Object>) result.get(ToolOrientedState.TOOL_CALLS_KEY);
        assertThat(toolCalls).hasSize(1);
    }

    @Test
    @DisplayName("Should parse tool call with boolean arguments correctly")
    public void shouldParseToolCallWithBooleanArgumentsCorrectly() throws Exception {
        // Given
        String query = "Set flag to true";
        String analysisResponse = "{\"needsTools\": true, \"tools\": [{\"invocation\": \"setFlag\", \"args\": [{\"argName\": \"enabled\", \"type\": \"boolean\", \"stringValue\": \"true\"}], \"justification\": \"Need to set flag\"}]}";

        // Setup mock tool with boolean argument
        BaseInternalTool flagTool = mock(BaseInternalTool.class);
        when(flagTool.getName()).thenReturn("setFlag");
        when(flagTool.getDescription()).thenReturn("Sets a flag");
        when(flagTool.executionSpec()).thenReturn("{\"invocation\": \"setFlag\", \"args\": [{\"name\": \"enabled\", \"type\": \"boolean\"}]}");
        when(mockToolChooser.get()).thenReturn(List.of(flagTool));

        // Recreate node with new tool
        toolAnalyzerNode = new TestToolAnalyzerNode(mockChatModel, mockToolChooser);
        toolAnalyzerNode.setObjectMapper(objectMapper);

        when(mockState.getQuery()).thenReturn(query);
        when(mockChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(new org.springframework.ai.chat.messages.AssistantMessage(analysisResponse));

        // When
        Map<String, Object> result = toolAnalyzerNode.apply(mockState);

        // Then
        assertThat(result).containsKey(ToolOrientedState.TOOL_CALLS_KEY);
        @SuppressWarnings("unchecked")
        List<Object> toolCalls = (List<Object>) result.get(ToolOrientedState.TOOL_CALLS_KEY);
        assertThat(toolCalls).hasSize(1);
    }

    @Test
    @DisplayName("Should parse tool call with multiple arguments correctly")
    public void shouldParseToolCallWithMultipleArgumentsCorrectly() throws Exception {
        // Given
        String query = "Process file with settings";
        String analysisResponse = "{\"needsTools\": true, \"tools\": [{\"invocation\": \"processFile\", \"args\": [{\"argName\": \"filename\", \"type\": \"string\", \"stringValue\": \"data.txt\"}, {\"argName\": \"count\", \"type\": \"number\", \"stringValue\": \"10\"}, {\"argName\": \"verbose\", \"type\": \"boolean\", \"stringValue\": \"false\"}], \"justification\": \"Need to process file\"}]}";

        // Setup mock tool with multiple arguments
        BaseInternalTool processTool = mock(BaseInternalTool.class);
        when(processTool.getName()).thenReturn("processFile");
        when(processTool.getDescription()).thenReturn("Processes a file");
        when(processTool.executionSpec()).thenReturn("{\"invocation\": \"processFile\", \"args\": [{\"name\": \"filename\", \"type\": \"string\"}, {\"name\": \"count\", \"type\": \"number\"}, {\"name\": \"verbose\", \"type\": \"boolean\"}]}");
        when(mockToolChooser.get()).thenReturn(List.of(processTool));

        // Recreate node with new tool
        toolAnalyzerNode = new TestToolAnalyzerNode(mockChatModel, mockToolChooser);
        toolAnalyzerNode.setObjectMapper(objectMapper);

        when(mockState.getQuery()).thenReturn(query);
        when(mockChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(new org.springframework.ai.chat.messages.AssistantMessage(analysisResponse));

        // When
        Map<String, Object> result = toolAnalyzerNode.apply(mockState);

        // Then
        assertThat(result).containsKey(ToolOrientedState.TOOL_CALLS_KEY);
        @SuppressWarnings("unchecked")
        List<Object> toolCalls = (List<Object>) result.get(ToolOrientedState.TOOL_CALLS_KEY);
        assertThat(toolCalls).hasSize(1);
    }

    @Test
    @DisplayName("Should throw exception when tool not found")
    public void shouldThrowExceptionWhenToolNotFound() throws Exception {
        // Given
        String query = "Use unknown tool";
        String analysisResponse = "{\"needsTools\": true, \"tools\": [{\"invocation\": \"unknownTool\", \"args\": [], \"justification\": \"Test unknown tool\"}]}";

        when(mockState.getQuery()).thenReturn(query);
        when(mockChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(new org.springframework.ai.chat.messages.AssistantMessage(analysisResponse));

        // When/Then
        assertThatThrownBy(() -> toolAnalyzerNode.apply(mockState))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Requested tool not found: unknownTool");
    }

    @Test
    @DisplayName("Should throw exception for unsupported argument type")
    public void shouldThrowExceptionForUnsupportedArgumentType() throws Exception {
        // Given
        String query = "Use tool with unsupported type";
        String analysisResponse = "{\"needsTools\": true, \"tools\": [{\"invocation\": \"getDateTime\", \"args\": [{\"argName\": \"badArg\", \"type\": \"unsupportedType\", \"stringValue\": \"value\"}], \"justification\": \"Test bad type\"}]}";

        when(mockState.getQuery()).thenReturn(query);
        when(mockChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(new org.springframework.ai.chat.messages.AssistantMessage(analysisResponse));

        // When/Then
        assertThatThrownBy(() -> toolAnalyzerNode.apply(mockState))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported argument type: unsupportedType");
    }

    @Test
    @DisplayName("Should throw exception for invalid number format")
    public void shouldThrowExceptionForInvalidNumberFormat() throws Exception {
        // Given
        String query = "Calculate with invalid number";
        String analysisResponse = "{\"needsTools\": true, \"tools\": [{\"invocation\": \"calculate\", \"args\": [{\"argName\": \"value\", \"type\": \"number\", \"stringValue\": \"not-a-number\"}], \"justification\": \"Test invalid number\"}]}";

        // Setup mock tool with number argument
        BaseInternalTool calculatorTool = mock(BaseInternalTool.class);
        when(calculatorTool.getName()).thenReturn("calculate");
        when(calculatorTool.getDescription()).thenReturn("Performs calculation");
        when(calculatorTool.executionSpec()).thenReturn("{\"invocation\": \"calculate\", \"args\": [{\"name\": \"value\", \"type\": \"number\"}]}");
        when(mockToolChooser.get()).thenReturn(List.of(calculatorTool));

        // Recreate node with new tool
        toolAnalyzerNode = new TestToolAnalyzerNode(mockChatModel, mockToolChooser);
        toolAnalyzerNode.setObjectMapper(objectMapper);

        when(mockState.getQuery()).thenReturn(query);
        when(mockChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(new org.springframework.ai.chat.messages.AssistantMessage(analysisResponse));

        // When/Then
        assertThatThrownBy(() -> toolAnalyzerNode.apply(mockState))
            .isInstanceOf(NumberFormatException.class);
    }

    @SuppressWarnings ( "serial" )
	@Test
    @DisplayName("Should handle JSON serialization failure for tool arguments")
    public void shouldHandleJsonSerializationFailureForToolArguments() throws Exception {
        // Given
        String query = "Test with problematic arguments";
        String analysisResponse = "{\"needsTools\": true, \"tools\": [{\"invocation\": \"getDateTime\", \"args\": [{\"argName\": \"test\", \"type\": \"string\", \"stringValue\": \"value\"}], \"justification\": \"Test serialization failure\"}]}";

        // Mock ObjectMapper to throw JsonProcessingException
        ObjectMapper faultyObjectMapper = mock(ObjectMapper.class);
        when(faultyObjectMapper.readValue(anyString(), eq(TestToolAnalyzerNode.AnalysisResult.class)))
            .thenReturn(objectMapper.readValue(analysisResponse, TestToolAnalyzerNode.AnalysisResult.class));
        when(faultyObjectMapper.writeValueAsString(any()))
            .thenThrow(new JsonProcessingException("Serialization failed") {});

        toolAnalyzerNode.setObjectMapper(faultyObjectMapper);

        when(mockState.getQuery()).thenReturn(query);
        when(mockChatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(new org.springframework.ai.chat.messages.AssistantMessage(analysisResponse));

        // When/Then
        assertThatThrownBy(() -> toolAnalyzerNode.apply(mockState))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to serialize tool arguments for tool: getDateTime");
    }

    // Concrete test implementation of the abstract class
    private static class TestToolAnalyzerNode extends ToolAnalyzerNode {

        public TestToolAnalyzerNode(ChatModel chatModel, ToolChooser toolChooser) {
            super(chatModel, toolChooser);
        }
    }
}
