package ch.qos.logback.core.rolling;

import java.io.File;
import java.util.Date;

/**
 * This class is used to simulate the elapsed time during TimeBasedRollingTest.
 * Overload start() and isTriggeringEvent() from TimeBasedRollingPolicy in order
 * to replace current time.
 * 
 * @author Jean-Noel Charpin
 */
public class TimeBasedRollingPolicyTest extends TimeBasedRollingPolicy {

  private long simulatedTime;

  public void setSimulatedTime(long timeInMillis) {
    simulatedTime = timeInMillis;
  }

  public long getSimulatedTime() {
    return simulatedTime;
  }

  public void start() {
    super.start();
    lastCheck.setTime(simulatedTime);
    nextCheck = rc.getNextCheckMillis(lastCheck);
  }

  public boolean isTriggeringEvent(File activeFile, final Object event) {
    if (simulatedTime >= nextCheck) {
      // addInfo("Time to trigger roll-over");
      // We set the elapsedPeriodsFileName before we set the 'lastCheck'
      // variable
      // The elapsedPeriodsFileName corresponds to the file name of the period
      // that just elapsed.
      elapsedPeriodsFileName = activeFileNamePattern.convertDate(lastCheck);
      // addInfo("elapsedPeriodsFileName set to "+elapsedPeriodsFileName);
      lastCheck.setTime(simulatedTime);
      nextCheck = rc.getNextCheckMillis(lastCheck);

      Date x = new Date();
      x.setTime(nextCheck);
      // addInfo("Next check on "+ x);

      return true;
    } else {
      return false;
    }
  }

}
