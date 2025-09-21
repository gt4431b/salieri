package bill.zeacc.salieri.fifthgraph.model;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

import bill.zeacc.salieri.fifthgraph.model.meta.AgentDescriptorTest;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolCallTest;
import bill.zeacc.salieri.fifthgraph.model.meta.ToolResponseTest;
import bill.zeacc.salieri.fifthgraph.model.states.EngineeringStateTest ;
import bill.zeacc.salieri.fifthgraph.model.states.ResultOrientedStateTest ;
import bill.zeacc.salieri.fifthgraph.model.states.ToolOrientedStateTest;

/**
 * Test suite for all model-related tests in the fifthgraph package.
 */
@Suite
@SuiteDisplayName("Model Test Suite")
@SelectClasses({
    AgentDescriptorTest.class,
    ToolCallTest.class,
    ToolResponseTest.class,
    ToolOrientedStateTest.class,
    EngineeringStateTest.class,
    ResultOrientedStateTest.class,
    ToolOrientedStateTest.class
})
public class ModelTestSuite {
}