/**
 * Logback: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.util;

import java.io.PrintStream;
import java.util.Iterator;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;

public class StatusPrinter {
  
  private static PrintStream ps = System.out;

  public static void setPrintStream(PrintStream printStream) {
    ps = printStream;
  }
  
  public static void print(Context context) {
    if (context == null) {
      throw new IllegalArgumentException("Context argument cannot be null");
    }

    StatusManager sm = context.getStatusManager();
    if (sm == null) {
      ps.println("WARN: Context named \"" + context.getName()
          + "\" has no status manager");
    } else {
      print(sm);
    }

  }

  public static void print(StatusManager sm) {
    synchronized (sm) {
      Iterator it = sm.iterator();
      while (it.hasNext()) {
        Status s = (Status) it.next();
        print("", s);
      }
    }
  }
  
  private static void print(String indentation, Status s) {
    String prefix;
    if(s.hasChildren()) {
       prefix = indentation + "+ ";
    } else {
      prefix = indentation + "|-";
    }
    ps.println(prefix+s);
    if (s.getThrowable() != null) {
      s.getThrowable().printStackTrace(ps);
    }
    if(s.hasChildren()) {
      Iterator<Status> ite = s.iterator();
      while(ite.hasNext()) {
        Status child = ite.next();
        print(indentation+"  ", child);
      }
      
    }
  }
  
}
