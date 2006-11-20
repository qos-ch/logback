package ch.qos.logback.core.util;

import java.util.Date;

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
  
  public void testWeek() {
    // Mon Nov 20 18:05:17 CET 2006
    long now = 1164042317522L;
    // Sun Nov 26 00:00:00 CET 2006
    long expected = 1164495600000L;
    long computed = TimeUtil.computeStartOfNextWeek(now);
    assertEquals(expected - now, 1000*(3600*(5+24*5)+60*54+42)+478); 
    assertEquals(expected, computed);
  }
  
  public void testMonth() {
    // Mon Nov 20 18:05:17 CET 2006
    long now = 1164042317522L;
    // Fri Dec 01 00:00:00 CET 2006
    long expected = 1164927600000L;
    long computed = TimeUtil.computeStartOfNextMonth(now);
    
    System.out.println(computed);
    System.out.println(new Date(computed));
    assertEquals(expected - now, 1000*(3600*(5+24*10)+60*54+42)+478); 
    assertEquals(expected, computed);
  }

  
}
