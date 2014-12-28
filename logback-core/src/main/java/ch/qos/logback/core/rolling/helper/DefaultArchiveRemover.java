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
import java.util.Date;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.LiteralConverter;
import ch.qos.logback.core.spi.ContextAwareBase;

abstract public class DefaultArchiveRemover extends ContextAwareBase implements
        ArchiveRemover {

  static protected final long UNINITIALIZED = -1;
  // aim for 64 days, except in case of hourly rollover
  static protected final long INACTIVITY_TOLERANCE_IN_MILLIS = 64L * (long) CoreConstants.MILLIS_IN_ONE_DAY;
  static final int MAX_VALUE_FOR_INACTIVITY_PERIODS = 14 * 24; // 14 days in case of hourly rollover

  final FileNamePattern fileNamePattern;
  final RollingCalendar rc;
  int periodOffsetForDeletionTarget;
  final boolean parentClean;
  long lastHeartBeat = UNINITIALIZED;

  public DefaultArchiveRemover(FileNamePattern fileNamePattern,
                               RollingCalendar rc) {
    this.fileNamePattern = fileNamePattern;
    this.rc = rc;
    this.parentClean = computeParentCleaningFlag(fileNamePattern);
  }


  int computeElapsedPeriodsSinceLastClean(long nowInMillis) {
    long periodsElapsed = 0;
    if (lastHeartBeat == UNINITIALIZED) {
      addInfo("first clean up after appender initialization");
      periodsElapsed = rc.periodsElapsed(nowInMillis, nowInMillis + INACTIVITY_TOLERANCE_IN_MILLIS);
      if (periodsElapsed > MAX_VALUE_FOR_INACTIVITY_PERIODS)
        periodsElapsed = MAX_VALUE_FOR_INACTIVITY_PERIODS;
    } else {
      periodsElapsed = rc.periodsElapsed(lastHeartBeat, nowInMillis);
      if (periodsElapsed < 1) {
        addWarn("Unexpected periodsElapsed value " + periodsElapsed);
        periodsElapsed = 1;
      }
    }
    return (int) periodsElapsed;
  }

  public void clean(Date now) {
    long nowInMillis = now.getTime();
    int periodsElapsed = computeElapsedPeriodsSinceLastClean(nowInMillis);
    lastHeartBeat = nowInMillis;
    if (periodsElapsed > 1) {
      addInfo("periodsElapsed = " + periodsElapsed);
    }
    for (int i = 0; i < periodsElapsed; i++) {
      cleanByPeriodOffset(now, periodOffsetForDeletionTarget - i);
    }
  }

  abstract void cleanByPeriodOffset(Date now, int periodOffset);

  boolean computeParentCleaningFlag(FileNamePattern fileNamePattern) {
    DateTokenConverter dtc = fileNamePattern.getPrimaryDateTokenConverter();
    // if the date pattern has a /, then we need parent cleaning
    if (dtc.getDatePattern().indexOf('/') != -1) {
      return true;
    }
    // if the literal string subsequent to the dtc contains a /, we also
    // need parent cleaning

    Converter<Object> p = fileNamePattern.headTokenConverter;

    // find the date converter
    while (p != null) {
      if (p instanceof DateTokenConverter) {
        break;
      }
      p = p.getNext();
    }

    while (p != null) {
      if (p instanceof LiteralConverter) {
        String s = p.convert(null);
        if (s.indexOf('/') != -1) {
          return true;
        }
      }
      p = p.getNext();
    }

    // no /, so we don't need parent cleaning
    return false;
  }

  void removeFolderIfEmpty(File dir) {
    removeFolderIfEmpty(dir, 0);
  }

  /**
   * Will remove the directory passed as parameter if empty. After that, if the
   * parent is also becomes empty, remove the parent dir as well but at most 3
   * times.
   *
   * @param dir
   * @param depth
   */
  private void removeFolderIfEmpty(File dir, int depth) {
    // we should never go more than 3 levels higher
    if (depth >= 3) {
      return;
    }
    if (dir.isDirectory() && FileFilterUtil.isEmptyDirectory(dir)) {
      addInfo("deleting folder [" + dir + "]");
      dir.delete();
      removeFolderIfEmpty(dir.getParentFile(), depth + 1);
    }
  }

  public void setMaxHistory(int maxHistory) {
    this.periodOffsetForDeletionTarget = -maxHistory - 1;
  }

}
