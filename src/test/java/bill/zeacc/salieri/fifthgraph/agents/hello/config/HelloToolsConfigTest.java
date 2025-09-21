package bill.zeacc.salieri.fifthgraph.agents.hello.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException ;
import java.util.Arrays;
import java.util.List;
import java.util.Map ;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import bill.zeacc.salieri.fifthgraph.model.meta.BaseInternalTool ;
import bill.zeacc.salieri.fifthgraph.model.meta.InternalTool;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolChooser;

@ExtendWith(MockitoExtension.class)
public class HelloToolsConfigTest {

    private HelloToolsConfig helloToolsConfig;
    
    @Mock
    private InternalTool helloAnnotatedTool;
    
    @Mock
    private InternalTool nonHelloTool;

    @BeforeEach
    public void setUp() {
        helloToolsConfig = new HelloToolsConfig();
    }

    @Test
    public void testHelloToolsBeanCreation() {
        List<InternalTool> allTools = Arrays.asList(helloAnnotatedTool, nonHelloTool);
        
        ToolChooser toolChooser = helloToolsConfig.helloTools(allTools);
        
        assertNotNull(toolChooser);
    }

    @Test
    public void testHelloToolsFiltersCorrectly() throws Exception {
        // Create mock tools with proper class setup
        InternalTool annotatedTool = new TestToolWithAnnotation ( ) ;
        InternalTool nonAnnotatedTool = new TestToolWithoutAnnotation ( ) ;

        List<InternalTool> allTools = Arrays.asList(annotatedTool, nonAnnotatedTool);

        ToolChooser toolChooser = helloToolsConfig.helloTools(allTools);
        List<InternalTool> filteredTools = toolChooser.get();
        
        // Should only return tools with @HelloTool annotation
        assertEquals(1, filteredTools.size());
        assertEquals(annotatedTool, filteredTools.get(0));
    }

    @Test
    public void testHelloToolsWithEmptyList() {
        List<InternalTool> emptyTools = Arrays.asList();
        
        ToolChooser toolChooser = helloToolsConfig.helloTools(emptyTools);
        List<InternalTool> filteredTools = toolChooser.get();
        
        assertNotNull(filteredTools);
        assertTrue(filteredTools.isEmpty());
    }

    @Test
    public void testHelloToolsWithAllAnnotatedTools() throws Exception {
        InternalTool tool1 = new TestToolWithAnnotation ( ) ;
        InternalTool tool2 = new TestToolWithAnnotation ( ) ;

        List<InternalTool> allAnnotatedTools = Arrays.asList(tool1, tool2);

        ToolChooser toolChooser = helloToolsConfig.helloTools(allAnnotatedTools);
        List<InternalTool> filteredTools = toolChooser.get();
        
        assertEquals(2, filteredTools.size());
        assertTrue(filteredTools.contains(tool1));
        assertTrue(filteredTools.contains(tool2));
    }

    @Test
    public void testHelloToolsWithNoAnnotatedTools() {
        InternalTool tool1 = new TestToolWithoutAnnotation ( ) ;
        InternalTool tool2 = new TestToolWithoutAnnotation ( ) ;

        List<InternalTool> nonAnnotatedTools = Arrays.asList(tool1, tool2);

        ToolChooser toolChooser = helloToolsConfig.helloTools(nonAnnotatedTools);
        List<InternalTool> filteredTools = toolChooser.get();

        assertTrue(filteredTools.isEmpty());
    }

    // Create a test class that has the @HelloTool annotation
    @HelloTool
    public class TestToolWithAnnotation extends BaseInternalTool {
    	public TestToolWithAnnotation ( ) { ; }
        public String getName() { return "test"; }
        public String getDescription() { return "test"; }
        public String executionSpec() { return "test"; }
		@Override
		public String doExecute ( String toolExecutionId, Map <String, Object> argsMap ) throws IOException {
			return "test" ;
		}
    }

    public class TestToolWithoutAnnotation extends BaseInternalTool {
    	public TestToolWithoutAnnotation ( ) { ; }
		public String getName() { return "test"; }
		public String getDescription() { return "test"; }
		public String executionSpec() { return "test"; }
		@Override
		public String doExecute ( String toolExecutionId, Map <String, Object> argsMap ) throws IOException {
			return "test" ;
		}
    }
}
