package ch.qos.logback.classic.spi;

import junit.framework.TestCase;

public class CallerDataTest extends TestCase {

  public CallerDataTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testBasic() {
    Throwable t = new Throwable();
    StackTraceElement[] steArray = t.getStackTrace();
    
    CallerData[] cda = CallerData.extract(t, CallerDataTest.class.getName());
    assertNotNull(cda);
    assertTrue(cda.length > 0);
    assertEquals(steArray.length - 1, cda.length);
  }
  
  /**
   * This test verifies that in case caller data cannot be
   * extracted, CallerData.extract does not throw an exception
   *
   */
  public void testDeferredProcessing() {
    CallerData[] cda = CallerData.extract(new Throwable(), "com.inexistent.foo");
    assertNotNull(cda);
    assertEquals(0, cda.length);
  }
  
}
