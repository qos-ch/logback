/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
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
 * powerful. 
 * 
 * <p>In order to use  <code>TimeBasedRollingPolicy</code>, the 
 * <b>FileNamePattern</b> option must be set. It basically specifies the name of the 
 * rolled log files. The value <code>FileNamePattern</code> should consist of 
 * the name of the file, plus a suitably placed <code>%d</code> conversion 
 * specifier. The <code>%d</code> conversion specifier may contain a date and 
 * time pattern as specified by the {@link java.text.SimpleDateFormat} class. If 
 * the date and time pattern is omitted, then the default pattern of 
 * "yyyy-MM-dd" is assumed. The following examples should clarify the point.
 *
 * <p>
 * <table cellspacing="5px" border="1">
 *   <tr>
 *     <th><code>FileNamePattern</code> value</th>
 *     <th>Roll-over schedule</th>
 *     <th>Example</th>
 *   </tr>
 *   <tr>
 *     <td nowrap="true"><code>/wombat/folder/foo.%d</code></td>
 *     <td>Daily rollover (at midnight).  Due to the omission of the optional 
 *         time and date pattern for the %d token specifier, the default pattern
 *         of "yyyy-MM-dd" is assumed, which corresponds to daily rollover.
 *     </td>
 *     <td>During November 23rd, 2004, logging output will go to 
 *       the file <code>/wombat/foo.2004-11-23</code>. At midnight and for
 *       the rest of the 24th, logging output will be directed to 
 *       <code>/wombat/foo.2004-11-24</code>. 
 *     </td>
 *   </tr>
 *   <tr>
 *     <td nowrap="true"><code>/wombat/foo.%d{yyyy-MM}.log</code></td>
 *     <td>Rollover at the beginning of each month.</td>
 *     <td>During the month of October 2004, logging output will go to
 *     <code>/wombat/foo.2004-10.log</code>. After midnight of October 31st 
 *     and for the rest of November, logging output will be directed to 
 *       <code>/wombat/foo.2004-11.log</code>.
 *     </td>
 *   </tr>
 * </table>
 * <h2>Automatic file compression</h2>
 * <code>TimeBasedRollingPolicy</code> supports automatic file compression. 
 * This feature is enabled if the value of the <b>FileNamePattern</b> option 
 * ends with <code>.gz</code> or <code>.zip</code>.
 * <p>
 * <table cellspacing="5px" border="1">
 *   <tr>
 *     <th><code>FileNamePattern</code> value</th>
 *     <th>Rollover schedule</th>
 *     <th>Example</th>
 *   </tr>
 *   <tr>
 *     <td nowrap="true"><code>/wombat/foo.%d.gz</code></td>
 *     <td>Daily rollover (at midnight) with automatic GZIP compression of the 
 *      arcived files.</td>
 *     <td>During November 23rd, 2004, logging output will go to 
 *       the file <code>/wombat/foo.2004-11-23</code>. However, at midnight that
 *       file will be compressed to become <code>/wombat/foo.2004-11-23.gz</code>.
 *       For the 24th of November, logging output will be directed to 
 *       <code>/wombat/folder/foo.2004-11-24</code> until its rolled over at the
 *       beginning of the next day.
 *     </td>
 *   </tr>
 * </table>
 * 
 * <h2>Decoupling the location of the active log file and the archived log files</h2>
 * <p>The <em>active file</em> is defined as the log file for the current period 
 * whereas <em>archived files</em> are those files which have been rolled over
 * in previous periods.
 * 
 * <p>By setting the <b>ActiveFileName</b> option you can decouple the location 
 * of the active log file and the location of the archived log files.
 * <p> 
 *  <table cellspacing="5px" border="1">
 *   <tr>
 *     <th><code>FileNamePattern</code> value</th>
 *     <th>ActiveFileName</th>
 *     <th>Rollover schedule</th>
 *     <th>Example</th>
 *   </tr>
 *   <tr>
 *     <td nowrap="true"><code>/wombat/foo.log.%d</code></td>
 *     <td nowrap="true"><code>/wombat/foo.log</code></td>
 *     <td>Daily rollover.</td>
 * 
 *     <td>During November 23rd, 2004, logging output will go to 
 *       the file <code>/wombat/foo.log</code>. However, at midnight that file 
 *       will archived as <code>/wombat/foo.log.2004-11-23</code>. For the 24th
 *       of November, logging output will be directed to 
 *       <code>/wombat/folder/foo.log</code> until its archived as 
 *       <code>/wombat/foo.log.2004-11-24</code> at the beginning of the next 
 *       day.
 *     </td>
 *   </tr>
 * </table>
 * <p>
 * @author Ceki G&uuml;lc&uuml;
 */
public class TimeBasedRollingPolicy extends RollingPolicyBase implements TriggeringPolicy {
  static final String FNP_NOT_SET =
    "The FileNamePattern option must be set before using TimeBasedRollingPolicy. ";
  static final String SEE_FNP_NOT_SET =
    "See also http://www.logback.com/doc/codes.html#tbr_fnp_not_set";
  RollingCalendar rc;
  long nextCheck;
  Date lastCheck = new Date();
  String elapsedPeriodsFileName;
  FileNamePattern activeFileNamePattern;
  RenameUtil util = new RenameUtil();
  Compress compress = new Compress();
  
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
    //getLogger().debug("Next check set to: " + nc);  
  }

  public void rollover() throws RolloverFailure {
    addInfo("roll-over called");
    addInfo("compressionMode: " + compressionMode);

    if (activeFileName == null) {
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
        util.rename(activeFileName, elapsedPeriodsFileName);
        break;
      case Compress.GZ:
        addInfo("GZIP compressing ["+elapsedPeriodsFileName+"]");
        compress.GZCompress(activeFileName, elapsedPeriodsFileName);
        break;
      case Compress.ZIP:
        addInfo("ZIP compressing ["+elapsedPeriodsFileName+"]");
        compress.ZIPCompress(activeFileName, elapsedPeriodsFileName);
        break;
      }
    }
  }

  /**
  *
  * The active log file is determined by the value of the activeFileName
  * option if it is set. However, in case the activeFileName is left blank,
  * then, the active log file equals the file name for the current period
  * as computed by the <b>FileNamePattern</b> option.
  *
  */
  public String getActiveFileName() {
    if (activeFileName == null) {
      return activeFileNamePattern.convertDate(lastCheck);
    } else {
      return activeFileName;
    }
  }

  public boolean isTriggeringEvent(File file, final Object event) {
    //getLogger().debug("Is triggering event called");
    long n = System.currentTimeMillis();

    if (n >= nextCheck) {
      addInfo("Time to trigger roll-over");

      // We set the elapsedPeriodsFileName before we set the 'lastCheck' variable
      // The elapsedPeriodsFileName corresponds to the file name of the period
      // that just elapsed.
      elapsedPeriodsFileName = activeFileNamePattern.convertDate(lastCheck);
      addInfo("elapsedPeriodsFileName set to "+elapsedPeriodsFileName);

      lastCheck.setTime(n);
      //getLogger().debug("ActiveLogFileName will return " + getActiveLogFileName());
      nextCheck = rc.getNextCheckMillis(lastCheck);

      Date x = new Date();
      x.setTime(nextCheck);
      addInfo("Next check on "+ x);

      return true;
    } else {
      return false;
    }
  }
}
