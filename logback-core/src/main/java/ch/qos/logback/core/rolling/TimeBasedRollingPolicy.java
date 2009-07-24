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
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.rolling.helper.RenameUtil;
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

  FileNamePattern activeFileNamePattern;
  Compressor compressor;
  RenameUtil renameUtil = new RenameUtil();
  Future<?> future;

  int maxHistory = NO_DELETE_HISTORY;
  TimeBasedCleaner tbCleaner;

  DefaultTimeBasedTriggeringPolicy<E> timeBasedTriggering = new DefaultTimeBasedTriggeringPolicy<E>();
  
//  public void setCurrentTime(long timeInMillis) {
//    currentTime = timeInMillis;
//    isTimeForced = true;
//  }
//
//  public long getCurrentTime() {
//    // if time is forced return the time set by user
//    if (isTimeForced) {
//      return currentTime;
//    } else {
//      return System.currentTimeMillis();
//    }
//  }

  public void start() {
    // set the LR for our utility object
    renameUtil.setContext(this.context);

    timeBasedTriggering.setContext(context);
    
    // find out period from the filename pattern
    if (fileNamePatternStr != null) {
      fileNamePattern = new FileNamePattern(fileNamePatternStr, this.context);
      timeBasedTriggering.fileNamePattern = fileNamePattern;
      determineCompressionMode();
    } else {
      addWarn(FNP_NOT_SET);
      addWarn(CoreConstants.SEE_FNP_NOT_SET);
      throw new IllegalStateException(FNP_NOT_SET
          + CoreConstants.SEE_FNP_NOT_SET);
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

    timeBasedTriggering.activeFileNamePattern = activeFileNamePattern;
    timeBasedTriggering.start();
    
    if (maxHistory != NO_DELETE_HISTORY) {
      tbCleaner = new TimeBasedCleaner(fileNamePattern, timeBasedTriggering.rc, maxHistory);
    }
  }


  public long getCurrentTime() {
    return timeBasedTriggering.getCurrentTime();
  }
  public void setCurrentTime(long timeInMillis) {
    timeBasedTriggering.setCurrentTime(timeInMillis);
  }
  void setLastCheck(Date _lastCheck) {
    timeBasedTriggering.setLastCheck(_lastCheck);
  }
  
  public void rollover() throws RolloverFailure {

    // when rollover is called the elapsed period's file has
    // been already closed. This is a working assumption of this method.

    String elapsedPeriodsFileName = timeBasedTriggering.getElapsedPeriodsFileName();
    
    if (compressionMode == CompressionMode.NONE) {
      if (getParentsRawFileProperty() != null) {
        renameUtil.rename(getParentsRawFileProperty(), elapsedPeriodsFileName);
      }
    } else {
      if (getParentsRawFileProperty() == null) {
        future = asyncCompress(elapsedPeriodsFileName, elapsedPeriodsFileName);
      } else {
        future = renamedRawAndAsyncCompress(elapsedPeriodsFileName);
      }
    }

    if (tbCleaner != null) {
      tbCleaner.clean(new Date(timeBasedTriggering.getCurrentTime()));
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
    renameUtil.rename(parentsRawFile, tmpTarget);
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
    
      return activeFileNamePattern.convertDate(timeBasedTriggering.lastCheck);
    
  }

//  // get the active file name for the current (latest) period
//  private String getLatestPeriodsFileName() {
//    return activeFileNamePattern.convertDate(lastCheck);
//  }

  public boolean isTriggeringEvent(File activeFile, final E event) {
    
    return timeBasedTriggering.isTriggeringEvent(activeFile, event);
    
//    long time = getCurrentTime();
//
//    if (time >= nextCheck) {
//      // We set the elapsedPeriodsFileName before we set the 'lastCheck'
//      // variable
//      // The elapsedPeriodsFileName corresponds to the file name of the period
//      // that just elapsed.
//      elapsedPeriodsFileName = activeFileNamePattern.convertDate(lastCheck);
//
//      lastCheck.setTime(time);
//      nextCheck = rc.getNextTriggeringMillis(lastCheck);
//      return true;
//    } else {
//      return false;
//    }
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
