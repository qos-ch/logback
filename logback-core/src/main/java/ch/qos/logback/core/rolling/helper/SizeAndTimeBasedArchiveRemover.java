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
package ch.qos.logback.core.rolling.helper;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class SizeAndTimeBasedArchiveRemover extends DefaultArchiveRemover {

  boolean historyAsFileCount = false;

  public SizeAndTimeBasedArchiveRemover(FileNamePattern fileNamePattern,  RollingCalendar rc, boolean historyAsFileCount) {
    super(fileNamePattern, rc);
    this.historyAsFileCount = historyAsFileCount;
  }

  public void cleanByPeriodOffset(Date now, int periodOffset) {
    Date dateOfPeriodToClean = rc.getEndOfNextNthPeriod(now, periodOffset);

    File archive0 = new File(fileNamePattern.convertMultipleArguments(
            dateOfPeriodToClean, 0));
    // in case the file has no directory part, i.e. if it's written into the
    // user's current directory.
    archive0 = archive0.getAbsoluteFile();

    File parentDir = archive0.getAbsoluteFile().getParentFile();

    if (historyAsFileCount) {
      cleanByFile(now, dateOfPeriodToClean, -periodOffset - 1, parentDir);
    } else {
      cleanByPeriod(dateOfPeriodToClean, parentDir);
    }
  }

  private void cleanByPeriod(final Date dateOfPeriodToClean, final File parentDir) {
    String stemRegex = createStemRegex(dateOfPeriodToClean);

    File[] matchingFileArray = FileFilterUtil.filesInFolderMatchingStemRegex(
            parentDir, stemRegex);

    for (File f : matchingFileArray) {
      Date fileLastModified = rc.getEndOfNextNthPeriod(
              new Date(f.lastModified()), -1);

      if (fileLastModified.compareTo(dateOfPeriodToClean) <= 0) {
        addInfo("deleting " + f);
        f.delete();
      }
    }

    if (parentClean) {
      removeFolderIfEmpty(parentDir);
    }
  }

  private void cleanByFile(final Date cleanFrom, final Date cleanTo, final int maxFilesToRetain, final File parentDir) {
    int periodOffset = 0;
    int filesToRetain = maxFilesToRetain;
    Date dateOfPeriodToClean = rc.getEndOfNextNthPeriod(cleanFrom, periodOffset);
    while (dateOfPeriodToClean.after(cleanTo) || dateOfPeriodToClean.equals(cleanTo)) {
      // Find all the files for the period to clean
      String stemRegex = createStemRegex(dateOfPeriodToClean);
      File[] matchingFileArray = FileFilterUtil.filesInFolderMatchingStemRegex(
              parentDir, stemRegex);

      // Sort the files to delete the oldest first (smallest last modified time)
      Arrays.sort(matchingFileArray, new Comparator<File>() {
        @Override
        public int compare(final File f1, final File f2) {
          return Long.compare(f1.lastModified(), f2.lastModified());
        }
      });

      // Delete files from this period if there are more than should be retained
      for (int i = 0; i <= matchingFileArray.length - filesToRetain - 1; ++i) {
          final File file = matchingFileArray[i];
          addInfo("deleting " + file);
          file.delete();
      }

      // Allow each period to be in a separate folder
      if (parentClean && matchingFileArray.length != 0) {
        removeFolderIfEmpty(matchingFileArray[0].getAbsoluteFile().getParentFile());
      }

      // Update remaining files to retain and move back a time period
      filesToRetain = Math.max(0, filesToRetain - matchingFileArray.length);
      dateOfPeriodToClean = rc.getEndOfNextNthPeriod(cleanFrom, --periodOffset);
    }
  }

  private String createStemRegex(final Date dateOfPeriodToClean) {
    String regex = fileNamePattern.toRegexForFixedDate(dateOfPeriodToClean);
    return FileFilterUtil.afterLastSlash(regex);
  }

}
