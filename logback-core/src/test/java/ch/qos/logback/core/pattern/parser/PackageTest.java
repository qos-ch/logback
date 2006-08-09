package ch.qos.logback.core.pattern.parser;



import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PackageTest extends TestCase {

	public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(TokenStreamTest.class);
    suite.addTestSuite(OptionTokenizerTest.class);
    suite.addTestSuite(ParserTest.class);
    suite.addTestSuite(FormatInfoTest.class);
    suite.addTestSuite(CompilerTest.class);
    suite.addTestSuite(SamplePatternLayoutTest.class);
    return suite;
  }
}
