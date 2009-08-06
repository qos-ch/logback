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
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.core.joran.spi.NoAutoStart;
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

    // we need to get the correct value of currentPeriodsCounter.
    // usually the value is 0, unless the appender or the application
    // is stopped and restarted within the same period

    if (tbrp.getParentsRawFileProperty() == null) {
      String sregex = tbrp.fileNamePattern.toSRegex(dateInCurrentPeriod);
      String simplifiedRegex = afterLastSlash(sregex);
      computeCurrentPeriodsCounter(simplifiedRegex);
    }
    started = true;
  }

  String afterLastSlash(String sregex) {
    int i = sregex.lastIndexOf('/');
    if (i == -1) {
      return sregex;
    } else {
      return sregex.substring(i + 1);
    }
  }

  void computeCurrentPeriodsCounter(final String simplifiedRegex) {
    File file = new File(getCurrentPeriodsFileNameWithoutCompressionSuffix());

    File parentDir = file.getParentFile();
    if (parentDir != null && parentDir.isDirectory()) {
      File[] matchingFileArray = parentDir.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.matches(simplifiedRegex);
        }
      });
      if (matchingFileArray == null || matchingFileArray.length == 0) {
        return;
      }
      Arrays.sort(matchingFileArray, new Comparator<File>() {
        public int compare(File o1, File o2) {
          String o1Name = o1.getName();
          String o2Name = o2.getName();
          return (o2Name.compareTo(o1Name));
        }
      });
      File lastFile = matchingFileArray[0];

      Pattern p = Pattern.compile(simplifiedRegex);
      String lastFileName = lastFile.getName();

      Matcher m = p.matcher(lastFileName);
      if (!m.matches()) {
        throw new IllegalStateException("The regex [" + simplifiedRegex
            + "] should match [" + lastFileName + "]");
      }
      String currentPeriodsCounterAsStr = m.group(1);
      currentPeriodsCounter = new Integer(currentPeriodsCounterAsStr)
          .intValue();
    }
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
