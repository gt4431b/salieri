package bill.zeacc.salieri.fifthgraph.rags;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite for all RAG (Retrieval-Augmented Generation) related tests in the fifthgraph package.
 */
@Suite
@SuiteDisplayName("RAGs Test Suite")
@SelectClasses({
    DirectRagPullerServiceTest.class,
    HybridizingSegmentMapperTest.class,
    PdfBoxCorpusParserTest.class,
    RagIngestServiceTest.class,
    RagIngestionTreeContextTest.class,
    TextCorpusParserTest.class
})
public class RagsTestSuite {
}