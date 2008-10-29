package ch.qos.logback.core.rolling;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PackageTest extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(RenamingTest.class);
    suite.addTest(new JUnit4TestAdapter(SizeBasedRollingTest.class));
    suite.addTest(new JUnit4TestAdapter(TimeBasedRollingTest.class));
    suite.addTest(ch.qos.logback.core.rolling.helper.PackageTest.suite());
    return suite;
  }
}
