package ch.qos.logback.core.pattern;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PackageTest extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new JUnit4TestAdapter(ch.qos.logback.core.pattern.SpacePadderTest.class));
    suite.addTest(ch.qos.logback.core.pattern.parser.PackageTest.suite());
    return suite;
  }
}
