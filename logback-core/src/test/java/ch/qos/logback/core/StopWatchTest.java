package ch.qos.logback.core;

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
      long d = sw.startTime+9*StopWatch.NANOS_IN_ONE_MICROSECOND;
      String s = sw.stop(d).toString();
      assertTrue(s.endsWith("9000 nanoseconds."));
    }

    { 
      long d = sw.startTime+11*StopWatch.NANOS_IN_ONE_MICROSECOND;
      String s = sw.stop(d).toString();
      assertTrue(s.endsWith("11 microseconds."));
    }
    { 
      long d = sw.startTime+9*StopWatch.NANOS_IN_ONE_MILLISECOND;
      String s = sw.stop(d).toString();
      assertTrue(s.endsWith("9000 microseconds."));
    }
    { 
      long d = sw.startTime+3*StopWatch.NANOS_IN_ONE_SECOND;
      String s = sw.stop(d).toString();
      assertTrue(s.endsWith("3000 milliseconds."));
    }    
    { 
      long d = sw.startTime+6*StopWatch.NANOS_IN_ONE_SECOND;
      String s = sw.stop(d).toString();
      assertTrue(s.endsWith("6.000 milliseconds."));
    }    
  }

  public void testSelectDurationUnitForDisplay() throws InterruptedException {
    StopWatch sw = new StopWatch("testBasic");
    assertEquals(StopWatch.DurationUnit.NANOSECOND, sw.selectDurationUnitForDisplay(10));
    assertEquals(StopWatch.DurationUnit.NANOSECOND, sw.selectDurationUnitForDisplay(9*StopWatch.NANOS_IN_ONE_MICROSECOND));
    assertEquals(StopWatch.DurationUnit.MICROSECOND, sw.selectDurationUnitForDisplay(11*StopWatch.NANOS_IN_ONE_MICROSECOND));
    assertEquals(StopWatch.DurationUnit.MICROSECOND, sw.selectDurationUnitForDisplay(9*StopWatch.NANOS_IN_ONE_MILLISECOND));
    assertEquals(StopWatch.DurationUnit.MILLISSECOND, sw.selectDurationUnitForDisplay(11*StopWatch.NANOS_IN_ONE_MILLISECOND));
    assertEquals(StopWatch.DurationUnit.MILLISSECOND, sw.selectDurationUnitForDisplay(3*StopWatch.NANOS_IN_ONE_SECOND));
    assertEquals(StopWatch.DurationUnit.SECOND, sw.selectDurationUnitForDisplay(6*StopWatch.NANOS_IN_ONE_SECOND));
  }
  
}
