package ch.qos.logback.classic.stopwatch;

import junit.framework.TestCase;

public class StopWatchTest extends TestCase {

  public StopWatchTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testBasic() throws InterruptedException {
    StopWatch sw = new StopWatch("testBasic");
   
    { 
      long d = sw.startTime+10;
      String s = sw.stop(d).toString();
      assertTrue(s.endsWith("10 nanoseconds."));
    }
    
    { 
      long d = sw.startTime+9*Util.NANOS_IN_ONE_MICROSECOND;
      String s = sw.stop(d).toString();
      assertTrue(s.endsWith("9000 nanoseconds."));
    }

    { 
      long d = sw.startTime+11*Util.NANOS_IN_ONE_MICROSECOND;
      String s = sw.stop(d).toString();
      assertTrue(s.endsWith("11 microseconds."));
    }
    { 
      long d = sw.startTime+9*Util.NANOS_IN_ONE_MILLISECOND;
      String s = sw.stop(d).toString();
      assertTrue(s.endsWith("9000 microseconds."));
    }
    { 
      long d = sw.startTime+3*Util.NANOS_IN_ONE_SECOND;
      String s = sw.stop(d).toString();
      System.out.println(s);
      assertTrue(s.endsWith("3.000 seconds."));
    } 
  }

  public void testSelectDurationUnitForDisplay() throws InterruptedException {
    assertEquals(DurationUnit.NANOSECOND, Util.selectDurationUnitForDisplay(10));
    assertEquals(DurationUnit.NANOSECOND, Util.selectDurationUnitForDisplay(9*Util.NANOS_IN_ONE_MICROSECOND));
    assertEquals(DurationUnit.MICROSECOND, Util.selectDurationUnitForDisplay(11*Util.NANOS_IN_ONE_MICROSECOND));
    assertEquals(DurationUnit.MICROSECOND, Util.selectDurationUnitForDisplay(9*Util.NANOS_IN_ONE_MILLISECOND));
    assertEquals(DurationUnit.MILLISSECOND, Util.selectDurationUnitForDisplay(11*Util.NANOS_IN_ONE_MILLISECOND));
    assertEquals(DurationUnit.SECOND, Util.selectDurationUnitForDisplay(3*Util.NANOS_IN_ONE_SECOND));
  }
  
}
