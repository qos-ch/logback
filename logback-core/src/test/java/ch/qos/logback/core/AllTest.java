package ch.qos.logback.core;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTest extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(ch.qos.logback.core.util.PackageTest.suite());
    suite.addTest(ch.qos.logback.core.pattern.AllTest.suite());
    suite.addTest(ch.qos.logback.core.appender.PackageTest.suite());
    suite.addTest(ch.qos.logback.core.rolling.helper.PackageTest.suite());
    suite.addTest(ch.qos.logback.core.rolling.PackageTest.suite());
    suite.addTest(ch.qos.logback.core.joran.PackageTest.suite());
    return suite;
  }
}
