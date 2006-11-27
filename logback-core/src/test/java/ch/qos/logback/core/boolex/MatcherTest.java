package ch.qos.logback.core.boolex;

import junit.framework.TestCase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;

public class MatcherTest extends TestCase {

  Context context;
  Matcher matcher;
  
  public void setUp() throws Exception {
    context = new ContextBase();
    matcher = new Matcher();
    matcher.setContext(context);
    matcher.setName("testMatcher");
    super.setUp();
  }
  
  public void tearDown() throws Exception {
    matcher = null;
    super.tearDown();
  }
  
  public void testFullRegion() throws Exception {
    matcher.setRegex(".*test.*");
    matcher.start();
    assertTrue(matcher.matches("test"));
    assertTrue(matcher.matches("xxxxtest"));
    assertTrue(matcher.matches("testxxxx"));
    assertTrue(matcher.matches("xxxxtestxxxx"));
  }
  
  public void testPartRegion() throws Exception {
    matcher.setRegex("test");
    matcher.start();
    assertTrue(matcher.matches("test"));
    assertTrue(matcher.matches("xxxxtest"));
    assertTrue(matcher.matches("testxxxx"));
    assertTrue(matcher.matches("xxxxtestxxxx"));
  }
  
  public void testCaseInsensitive() throws Exception {
    matcher.setRegex("test");
    matcher.setCaseSensitive(false);
    matcher.start();
    
    assertTrue(matcher.matches("TEST"));
    assertTrue(matcher.matches("tEst"));
    assertTrue(matcher.matches("tESt"));
    assertTrue(matcher.matches("TesT"));
  }
  
  public void testCaseSensitive() throws Exception {
    matcher.setRegex("test");
    matcher.setCaseSensitive(true);
    matcher.start();
    
    assertFalse(matcher.matches("TEST"));
    assertFalse(matcher.matches("tEst"));
    assertFalse(matcher.matches("tESt"));
    assertFalse(matcher.matches("TesT"));
  }
}
