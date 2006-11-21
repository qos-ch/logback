package ch.qos.logback.core.util;

import java.util.Calendar;
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
    
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date(now));
    
    int dayOffset = cal.getFirstDayOfWeek()-Calendar.SUNDAY;
    if(dayOffset != 0) {
      expected += 24L*3600*1000* (cal.getFirstDayOfWeek()-Calendar.SUNDAY);
    }
    
    long computed = TimeUtil.computeStartOfNextWeek(now);
//    System.out.println("now      "+new Date(now));
//    System.out.println("computed "+new Date(computed));
//    System.out.println("expected "+new Date(expected));
    assertEquals(expected - now, 1000*(3600*(5+24*(5+dayOffset))+60*54+42)+478); 
    assertEquals(expected, computed);
  }
  
  public void testMonth() {
    // Mon Nov 20 18:05:17 CET 2006
    long now = 1164042317522L;
    // Fri Dec 01 00:00:00 CET 2006
    long expected = 1164927600000L;
    long computed = TimeUtil.computeStartOfNextMonth(now);
    assertEquals(expected - now, 1000*(3600*(5+24*10)+60*54+42)+478); 
    assertEquals(expected, computed);
  }

  
}
