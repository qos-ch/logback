/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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
import org.codehaus.groovy.tools.shell.util.NoExitSecurityManager;

public class TimeBasedArchiveRemover extends DefaultArchiveRemover {

  public TimeBasedArchiveRemover(FileNamePattern fileNamePattern,
                                 RollingCalendar rc, long currentTime) {
    super(fileNamePattern, rc, currentTime);
  }


  public void clean(Date now) {
    long nowInMillis = now.getTime();
    int periodsElapsed = rc.periodsElapsed(lastHeartBeat, nowInMillis);
    if(periodsElapsed < 1) {
      addWarn("Unexpected periodsElapsed value "+periodsElapsed);
      periodsElapsed = 1;
    }
    lastHeartBeat = nowInMillis;
    for(int i=0; i < periodsElapsed; i++) {
      cleanByPeriodOffset(now, periodOffsetForDeletionTarget-i);
    }
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
}
