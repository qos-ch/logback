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
    Date dateOfPeriodToClean = rc.getRelativeDate(now, periodOffsetForDeletionTarget);

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
