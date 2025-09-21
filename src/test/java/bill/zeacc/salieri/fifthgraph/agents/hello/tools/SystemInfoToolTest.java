package bill.zeacc.salieri.fifthgraph.agents.hello.tools;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SystemInfoToolTest {

    private SystemInfoTool systemInfoTool;

    @BeforeEach
    public void setUp() {
        systemInfoTool = new SystemInfoTool();
    }

    @Test
    public void testGetName() {
        assertEquals("getSystemInfo", systemInfoTool.getName());
    }

    @Test
    public void testGetDescription() {
        String description = systemInfoTool.getDescription();
        assertNotNull(description);
        assertTrue(description.contains("system information"));
        assertTrue(description.contains("hostname"));
        assertTrue(description.contains("OS"));
        assertTrue(description.contains("Java version"));
        assertTrue(description.contains("No parameters"));
    }

    @Test
    public void testExecutionSpec() {
        String spec = systemInfoTool.executionSpec();
        assertNotNull(spec);
        assertTrue(spec.contains("getSystemInfo"));
        assertTrue(spec.contains("args"));
        assertTrue(spec.contains("[]")); // Empty args array
    }

    @Test
    public void testDoExecute() throws IOException {
        Map<String, Object> argsMap = new HashMap<>();
        String result = systemInfoTool.doExecute("test-id", argsMap);
        
        assertNotNull(result);
        assertTrue(result.contains("hostname"));
        assertTrue(result.contains("os"));
        assertTrue(result.contains("java"));
        assertTrue(result.contains("user"));
        
        // Verify that actual system properties are included
        assertTrue(result.contains(System.getProperty("os.name")));
        assertTrue(result.contains(System.getProperty("java.version")));
        assertTrue(result.contains(System.getProperty("user.name")));
    }

    @Test
    public void testDoExecuteWithNullArgs() throws IOException {
        String result = systemInfoTool.doExecute("test-id", null);
        
        assertNotNull(result);
        // Should still work with null args since it doesn't use them
        assertTrue(result.contains("hostname"));
        assertTrue(result.contains("os"));
        assertTrue(result.contains("java"));
        assertTrue(result.contains("user"));
    }

    @Test
    public void testDoExecuteWithEmptyArgs() throws IOException {
        Map<String, Object> emptyArgs = new HashMap<>();
        String result = systemInfoTool.doExecute("test-id", emptyArgs);
        
        assertNotNull(result);
        assertTrue(result.contains("hostname"));
        assertTrue(result.contains("os"));
        assertTrue(result.contains("java"));
        assertTrue(result.contains("user"));
    }

    @Test
    public void testDoExecuteWithNonEmptyArgs() throws IOException {
        Map<String, Object> argsWithData = new HashMap<>();
        argsWithData.put("someParam", "someValue");
        
        String result = systemInfoTool.doExecute("test-id", argsWithData);
        
        assertNotNull(result);
        // Should ignore extra parameters and still work
        assertTrue(result.contains("hostname"));
        assertTrue(result.contains("os"));
        assertTrue(result.contains("java"));
        assertTrue(result.contains("user"));
    }

    @Test
    public void testResultContainsExpectedKeys() throws IOException {
        Map<String, Object> argsMap = new HashMap<>();
        String result = systemInfoTool.doExecute("test-id", argsMap);
        
        // The result should be a string representation of a map
        // containing the expected system information keys
        assertNotNull(result);
        assertTrue(result.startsWith("{"));
        assertTrue(result.endsWith("}"));
        
        // Verify all expected keys are present
        assertTrue(result.contains("hostname="));
        assertTrue(result.contains("os="));
        assertTrue(result.contains("java="));
        assertTrue(result.contains("user="));
    }
}
