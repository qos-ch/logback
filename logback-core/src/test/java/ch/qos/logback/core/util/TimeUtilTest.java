package ch.qos.logback.core.util;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;

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

  public void testSecond() {
    // Mon Nov 20 18:05:17,522 CET 2006
    long now = 1164042317522L;
    //Mon Nov 20 18:05:18,000 CET 2006
    long expected = 1164042318000L;
    long computed = TimeUtil.computeStartOfNextSecond(now);
    assertEquals(expected - now, 478); 
    assertEquals(expected, computed);
  }
  
  public void testMinute() {
    // Mon Nov 20 18:05:17,522 CET 2006
    long now = 1164042317522L;
    // Mon Nov 20 18:06:00 CET 2006
    long expected = 1164042360000L;
    long computed = TimeUtil.computeStartOfNextMinute(now);
    assertEquals(expected - now, 1000*42+478); 
    assertEquals(expected, computed);
  }

  public void testHour() {
    // Mon Nov 20 18:05:17,522 CET 2006
    long now = 1164042317522L;
    // Mon Nov 20 19:00:00 CET 2006
    long expected = 1164045600000L;
    //System.out.println(new Date(expected));
    long computed = TimeUtil.computeStartOfNextHour(now);
    assertEquals(expected - now, 1000*(42+60*54)+478); 
    assertEquals(expected, computed);
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
