package ch.qos.logback.core.util;



import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PackageTest extends TestCase {

	public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(PropertySetterTest.class);
    return suite;
  }
}
