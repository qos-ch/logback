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

import ch.qos.logback.core.util.FileSize;

public class SizeAndTimeBasedFileNamingAndTriggeringPolicy<E> extends
    TimeBasedFileNamingAndTriggeringPolicyBase<E> {

  int currentPeriodsCounter = 0;
  FileSize maxFileSize;
  String maxFileSizeAsString;

  @Override
  public void start() {
    super.start();
    started = true;
  }

  // IMPORTANT: This field can be updated by multiple threads. It follows that
  // its values may *not* be incremented sequentially. However, we don't care
  // about the actual value of the field except that from time to time the
  // expression (invocationCounter++ & 0xF) == 0xF) should be true.
  private int invocationCounter;

  public boolean isTriggeringEvent(File activeFile, final E event) {
    long time = getCurrentTime();
    if (time >= nextCheck) {
      Date dateInElapsedPeriod = dateInCurrentPeriod;
      elapsedPeriodsFileName = tbrp.fileNamePatternWCS
          .convertMultipleArguments(dateInElapsedPeriod, currentPeriodsCounter);
      currentPeriodsCounter = 0;
      updateDateInCurrentPeriod(time);
      computeNextCheck();
      return true;
    }

    // for performance reasons, check for changes every 16 invocations
    if (((invocationCounter++) & 0xF) != 0xF) {
      return false;
    }

    if (activeFile.length() >= maxFileSize.getSize()) {
      elapsedPeriodsFileName = tbrp.fileNamePatternWCS
          .convertMultipleArguments(dateInCurrentPeriod, currentPeriodsCounter);
      currentPeriodsCounter++;
      return true;
    }

    return false;
  }

  @Override
  public String getCurrentPeriodsFileNameWithoutCompressionSuffix() {
    return tbrp.fileNamePatternWCS.convertMultipleArguments(
        dateInCurrentPeriod, currentPeriodsCounter);
  }

  public String getMaxFileSize() {
    return maxFileSizeAsString;
  }

  public void setMaxFileSize(String maxFileSize) {
    this.maxFileSizeAsString = maxFileSize;
    this.maxFileSize = FileSize.valueOf(maxFileSize);
  }

}
