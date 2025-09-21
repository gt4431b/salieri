package bill.zeacc.salieri.fifthgraph.service;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite for all service-related tests in the fifthgraph package.
 */
@Suite
@SuiteDisplayName("Service Test Suite")
@SelectClasses({
    GraphServiceTest.class
})
public class ServiceTestSuite {
}