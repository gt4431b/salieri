package bill.zeacc.salieri.fifthgraph.agents.hello.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FileReaderToolTest {

    private FileReaderTool fileReaderTool;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() throws Exception {
        fileReaderTool = new FileReaderTool();
        fileReaderTool.afterPropertiesSet(); // Test InitializingBean interface
    }

    @Test
    public void testGetName() {
        assertEquals("readFile", fileReaderTool.getName());
    }

    @Test
    public void testGetDescription() {
        String description = fileReaderTool.getDescription();
        assertNotNull(description);
        assertTrue(description.contains("Reads the content of a file"));
        assertTrue(description.contains("fileName"));
        assertTrue(description.contains("required parameter"));
    }

    @Test
    public void testExecutionSpec() {
        String spec = fileReaderTool.executionSpec();
        assertNotNull(spec);
        assertTrue(spec.contains("readFile"));
        assertTrue(spec.contains("fileName"));
        assertTrue(spec.contains("string"));
        assertTrue(spec.contains("required"));
        assertTrue(spec.contains("true"));
    }

    @Test
    public void testGetToolExecutionId() {
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("fileName", "test.txt");
        
        String executionId = fileReaderTool.getToolExecutionId(argsMap);
        assertEquals("test.txt", executionId);
    }

    @Test
    public void testGetToolExecutionIdWithDifferentFileName() {
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("fileName", "different-file.log");
        
        String executionId = fileReaderTool.getToolExecutionId(argsMap);
        assertEquals("different-file.log", executionId);
    }

    @Test
    public void testDoExecuteWithValidFile() throws IOException {
        // Create a test file
        Path testFile = tempDir.resolve("test.txt");
        String testContent = "This is test content\nWith multiple lines\nFor testing purposes";
        Files.writeString(testFile, testContent);
        
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("fileName", testFile.toString());
        
        String result = fileReaderTool.doExecute("test-id", argsMap);
        
        assertEquals(testContent, result);
    }

    @Test
    public void testDoExecuteWithEmptyFile() throws IOException {
        // Create an empty test file
        Path testFile = tempDir.resolve("empty.txt");
        Files.writeString(testFile, "");
        
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("fileName", testFile.toString());
        
        String result = fileReaderTool.doExecute("test-id", argsMap);
        
        assertEquals("", result);
    }

    @Test
    public void testDoExecuteWithNonExistentFile() {
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("fileName", "nonexistent.txt");
        
        assertThrows(NoSuchFileException.class, () -> {
            fileReaderTool.doExecute("test-id", argsMap);
        });
    }

    @Test
    public void testDoExecuteWithRelativePath() throws IOException {
        // Create a test file with relative path
        Path testFile = tempDir.resolve("subdir").resolve("relative.txt");
        Files.createDirectories(testFile.getParent());
        String testContent = "Relative path content";
        Files.writeString(testFile, testContent);
        
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("fileName", testFile.toString());
        
        String result = fileReaderTool.doExecute("test-id", argsMap);
        
        assertEquals(testContent, result);
    }

    @Test
    public void testDoExecuteWithSpecialCharacters() throws IOException {
        // Create a test file with special characters
        Path testFile = tempDir.resolve("special.txt");
        String testContent = "Content with special chars: äöü €£¥ 中文 🚀";
        Files.writeString(testFile, testContent);
        
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("fileName", testFile.toString());
        
        String result = fileReaderTool.doExecute("test-id", argsMap);
        
        assertEquals(testContent, result);
    }

    @Test
    public void testDoExecuteWithLargeFile() throws IOException {
        // Create a larger test file
        Path testFile = tempDir.resolve("large.txt");
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeContent.append("Line ").append(i).append(": This is line number ").append(i).append("\n");
        }
        Files.writeString(testFile, largeContent.toString());
        
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("fileName", testFile.toString());
        
        String result = fileReaderTool.doExecute("test-id", argsMap);
        
        assertEquals(largeContent.toString(), result);
        assertTrue(result.contains("Line 0:"));
        assertTrue(result.contains("Line 999:"));
    }

    @Test
    public void testAfterPropertiesSet() throws Exception {
        // Test that afterPropertiesSet can be called without exception
        FileReaderTool newTool = new FileReaderTool();
        assertDoesNotThrow(() -> newTool.afterPropertiesSet());
    }

    @Test
    public void testDoExecuteWithNullFileName() {
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("fileName", null);
        
        assertThrows(Exception.class, () -> {
            fileReaderTool.doExecute("test-id", argsMap);
        });
    }

    @Test
    public void testDoExecuteWithMissingFileNameKey() {
        Map<String, Object> argsMap = new HashMap<>();
        // No fileName key in the map
        
        assertThrows(Exception.class, () -> {
            fileReaderTool.doExecute("test-id", argsMap);
        });
    }
}
