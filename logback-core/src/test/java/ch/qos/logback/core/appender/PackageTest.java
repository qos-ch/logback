package ch.qos.logback.core.appender;



import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PackageTest extends TestCase {

	public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(DummyAppenderTest.class);
    suite.addTestSuite(ConsoleAppenderTest.class);
    suite.addTestSuite(FileAppenderTest.class);
    return suite;
  }
}
