package ch.qos.logback.core.rolling;

import java.io.File;
import java.util.Date;

import ch.qos.logback.core.rolling.helper.DateTokenConverter;
import ch.qos.logback.core.rolling.helper.RollingCalendar;
import ch.qos.logback.core.spi.ContextAwareBase;

public class DefaultTimeBasedFileNamingAndTriggeringPolicy<E> extends ContextAwareBase
    implements TimeBasedFileNamingAndTriggeringPolicy<E> {

  private TimeBasedRollingPolicy<E> tbrp;
  private String elapsedPeriodsFileName;
  private RollingCalendar rc;
  private long currentTime;
  private long nextCheck;
  // indicate whether the time has been forced or not
  private boolean isTimeForced = false;
  private Date dateInCurrentPeriod = null;
  boolean started = false;

  public boolean isStarted() {
    return started;
  }

  public void start() {

    DateTokenConverter dtc = tbrp.fileNamePattern.getDateTokenConverter();

    if (dtc == null) {
      throw new IllegalStateException("FileNamePattern ["
          + tbrp.fileNamePattern.getPattern()
          + "] does not contain a valid DateToken");
    }

    rc = new RollingCalendar();
    rc.init(dtc.getDatePattern());
    addInfo("The date pattern is '" + dtc.getDatePattern()
        + "' from file name pattern '" + tbrp.fileNamePattern.getPattern()
        + "'.");
    rc.printPeriodicity(this);

    // dateInCurrentPeriod can be set by test classes
    // if it has not been set, we set it here
    if (dateInCurrentPeriod == null) {
      dateInCurrentPeriod = new Date();
      updateDateInCurrentPeriod(getCurrentTime());
    }
    computeNextCheck();
  }
  
  public void stop() {
    started = false;
  }
  
  private void computeNextCheck() {
    nextCheck = rc.getNextTriggeringMillis(dateInCurrentPeriod);
  }

  // allow Test classes to act on the dateInCurrentPeriod field to simulate old
  // log files needing rollover
  public void setDateInCurrentPeriod(Date _dateInCurrentPeriod) {
    this.dateInCurrentPeriod = _dateInCurrentPeriod;
  }

  public Date getDateInCurrentPeriod() {
    return dateInCurrentPeriod;
  }

  public String getElapsedPeriodsFileName() {
    return elapsedPeriodsFileName;
  }

  public String getCurrentPeriodsFileNameWithoutCompressionSuffix() {
    return tbrp.fileNamePatternWCS.convertDate(dateInCurrentPeriod);
  }

  public boolean isTriggeringEvent(File activeFile, final E event) {
    long time = getCurrentTime();

    if (time >= nextCheck) {
      Date dateInElapsedPeriod = dateInCurrentPeriod;
      elapsedPeriodsFileName = tbrp.fileNamePatternWCS
          .convertDate(dateInElapsedPeriod);
      updateDateInCurrentPeriod(time);
      computeNextCheck();
      return true;
    } else {
      return false;
    }
  }

  private void updateDateInCurrentPeriod(long now) {
    dateInCurrentPeriod.setTime(now);
  }

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
  
  public void setTimeBasedRollingPolicy(TimeBasedRollingPolicy<E> _tbrp) {
    this.tbrp = _tbrp;

  }

  public RollingCalendar getRollingCalendar() {
    return rc;
  }

}
