/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.rolling;

import java.io.File;
import java.util.Date;

import ch.qos.logback.core.rolling.helper.ArchiveRemover;
import ch.qos.logback.core.rolling.helper.DateTokenConverter;
import ch.qos.logback.core.rolling.helper.RollingCalendar;
import ch.qos.logback.core.spi.ContextAwareBase;

abstract public class TimeBasedFileNamingAndTriggeringPolicyBase<E> extends
    ContextAwareBase implements TimeBasedFileNamingAndTriggeringPolicy<E> {

  protected TimeBasedRollingPolicy<E> tbrp;

  protected ArchiveRemover archiveRemover = null;
  protected String elapsedPeriodsFileName;
  protected RollingCalendar rc;

  protected long currentTime;
  // indicate whether the time has been forced or not
  protected boolean isTimeForced = false;
  protected Date dateInCurrentPeriod = null;

  protected long nextCheck;
  protected boolean started = false;

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

    
    if (dateInCurrentPeriod == null) {
      setDateInCurrentPeriod(new Date(getCurrentTime()));
      
      if (tbrp.getParentsRawFileProperty() != null) {
        File currentFile = new File(tbrp.getParentsRawFileProperty());
        if (currentFile.exists() && currentFile.canRead()) {
          setDateInCurrentPeriod(new Date(currentFile.lastModified()));
        }
      }
    }
    computeNextCheck();
  }

  public void stop() {
    started = false;
  }

  protected void computeNextCheck() {
    nextCheck = rc.getNextTriggeringMillis(dateInCurrentPeriod);
  }

  protected void setDateInCurrentPeriod(long now) {
    dateInCurrentPeriod.setTime(now);
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
    return tbrp.fileNamePatternWCS.convert(dateInCurrentPeriod);
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

  public ArchiveRemover getArchiveRemover() {
    return archiveRemover;
  }

}
