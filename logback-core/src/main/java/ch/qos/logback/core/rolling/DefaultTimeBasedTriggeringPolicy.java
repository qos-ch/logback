package ch.qos.logback.core.rolling;

import java.io.File;
import java.util.Date;

import ch.qos.logback.core.rolling.helper.DateTokenConverter;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.rolling.helper.RollingCalendar;
import ch.qos.logback.core.spi.ContextAwareBase;

public class DefaultTimeBasedTriggeringPolicy<E> extends ContextAwareBase
    implements NamingAndTriggeringPolicy<E> {

  FileNamePattern fileNamePattern;
  String elapsedPeriodsFileName;
  FileNamePattern activeFileNamePattern;
  RollingCalendar rc;
  long currentTime;
  long nextCheck;
  // indicate whether the time has been forced or not
  boolean isTimeForced = false;
  Date lastCheck = null;
  boolean started = false;

  public void setCurrentTime(long timeInMillis) {
    currentTime = timeInMillis;
    isTimeForced = true;
  }

  public long getCurrentTime() {
    // if time is forced return the time set by user
    if (isTimeForced) {
      return currentTime;
    } else {
      return System.currentTimeMillis();
    }
  }

  public boolean isStarted() {
    return started;
  }

  public void start() {

    DateTokenConverter dtc = fileNamePattern.getDateTokenConverter();

    if (dtc == null) {
      throw new IllegalStateException("FileNamePattern ["
          + fileNamePattern.getPattern()
          + "] does not contain a valid DateToken");
    }

    rc = new RollingCalendar();
    rc.init(dtc.getDatePattern());
    addInfo("The date pattern is '" + dtc.getDatePattern()
        + "' from file name pattern '" + fileNamePattern.getPattern() + "'.");
    rc.printPeriodicity(this);

    // lastCheck can be set by test classes
    // if it has not been set, we set it here
    if (lastCheck == null) {
      lastCheck = new Date();
      lastCheck.setTime(getCurrentTime());
    }
    nextCheck = rc.getNextTriggeringMillis(lastCheck);

  }

  public void stop() {
    started = false;
  }

  // allow Test classes to act on the lastCheck field to simulate old
  // log files needing rollover
  void setLastCheck(Date _lastCheck) {
    this.lastCheck = _lastCheck;
  }

  public String getElapsedPeriodsFileName() {
    return elapsedPeriodsFileName;
  }

  public boolean isTriggeringEvent(File activeFile, final E event) {
    long time = getCurrentTime();

    if (time >= nextCheck) {
      // We set the elapsedPeriodsFileName before we set the 'lastCheck'
      // variable
      // The elapsedPeriodsFileName corresponds to the file name of the period
      // that just elapsed.
      elapsedPeriodsFileName = activeFileNamePattern.convertDate(lastCheck);

      lastCheck.setTime(time);
      nextCheck = rc.getNextTriggeringMillis(lastCheck);
      return true;
    } else {
      return false;
    }
  }

}
