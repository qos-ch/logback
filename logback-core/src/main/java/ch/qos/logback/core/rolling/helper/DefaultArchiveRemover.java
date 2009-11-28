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

import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.LiteralConverter;
import ch.qos.logback.core.spi.ContextAwareBase;

public class DefaultArchiveRemover extends ContextAwareBase implements
    ArchiveRemover {

  final FileNamePattern fileNamePattern;
  final RollingCalendar rc;
  int periodOffsetForDeletionTarget;
  final boolean parentClean;

  public DefaultArchiveRemover(FileNamePattern fileNamePattern,
      RollingCalendar rc) {
    this.fileNamePattern = fileNamePattern;
    this.rc = rc;
    this.parentClean = computeParentCleaningFlag(fileNamePattern);
  }

  boolean computeParentCleaningFlag(FileNamePattern fileNamePattern) {
    DateTokenConverter dtc = fileNamePattern.getDateTokenConverter();
    // if the date pattern has a /, then we need parent cleaning
    if (dtc.getDatePattern().indexOf('/') != -1) {
      return true;
    }
    // if the literal string subsequent to the dtc contains a /, we also
    // need
    // parent cleaning

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

  public void clean(Date now) {
    Date date2delete = rc.getRelativeDate(now, periodOffsetForDeletionTarget);
    String filename = fileNamePattern.convert(date2delete);
    File file2Delete = new File(filename);
    if (file2Delete.exists() && file2Delete.isFile()) {
      file2Delete.delete();
      addInfo("deleting " + file2Delete);
      if (parentClean) {
        removeFolderIfEmpty(file2Delete.getParentFile(), 0);
      }
    }
  }

  /**
   * Will remove the directory passed as parameter if empty. After that, if the
   * parent is also becomes empty, remove the parent dir as well but at most 3
   * times.
   * 
   * @param dir
   * @param recursivityCount
   */
  void removeFolderIfEmpty(File dir, int recursivityCount) {
    // we should never go more than 3 levels higher
    if (recursivityCount >= 3) {
      return;
    }
    if (dir.isDirectory() && FileFilterUtil.isEmptyDirectory(dir)) {
      addInfo("deleting folder [" + dir +"]");
      dir.delete();
      removeFolderIfEmpty(dir.getParentFile(), recursivityCount + 1);
    }
  }

  public void setMaxHistory(int maxHistory) {
    this.periodOffsetForDeletionTarget = -maxHistory - 1;
  }

}
