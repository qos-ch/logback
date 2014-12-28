/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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

import ch.qos.logback.core.joran.spi.NoAutoStart;
import ch.qos.logback.core.rolling.helper.ArchiveRemover;
import ch.qos.logback.core.rolling.helper.CompressionMode;
import ch.qos.logback.core.rolling.helper.FileFilterUtil;
import ch.qos.logback.core.rolling.helper.SizeAndTimeBasedArchiveRemover;
import ch.qos.logback.core.util.FileSize;

@NoAutoStart
public class SizeAndTimeBasedFNATP<E> extends
        TimeBasedFileNamingAndTriggeringPolicyBase<E> {

  int currentPeriodsCounter = 0;
  FileSize maxFileSize;
  String maxFileSizeAsString;

  @Override
  public void start() {
    // we depend on certain fields having been initialized
    // in super.start()
    super.start();

    archiveRemover = createArchiveRemover();
    archiveRemover.setContext(context);

    // we need to get the correct value of currentPeriodsCounter.
    // usually the value is 0, unless the appender or the application
    // is stopped and restarted within the same period
    String regex = tbrp.fileNamePattern.toRegexForFixedDate(dateInCurrentPeriod);
    String stemRegex = FileFilterUtil.afterLastSlash(regex);


    computeCurrentPeriodsHighestCounterValue(stemRegex);

    started = true;
  }

  protected ArchiveRemover createArchiveRemover() {
    return new SizeAndTimeBasedArchiveRemover(tbrp.fileNamePattern, rc);
  }

  void computeCurrentPeriodsHighestCounterValue(final String stemRegex) {
    File file = new File(getCurrentPeriodsFileNameWithoutCompressionSuffix());
    File parentDir = file.getParentFile();

    File[] matchingFileArray = FileFilterUtil
            .filesInFolderMatchingStemRegex(parentDir, stemRegex);

    if (matchingFileArray == null || matchingFileArray.length == 0) {
      currentPeriodsCounter = 0;
      return;
    }
    currentPeriodsCounter = FileFilterUtil.findHighestCounter(matchingFileArray, stemRegex);

    // if parent raw file property is not null, then the next
    // counter is max  found counter+1
    if (tbrp.getParentsRawFileProperty() != null || (tbrp.compressionMode != CompressionMode.NONE)) {
      // TODO test me
      currentPeriodsCounter++;
    }
  }

  // IMPORTANT: This field can be updated by multiple threads. It follows that
  // its values may *not* be incremented sequentially. However, we don't care
  // about the actual value of the field except that from time to time the
  // expression (invocationCounter++ & invocationMask) == invocationMask) should be true.
  private int invocationCounter;
  private int invocationMask = 0x1;

  public boolean isTriggeringEvent(File activeFile, final E event) {

    long time = getCurrentTime();
    if (time >= nextCheck) {
      Date dateInElapsedPeriod = dateInCurrentPeriod;
      elapsedPeriodsFileName = tbrp.fileNamePatternWCS
              .convertMultipleArguments(dateInElapsedPeriod, currentPeriodsCounter);
      currentPeriodsCounter = 0;
      setDateInCurrentPeriod(time);
      computeNextCheck();
      return true;
    }

    // for performance reasons, check for changes every 16,invocationMask invocations
    if (((++invocationCounter) & invocationMask) != invocationMask) {
      return false;
    }
    if (invocationMask < 0x0F) {
      invocationMask = (invocationMask << 1) + 1;
    }

    if (activeFile.length() >= maxFileSize.getSize()) {
      elapsedPeriodsFileName = tbrp.fileNamePatternWCS
              .convertMultipleArguments(dateInCurrentPeriod, currentPeriodsCounter);
      currentPeriodsCounter++;
      return true;
    }

    return false;
  }

  private String getFileNameIncludingCompressionSuffix(Date date, int counter) {
    return tbrp.fileNamePattern.convertMultipleArguments(
            dateInCurrentPeriod, counter);
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
