/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.rolling;

import java.io.File;
import java.util.Date;
import java.util.concurrent.Future;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.rolling.helper.AsynchronousCompressor;
import ch.qos.logback.core.rolling.helper.CompressionMode;
import ch.qos.logback.core.rolling.helper.Compressor;
import ch.qos.logback.core.rolling.helper.DateTokenConverter;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.rolling.helper.RenameUtil;
import ch.qos.logback.core.rolling.helper.RollingCalendar;
import ch.qos.logback.core.rolling.helper.TimeBasedCleaner;

/**
 * <code>TimeBasedRollingPolicy</code> is both easy to configure and quite
 * powerful. It allows the roll over to be made based on time. It is possible to
 * specify that the roll over occur once per day, per week or per month.
 * 
 * <p>For more information, please refer to the online manual at
 * http://logback.qos.ch/manual/appenders.html#TimeBasedRollingPolicy
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class TimeBasedRollingPolicy<E> extends RollingPolicyBase implements
    TriggeringPolicy<E> {
  static final String FNP_NOT_SET = "The FileNamePattern option must be set before using TimeBasedRollingPolicy. ";
  static final int NO_DELETE_HISTORY = 0;

  RollingCalendar rc;
  long currentTime;
  long nextCheck;
  // indicate whether the time has been forced or not
  boolean isTimeForced = false;
  Date lastCheck = null;
  String elapsedPeriodsFileName;
  FileNamePattern activeFileNamePattern;
  Compressor compressor;
  RenameUtil util = new RenameUtil();
  Future<?> future;

  int maxHistory = NO_DELETE_HISTORY;
  TimeBasedCleaner tbCleaner;

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

  public void start() {
    // set the LR for our utility object
    util.setContext(this.context);

    // find out period from the filename pattern
    if (fileNamePatternStr != null) {
      fileNamePattern = new FileNamePattern(fileNamePatternStr, this.context);
      determineCompressionMode();
    } else {
      addWarn(FNP_NOT_SET);
      addWarn(CoreConstants.SEE_FNP_NOT_SET);
      throw new IllegalStateException(FNP_NOT_SET
          + CoreConstants.SEE_FNP_NOT_SET);
    }

    DateTokenConverter dtc = fileNamePattern.getDateTokenConverter();

    if (dtc == null) {
      throw new IllegalStateException("FileNamePattern ["
          + fileNamePattern.getPattern()
          + "] does not contain a valid DateToken");
    }

    compressor = new Compressor(compressionMode);
    compressor.setContext(context);

    int len = fileNamePatternStr.length();
    switch (compressionMode) {
    case GZ:
      activeFileNamePattern = new FileNamePattern(fileNamePatternStr.substring(
          0, len - 3), this.context);
      ;
      break;
    case ZIP:
      activeFileNamePattern = new FileNamePattern(fileNamePatternStr.substring(
          0, len - 4), this.context);
      break;
    case NONE:
      activeFileNamePattern = fileNamePattern;
    }

    addInfo("Will use the pattern " + activeFileNamePattern
        + " for the active file");

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
      if (getParentsRawFileProperty() != null) {
        File currentFile = new File(getParentsRawFileProperty());
        if (currentFile.exists() && currentFile.canRead()) {
          lastCheck.setTime(currentFile.lastModified());
        }
      }
    }
    nextCheck = rc.getNextTriggeringMillis(lastCheck);

    if (maxHistory != NO_DELETE_HISTORY) {
      tbCleaner = new TimeBasedCleaner(fileNamePattern, rc, maxHistory);
    }
  }

  // allow Test classes to act on the lastCheck field to simulate old
  // log files needing rollover
  void setLastCheck(Date _lastCheck) {
    this.lastCheck = _lastCheck;
  }

  boolean rolloverTargetIsParentFile() {
    return (getParentsRawFileProperty() != null && getParentsRawFileProperty()
        .equals(elapsedPeriodsFileName));
  }

  public void rollover() throws RolloverFailure {

    // when rollover is called the elapsed period's file has
    // been already closed. This is a working assumption of this method.

    if (compressionMode == CompressionMode.NONE) {
      if (getParentsRawFileProperty() != null) {
        util.rename(getParentsRawFileProperty(), elapsedPeriodsFileName);
      }
    } else {
      if (getParentsRawFileProperty() == null) {
        future = asyncCompress(elapsedPeriodsFileName, elapsedPeriodsFileName);
      } else {
        future = renamedRawAndAsyncCompress(elapsedPeriodsFileName);
      }
    }

    if (tbCleaner != null) {
      tbCleaner.clean(new Date(getCurrentTime()));
    }
  }

  Future asyncCompress(String nameOfFile2Compress,
      String nameOfCompressedFile) throws RolloverFailure {
    AsynchronousCompressor ac = new AsynchronousCompressor(compressor);
    return ac.compressAsynchronously(nameOfFile2Compress,
        nameOfCompressedFile);
  }

  Future renamedRawAndAsyncCompress(String nameOfCompressedFile) throws RolloverFailure {
    String parentsRawFile = getParentsRawFileProperty();
    String tmpTarget = parentsRawFile + System.nanoTime() + ".tmp";
    util.rename(parentsRawFile, tmpTarget);
    return asyncCompress(tmpTarget, nameOfCompressedFile);
  }

  /**
   * 
   * The active log file is determined by the value of the parent's filename
   * option. However, in case the file name is left blank, then, the active log
   * file equals the file name for the current period as computed by the
   * <b>FileNamePattern</b> option.
   * 
   * <p>The RollingPolicy must know whether it is responsible for changing the
   * name of the active file or not. If the active file name is set by the user
   * via the configuration file, then the RollingPolicy must let it like it is.
   * If the user does not specify an active file name, then the RollingPolicy
   * generates one.
   * 
   * <p> To be sure that the file name used by the parent class has been
   * generated by the RollingPolicy and not specified by the user, we keep track
   * of the last generated name object and compare its reference to the parent
   * file name. If they match, then the RollingPolicy knows it's responsible for
   * the change of the file name.
   * 
   */
  public String getActiveFileName() {
    String parentsRawFileProperty = getParentsRawFileProperty();

    if (parentsRawFileProperty != null) {
      return parentsRawFileProperty;
    } else {
      return getLatestPeriodsFileName();
    }
  }

  // get the active file name for the current (latest) period
  private String getLatestPeriodsFileName() {
    return activeFileNamePattern.convertDate(lastCheck);
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

  /**
   * Get the number of archive files to keep.
   * 
   * @return number of archive files to keep
   */
  public int getMaxHistory() {
    return maxHistory;
  }

  /**
   * Set the maximum number of archive files to keep.
   * 
   * @param maxHistory
   *                number of archive files to keep
   */
  public void setMaxHistory(int maxHistory) {
    this.maxHistory = maxHistory;
  }

  @Override
  public String toString() {
    return "c.q.l.core.rolling.TimeBasedRollingPolicy";
  }

}
