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
package ch.qos.logback.core.util;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.helpers.ThrowableToStringArray;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;

public class StatusPrinter {

  private static PrintStream ps = System.out;

  static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
      "HH:mm:ss,SSS");

  public static void setPrintStream(PrintStream printStream) {
    ps = printStream;
  }
  
  /**
   * Print the contents of the context statuses, but only if they contain
   * warnings or errors.
   * 
   * @param context
   */
  public static void printInCaseOfErrorsOrWarnings(Context context) {
    if (context == null) {
      throw new IllegalArgumentException("Context argument cannot be null");
    }

    StatusManager sm = context.getStatusManager();
    if (sm == null) {
      ps.println("WARN: Context named \"" + context.getName()
          + "\" has no status manager");
    } else {
      if (sm.getLevel() == ErrorStatus.WARN || (sm.getLevel() == ErrorStatus.ERROR) ) {
        print(sm);
      }
    }
  }

  /**
   * Print the contents of the context statuses, but only if they contain
   * errors.
   * 
   * @param context
   */
  public static void printIfErrorsOccured(Context context) {
    if (context == null) {
      throw new IllegalArgumentException("Context argument cannot be null");
    }

    StatusManager sm = context.getStatusManager();
    if (sm == null) {
      ps.println("WARN: Context named \"" + context.getName()
          + "\" has no status manager");
    } else {
      if (sm.getLevel() == ErrorStatus.ERROR) {
        print(sm);
      }
    }
  }

  /**
   * Print the contents of the context's status data.
   * 
   * @param context
   */
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
    StringBuilder sb = new StringBuilder();
    buildStrFromStatusManager(sb, sm);
    ps.println(sb.toString());
  }

  public static void print(List<Status> statusList) {
    StringBuilder sb = new StringBuilder();
    buildStrFromStatusList(sb, statusList);
    ps.println(sb.toString());
  }
  

  private static void buildStrFromStatusList(StringBuilder sb, List<Status> statusList) {
    if(statusList == null)
      return;
    for(Status s : statusList) {
      buildStr(sb, "", s);
    }
  }

  private static void buildStrFromStatusManager(StringBuilder sb, StatusManager sm) {
    buildStrFromStatusList(sb, sm.getCopyOfStatusList());
  }
  
  private static void appendThrowable(StringBuilder sb, Throwable t) {
    String[] stringRep = ThrowableToStringArray.convert(t);

    for (String s : stringRep) {
      if (s.startsWith(CoreConstants.CAUSED_BY)) {
        // nothing
      } else if (Character.isDigit(s.charAt(0))) {
        // if line resembles "48 common frames omitted"
        sb.append("\t... ");
      } else {
        // most of the time. just add a tab+"at"
        sb.append("\tat ");
      }
      sb.append(s).append(CoreConstants.LINE_SEPARATOR);
    }
  }

  public static void buildStr(StringBuilder sb, String indentation, Status s) {
    String prefix;
    if (s.hasChildren()) {
      prefix = indentation + "+ ";
    } else {
      prefix = indentation + "|-";
    }

    if (simpleDateFormat != null) {
      Date date = new Date(s.getDate());
      String dateStr = simpleDateFormat.format(date);
      sb.append(dateStr).append(" ");
    }
    sb.append(prefix).append(s).append(CoreConstants.LINE_SEPARATOR);

    if (s.getThrowable() != null) {
      appendThrowable(sb, s.getThrowable());
    }
    if (s.hasChildren()) {
      Iterator<Status> ite = s.iterator();
      while (ite.hasNext()) {
        Status child = ite.next();
        buildStr(sb, indentation + "  ", child);
      }
    }
  }

}
