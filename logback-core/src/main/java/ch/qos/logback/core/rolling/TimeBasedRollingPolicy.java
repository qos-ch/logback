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

import ch.qos.logback.core.rolling.helper.Compress;
import ch.qos.logback.core.rolling.helper.DateTokenConverter;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.rolling.helper.RenameUtil;
import ch.qos.logback.core.rolling.helper.RollingCalendar;

/**
 * <code>TimeBasedRollingPolicy</code> is both easy to configure and quite 
 * powerful. It allows the rollover to be made based on time conditions. 
 * It is possible to specify that the rollover must occur each day, or month, for example.
 * 
 *  For more informations about this policy, please refer to the online manual at
 *  http://logback.qos.ch/manual/appenders.html#TimeBasedRollingPolicy
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class TimeBasedRollingPolicy extends RollingPolicyBase implements TriggeringPolicy {
  static final String FNP_NOT_SET =
    "The FileNamePattern option must be set before using TimeBasedRollingPolicy. ";
  static final String SEE_FNP_NOT_SET =
    "See also http://logback.qos.ch/doc/codes.html#tbr_fnp_not_set";
  RollingCalendar rc;
  long nextCheck;
  Date lastCheck = new Date();
  String elapsedPeriodsFileName;
  FileNamePattern activeFileNamePattern;
  RenameUtil util = new RenameUtil();
  Compress compress = new Compress();
  String lastGeneratedFileName;
  
  public void start() {
    // set the LR for our utility object
    util.setContext(this.context);
    compress.setContext(this.context);
    
    // find out period from the filename pattern
    if (fileNamePatternStr != null) {
      fileNamePattern = new FileNamePattern(fileNamePatternStr, this.context);
      determineCompressionMode();
    } else {
      addWarn(FNP_NOT_SET);
      addWarn(SEE_FNP_NOT_SET);
      throw new IllegalStateException(FNP_NOT_SET + SEE_FNP_NOT_SET);
    }

    DateTokenConverter dtc = fileNamePattern.getDateTokenConverter();

    if (dtc == null) {
      throw new IllegalStateException(
        "FileNamePattern [" + fileNamePattern.getPattern()
        + "] does not contain a valid DateToken");
    }

    int len = fileNamePatternStr.length();
    switch(compressionMode) {
    case Compress.GZ:
      activeFileNamePattern =
        new FileNamePattern(fileNamePatternStr.substring(0, len - 3), this.context);
        
      break;
      case Compress.ZIP:
        activeFileNamePattern =
          new FileNamePattern(fileNamePatternStr.substring(0, len - 4), this.context);
        break;
       case Compress.NONE:
        activeFileNamePattern = fileNamePattern;
     }
     addInfo("Will use the pattern "+activeFileNamePattern+" for the active file");
   
    rc = new RollingCalendar();
    rc.init(dtc.getDatePattern());
    addInfo("The date pattern is '"+dtc.getDatePattern()+"' from file name pattern '"+
        fileNamePattern.getPattern()+"'.");
    rc.printPeriodicity(this);

    long n = System.currentTimeMillis();
    lastCheck.setTime(n);
    nextCheck = rc.getNextCheckMillis(lastCheck);

    //Date nc = new Date();
    //nc.setTime(nextCheck);
  }

  public void rollover() throws RolloverFailure {
    //addInfo("roll-over called");
    //addInfo("compressionMode: " + compressionMode);

    if (getParentFileName() == null) {
      switch (compressionMode) {
      case Compress.NONE:
        // nothing to do;
        break;
      case Compress.GZ:
        addInfo("GZIP compressing ["+elapsedPeriodsFileName+"].");
        compress.GZCompress(elapsedPeriodsFileName);
        break;
      case Compress.ZIP:
        addInfo("ZIP compressing ["+elapsedPeriodsFileName+"]");
        compress.ZIPCompress(elapsedPeriodsFileName);
        break;
      }
    } else {
      switch (compressionMode) {
      case Compress.NONE:
        util.rename(getParentFileName(), elapsedPeriodsFileName);
        break;
      case Compress.GZ:
        addInfo("GZIP compressing ["+elapsedPeriodsFileName+"]");
        compress.GZCompress(getParentFileName(), elapsedPeriodsFileName);
        break;
      case Compress.ZIP:
        addInfo("ZIP compressing ["+elapsedPeriodsFileName+"]");
        compress.ZIPCompress(getParentFileName(), elapsedPeriodsFileName);
        break;
      }
    }
    
    //let's update the parent active file name
    setParentFileName(getNewActiveFileName());
    
  }

  /**
   *
   * The active log file is determined by the value of the parent's filename
   * option. However, in case the file name is left blank,
   * then, the active log file equals the file name for the current period
   * as computed by the <b>FileNamePattern</b> option.
   * 
   * The RollingPolicy must know wether it is responsible for
   * changing the name of the active file or not. If the active file
   * name is set by the user via the configuration file, then the
   * RollingPolicy must let it like it is. If the user does not
   * specify an active file name, then the RollingPolicy generates one.
   * 
   * To be sure that the file name used by the parent class has been
   * generated by the RollingPolicy and not specified by the user, we
   * keep track of the last generated name object and compare its
   * reference to the parent file name. If they match, then
   * the RollingPolicy knows it's responsible for the change of the
   * file name.
   * 
   */
  public String getNewActiveFileName() {
    if (getParentFileName() == null || getParentFileName() == lastGeneratedFileName) {
      String newName = activeFileNamePattern.convertDate(lastCheck);
      addInfo("Generated a new name for RollingFileAppender: " + newName);
      lastGeneratedFileName = newName;
      return newName;
    } else {
      return getParentFileName();
    }
  }

  public boolean isTriggeringEvent(File activeFile, final Object event) {
    long n = System.currentTimeMillis();

    if (n >= nextCheck) {
      //addInfo("Time to trigger roll-over");
      // We set the elapsedPeriodsFileName before we set the 'lastCheck' variable
      // The elapsedPeriodsFileName corresponds to the file name of the period
      // that just elapsed.
      elapsedPeriodsFileName = activeFileNamePattern.convertDate(lastCheck);
      //addInfo("elapsedPeriodsFileName set to "+elapsedPeriodsFileName);

      lastCheck.setTime(n);
      nextCheck = rc.getNextCheckMillis(lastCheck);

      Date x = new Date();
      x.setTime(nextCheck);
      //addInfo("Next check on "+ x);

      return true;
    } else {
      return false;
    }
  }  

  @Override
  public String toString() {
    return "c.q.l.core.rolling.TimeBasedRollingPolicy";
  }
  
}
