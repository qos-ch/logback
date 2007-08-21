/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.status;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;


public class StatusChecker {

  StatusManager sm;
  
  public StatusChecker(StatusManager sm) {
    this.sm = sm;
  }
  
  public boolean containsMatch(String regex) {
    
    Pattern p = Pattern.compile(regex);
 
    
    Iterator stati = sm.iterator();
    while(stati.hasNext()) {
      Status status = (Status) stati.next();
      String msg = status.getMessage();
      Matcher matcher = p.matcher(msg);
      if(matcher.lookingAt()) {
        return true;
      } else {
        System.out.println("no match:"+msg);
        System.out.println("regex   :"+regex);
      }
    }
    return false;
  }
  
  
  public boolean containsException(Class exceptionType) {
    Iterator stati = sm.iterator();
    while(stati.hasNext()) {
      Status status = (Status) stati.next();
      Throwable t = status.getThrowable();
      if(t != null && t.getClass().getName().equals(exceptionType.getName())) {
        return true;
      }
    }
    return false;
  }

}
