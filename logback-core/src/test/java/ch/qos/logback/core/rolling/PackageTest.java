package ch.qos.logback.core.rolling;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PackageTest extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(RenamingTest.class);
    suite.addTestSuite(SizeBasedRollingTest.class);
    suite.addTestSuite(TimeBasedRollingTest.class);
    return suite;
  }
}
