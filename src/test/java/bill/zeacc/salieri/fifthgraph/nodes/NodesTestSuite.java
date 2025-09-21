package bill.zeacc.salieri.fifthgraph.nodes;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite for all node-related tests in the fifthgraph package.
 */
@Suite
@SuiteDisplayName("Nodes Test Suite")
@SelectClasses({
    ResponseFormatterNodeTest.class,
    ToolAnalyzerNodeTest.class,
    ToolExecutorNodeTest.class
})
public class NodesTestSuite {
}