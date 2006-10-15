package ch.qos.logback.core.joran;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PackageTest extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(SkippingInInterpreterTest.class);
    suite.addTestSuite(EventRecorderTest.class);
    suite.addTestSuite(TrivialcConfiguratorTest.class);
    suite.addTest(ch.qos.logback.core.joran.spi.PackageTest.suite());
    return suite;
  }
}
