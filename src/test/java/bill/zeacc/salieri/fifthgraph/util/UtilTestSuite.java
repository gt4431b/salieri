package bill.zeacc.salieri.fifthgraph.util;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite for all utility-related tests in the fifthgraph package.
 */
@Suite
@SuiteDisplayName("Utility Test Suite")
@SelectClasses({
    NodeHelperTest.class,
    DebouncedStdInBlocksTest.class,
    SandboxTest.class
})
public class UtilTestSuite {
}