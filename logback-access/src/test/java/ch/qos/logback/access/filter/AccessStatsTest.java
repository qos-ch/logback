package ch.qos.logback.access.filter;

import junit.framework.TestCase;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.util.TimeUtil;

public class AccessStatsTest extends TestCase {

  AccessEvent accessEvent = new AccessEvent(null, null, null);
  public AccessStatsTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testBasic() {
    
    CountingFilter cf = new CountingFilter();
    AccessStatsImpl asi = new AccessStatsImpl(cf);
    asi.start();
    // Tue Nov 21 18:05:36 CET 2006
    long now = 1164128736369L;
    
    // test fresh start
    asi.refresh(now);
    assertEquals(0, asi.getLastDaysCount());
    assertEquals(0, asi.getDailyAverage(), 0.01);

    // first event
    cf.decide(null);

    asi.refresh(now);
    assertEquals(0, asi.getLastDaysCount());
    assertEquals(0.0, asi.getDailyAverage(), 0.01);

    long nextDay0 = TimeUtil.computeStartOfNextDay(now);
    nextDay0 += 99;
    
    // there should be one event the next day, avg should also be 1
    asi.refresh(nextDay0);
    assertEquals(1.0, asi.getLastDaysCount(), 0.01);
    assertEquals(1.0, asi.getDailyAverage(), 0.01);

    cf.decide(null); // 2nd event
    cf.decide(null); // 3rd event

    asi.refresh(nextDay0);
    assertEquals(1, asi.getLastDaysCount());
    assertEquals(1.0, asi.getDailyAverage(), 0.01);

    long nextDay1 = TimeUtil.computeStartOfNextDay(nextDay0) + 6747;
    asi.refresh(nextDay1);
    assertEquals(2, asi.getLastDaysCount());
    assertEquals(1.5, asi.getDailyAverage(), 0.01);

    nextDay1 += 4444;
    cf.decide(null); // 4th event
    cf.decide(null); // 5th event
    cf.decide(null); // 6th event
    cf.decide(null); // 7th event

    asi.refresh(nextDay1);
    // values should remain unchanged
    assertEquals(2, asi.getLastDaysCount());
    assertEquals(1.5, asi.getDailyAverage(), 0.01);

    
    long nextDay2 = TimeUtil.computeStartOfNextDay(nextDay1) + 11177;

    asi.refresh(nextDay2);
    // values should remain unchanged
    assertEquals(4, asi.getLastDaysCount());
    assertEquals(7.0/3, asi.getDailyAverage(), 0.01);
    

    
  }
}
