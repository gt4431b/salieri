package bill.zeacc.salieri.fifthgraph;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Comprehensive test suite for the bill.zeacc.salieri.fifthgraph package.
 * This suite encompasses all unit tests across all subpackages.
 */
@Suite
@SuiteDisplayName("Fifth Graph Complete Test Suite")
@SelectPackages({"bill.zeacc.salieri.fifthgraph", "bill.zeacc.salieri.fifthgraph.config"})
public class FifthGraphTestSuite {
    // This test suite will automatically discover and run all tests in the 
    // bill.zeacc.salieri.fifthgraph package and its subpackages
}