package ch.qos.logback.core.pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PackageTest extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(ch.qos.logback.core.pattern.parser.PackageTest.suite());
    return suite;
  }
}
