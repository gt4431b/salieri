package bill.zeacc.salieri.fifthgraph.controller;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite for all controller-related tests in the fifthgraph package.
 */
@Suite
@SuiteDisplayName("Controller Test Suite")
@SelectClasses({
    ChatControllerTest.class,
    ChatRequestTest.class,
    ChatResponseTest.class
})
public class ControllerTestSuite {
}