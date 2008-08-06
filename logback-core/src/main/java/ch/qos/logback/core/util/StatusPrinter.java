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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreGlobal;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.helpers.ThrowableToStringArray;

public class StatusPrinter {

  private static PrintStream ps = System.out;

  static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
      "HH:mm:ss,SSS");

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
    StringBuilder sb = new StringBuilder();
    buildStr(sb, sm);
    ps.println(sb.toString());
  }

  public static void buildStr(StringBuilder sb, StatusManager sm) {
    Iterator it = sm.getCopyOfStatusList().iterator();
    while (it.hasNext()) {
      Status s = (Status) it.next();
      buildStr(sb, "", s);
    }
  }

  private static void appendThrowable(StringBuilder sb, Throwable t) {
    String[] stringRep = ThrowableToStringArray.extractStringRep(t, null);

    for (String s : stringRep) {
      if (s.startsWith(CoreGlobal.CAUSED_BY)) {
        // nothing
      } else if (Character.isDigit(s.charAt(0))) {
        // if line resembles "48 common frames omitted"
        sb.append("\t... ");
      } else {
        // most of the time. just add a tab+"at"
        sb.append("\tat ");
      }
      sb.append(s).append(CoreGlobal.LINE_SEPARATOR);
    }
  }

  private static void buildStr(StringBuilder sb, String indentation, Status s) {
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
    sb.append(prefix + s).append(Layout.LINE_SEP);

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
