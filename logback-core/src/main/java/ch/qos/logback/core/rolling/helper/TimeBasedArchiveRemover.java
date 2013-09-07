/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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

public class TimeBasedArchiveRemover extends DefaultArchiveRemover {

  public TimeBasedArchiveRemover(FileNamePattern fileNamePattern,
                                 RollingCalendar rc) {
    super(fileNamePattern, rc);
  }

  protected void cleanByPeriodOffset(Date now, int periodOffset) {
    Date date2delete = rc.getRelativeDate(now, periodOffset);
    String filename = fileNamePattern.convert(date2delete);
    File file2Delete = new File(filename);
    if (file2Delete.exists() && file2Delete.isFile()) {
      file2Delete.delete();
      addInfo("deleting " + file2Delete);
      if (parentClean) {
        removeFolderIfEmpty(file2Delete.getParentFile());
      }
    }
  }

  public String toString() {
    return "c.q.l.core.rolling.helper.TimeBasedArchiveRemover";
  }
}
