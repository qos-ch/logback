/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.rolling.helper;

import java.io.File;
import java.util.Date;

public class SizeAndTimeBasedArchiveRemover extends DefaultArchiveRemover {

  public SizeAndTimeBasedArchiveRemover(FileNamePattern fileNamePattern,
      RollingCalendar rc) {
    super(fileNamePattern, rc);
  }

  @Override
  public void clean(Date now) {
    Date dateOfPeriodToClean = rc.getRelativeDate(now, periodOffset);

    String regex = fileNamePattern.toRegex(dateOfPeriodToClean);
    String stemRegex = FileFilterUtil.afterLastSlash(regex);
    File archive0 = new File(fileNamePattern.convertMultipleArguments(
        dateOfPeriodToClean, 0));

    File parentDir = archive0.getParentFile();
    File[] matchingFileArray = FileFilterUtil.filesInFolderMatchingStemRegex(
        parentDir, stemRegex);

    for (File f : matchingFileArray) {
      f.delete();
    }

    if (parentClean) {
      removeFolderIfEmpty(parentDir, 0);
    }
  }

}
