/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.rolling;

import java.io.File;
import java.util.Date;

import ch.qos.logback.core.rolling.helper.DefaultArchiveRemover;

/**
 * 
 * @author Ceki G&uuml;lc&uuml;
 *
 * @param <E>
 */
public class DefaultTimeBasedFileNamingAndTriggeringPolicy<E> extends TimeBasedFileNamingAndTriggeringPolicyBase<E> {

  
  
  @Override
  public void start() {
    super.start();
    archiveRemover = new DefaultArchiveRemover(tbrp.fileNamePattern, rc);
    started = true;
  }
  
  public boolean isTriggeringEvent(File activeFile, final E event) {
    long time = getCurrentTime();
    if (time >= nextCheck) {
      Date dateOfElapsedPeriod = dateInCurrentPeriod;
      elapsedPeriodsFileName = tbrp.fileNamePatternWCS
          .convert(dateOfElapsedPeriod);
      updateDateInCurrentPeriod(time);
      computeNextCheck();
      return true;
    } else {
      return false;
    }
  }
}
