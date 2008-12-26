package ch.qos.logback.classic.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CallerDataTest  {


  @Test
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
  @Test
  public void testDeferredProcessing() {
    CallerData[] cda = CallerData.extract(new Throwable(), "com.inexistent.foo");
    assertNotNull(cda);
    assertEquals(0, cda.length);
  }
  
}
