package ch.qos.logback.core.util;

import junit.framework.TestCase;

public class TimeUtilTest extends TestCase {

  public TimeUtilTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  
  public void testDay() {
    // Mon Nov 20 18:05:17 CET 2006
    long now = 1164042317522L;
    // Tue Nov 21 00:00:00 CET 2006
    long expected = 1164063600000L;
    long computed = TimeUtil.computeStartOfNextDay(now);
    assertEquals(expected - now, 1000*(3600*5+60*54+42)+478); 
    assertEquals(expected, computed);
  }
}
