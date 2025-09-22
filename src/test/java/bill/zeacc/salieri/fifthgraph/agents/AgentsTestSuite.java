package bill.zeacc.salieri.fifthgraph.agents;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

import bill.zeacc.salieri.fifthgraph.agents.hello.GraphStateTest;
import bill.zeacc.salieri.fifthgraph.agents.hello.config.HelloAgentConfigTest;
import bill.zeacc.salieri.fifthgraph.agents.hello.config.HelloToolsConfigTest ;
import bill.zeacc.salieri.fifthgraph.agents.hello.tools.DateTimeToolTest;
import bill.zeacc.salieri.fifthgraph.agents.hello.tools.FileReaderToolTest ;
import bill.zeacc.salieri.fifthgraph.agents.hello.tools.SystemInfoToolTest ;
import bill.zeacc.salieri.fifthgraph.agents.justchat.config.JustChatConfigTest ;
import bill.zeacc.salieri.fifthgraph.agents.switchboard.config.SwitchboardAgentConfigTest ;
import bill.zeacc.salieri.fifthgraph.agents.switchboard.nodes.SwitchboardAnalysisNodeTest ;

/**
 * Test suite for all agent-related tests in the fifthgraph package.
 */
@Suite
@SuiteDisplayName("Agents Test Suite")
@SelectClasses({
    GraphStateTest.class,
    HelloAgentConfigTest.class,
    DateTimeToolTest.class,
    HelloToolsConfigTest.class,
    FileReaderToolTest.class,
    SystemInfoToolTest.class,
    JustChatConfigTest.class,
    SwitchboardAnalysisNodeTest.class,
    SwitchboardAgentConfigTest.class
})
public class AgentsTestSuite {
}