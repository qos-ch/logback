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
  
  public void testSelectDurationUnitForDisplay() throws InterruptedException {
    assertEquals(DurationUnit.NANOSECOND, Util.selectDurationUnitForDisplay(10));
    assertEquals(DurationUnit.NANOSECOND, Util.selectDurationUnitForDisplay(9*Util.NANOS_IN_ONE_MICROSECOND));
    assertEquals(DurationUnit.MICROSECOND, Util.selectDurationUnitForDisplay(11*Util.NANOS_IN_ONE_MICROSECOND));
    assertEquals(DurationUnit.MICROSECOND, Util.selectDurationUnitForDisplay(9*Util.NANOS_IN_ONE_MILLISECOND));
    assertEquals(DurationUnit.MILLISSECOND, Util.selectDurationUnitForDisplay(11*Util.NANOS_IN_ONE_MILLISECOND));
    assertEquals(DurationUnit.SECOND, Util.selectDurationUnitForDisplay(11*Util.NANOS_IN_ONE_SECOND));
  }
  
}
