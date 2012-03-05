package ch.qos.logback.core.rolling;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class TimeBasedFileNamingAndTriggeringPolicyBaseTest {

  static long MILLIS_IN_MINUTE = 60*1000;

  Context context = new ContextBase();
  RollingFileAppender rfa = new RollingFileAppender();
  TimeBasedRollingPolicy tbrp = new TimeBasedRollingPolicy();
  DefaultTimeBasedFileNamingAndTriggeringPolicy timeBasedFNATP = new DefaultTimeBasedFileNamingAndTriggeringPolicy();

  @Before
  public void setUp() {
    rfa.setContext(context);
    tbrp.setContext(context);
    timeBasedFNATP.setContext(context);

    rfa.setRollingPolicy(tbrp);
    tbrp.setParent(rfa);
    tbrp.setTimeBasedFileNamingAndTriggeringPolicy(timeBasedFNATP);
    timeBasedFNATP.setTimeBasedRollingPolicy(tbrp);
  }

  @Test
  public void singleDate() {
    // Tuesday December 20th 17:59:01 CET 2011
    long startTime = 1324400341553L;
    tbrp.setFileNamePattern("foo-%d{yyyy-MM'T'mm}.log");
    tbrp.start();

    timeBasedFNATP.setCurrentTime(startTime);
    timeBasedFNATP.start();

    timeBasedFNATP.setCurrentTime(startTime+MILLIS_IN_MINUTE);
    timeBasedFNATP.isTriggeringEvent(null, null);
    String elapsedPeriodsFileName = timeBasedFNATP.getElapsedPeriodsFileName();
    assertEquals("foo-2011-12T59.log", elapsedPeriodsFileName);
  }

  // see "log rollover should be configurable using %d multiple times in file name pattern"
  // http://jira.qos.ch/browse/LBCORE-242

  @Test
  public void multiDate() {
    // Tuesday December 20th 17:59:01 CET 2011
    long startTime = 1324400341553L;
    tbrp.setFileNamePattern("foo-%d{yyyy-MM, AUX}/%d{mm}.log");
    tbrp.start();

    timeBasedFNATP.setCurrentTime(startTime);
    timeBasedFNATP.start();

    timeBasedFNATP.setCurrentTime(startTime+MILLIS_IN_MINUTE);
    timeBasedFNATP.isTriggeringEvent(null, null);
    String elapsedPeriodsFileName = timeBasedFNATP.getElapsedPeriodsFileName();
    assertEquals("foo-2011-12/59.log", elapsedPeriodsFileName);
  }

}
