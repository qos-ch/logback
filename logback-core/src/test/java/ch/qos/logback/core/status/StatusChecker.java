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
package ch.qos.logback.core.status;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.core.Context;

public class StatusChecker {

  StatusManager sm;

  public StatusChecker(StatusManager sm) {
    this.sm = sm;
  }

  public StatusChecker(Context context) {
    this.sm = context.getStatusManager();
  }
  
  public boolean isErrorFree() {
    int level = sm.getLevel();
    return level < Status.ERROR;
  }
  
  public boolean containsMatch(int level, String regex) {
    Pattern p = Pattern.compile(regex);

    for(Status status: sm.getCopyOfStatusList()) {
      if(level != status.getLevel()) {
        continue;
      }
      String msg = status.getMessage();
      Matcher matcher = p.matcher(msg);
      if (matcher.lookingAt()) {
        return true;
      } 
    }
    return false;
    
  }
  
  public boolean containsMatch(String regex) {
    Pattern p = Pattern.compile(regex);
    for(Status status: sm.getCopyOfStatusList()) {
      String msg = status.getMessage();
      Matcher matcher = p.matcher(msg);
      if (matcher.lookingAt()) {
        return true;
      } 
    }
    return false;
  }

  public int matchCount(String regex) {
    int count = 0;
    Pattern p = Pattern.compile(regex);
    for(Status status: sm.getCopyOfStatusList()) {
      String msg = status.getMessage();
      Matcher matcher = p.matcher(msg);
      if (matcher.lookingAt()) {
        count++;
      } 
    }
    return count;
  }
  
  public boolean containsException(Class exceptionType) {
    Iterator stati = sm.getCopyOfStatusList().iterator();
    while (stati.hasNext()) {
      Status status = (Status) stati.next();
      Throwable t = status.getThrowable();
      if (t != null && t.getClass().getName().equals(exceptionType.getName())) {
        return true;
      }
    }
    return false;
  }

}
