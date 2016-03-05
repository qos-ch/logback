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
package ch.qos.logback.core.rolling;

import java.io.File;

/**
 * DeleteOldFilesRollingPolicy is an extension of RollingPolicy which deletes
 * existing log files older than a configurable number of days when logback is
 * starting up. This is well suited for uniquely named log files without roll
 * over but where the application is restarted on a regular basis.
 */
public class DeleteOldFilesRollingPolicy extends RollingPolicyBase {

  /**
   * The maximum number of days to keep an existing log file.
   */
  protected long daysToKeep;

  public void rollover() throws RolloverFailure {
    File logFile = new File(".", getActiveFileName());
    File logFileDir = logFile.getParentFile();

    if (logFileDir.exists() == false) {
      addInfo("Skipping non existing dir: " + logFileDir.getAbsolutePath());
      return;
    }

    long now = System.currentTimeMillis();

    long filesDeleted = 0;

    for (File f : logFileDir.listFiles()) {
      long lastModified = f.lastModified();

      long ageInMilliSeconds = now - lastModified;
      long ageInSeconds = ageInMilliSeconds / 1000;
      long ageInHours = ageInSeconds / 3600;
      long ageInDays = ageInHours / 24;

      final String absolutePath = f.getAbsolutePath();

      if (ageInDays > daysToKeep) {
        if (f.delete()) {
          addInfo("Deleted " + absolutePath);
        } else {
          addError("Could not delete " + absolutePath);
        }
        filesDeleted = filesDeleted + 1;
      } else {
        addInfo("Will keep " + absolutePath);
      }
    }
    addInfo("Deleted " + filesDeleted + " files.");
  }

  public String getActiveFileName() {
    // Copied from FixedWindowRollingPolicy.
    return getParentsRawFileProperty();
  }

  /**
   * Get the threshold number of days for which log files are kept.
   * 
   * @return
   */
  public long getDaysToKeep() {
    return daysToKeep;
  }

  /**
   * Set the threshold number of days for which log files are kept.
   * 
   * @param daysToKeep
   *          number of days (converted to number of milliseconds when used,
   *          does not respect time zone adjustments)
   */
  public void setDaysToKeep(long daysToKeep) {
    this.daysToKeep = daysToKeep;
  }
}
