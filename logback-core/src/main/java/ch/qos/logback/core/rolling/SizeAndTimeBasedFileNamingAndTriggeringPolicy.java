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

public class SizeAndTimeBasedFileNamingAndTriggeringPolicy<E> extends  TimeBasedFileNamingAndTriggeringPolicyBase<E> {


  @Override
  public void start() {
    super.start();
    started = true;
  }  

  public boolean isTriggeringEvent(File activeFile, final E event) {
    long time = getCurrentTime();

    if (time >= nextCheck) {
      Date dateInElapsedPeriod = dateInCurrentPeriod;
      elapsedPeriodsFileName = tbrp.fileNamePatternWCS
          .convert(dateInElapsedPeriod);
      updateDateInCurrentPeriod(time);
      computeNextCheck();
      return true;
    } else {
      return false;
    }
  }
}
