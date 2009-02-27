/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic.spi;

import ch.qos.logback.core.CoreConstants;

/**
 * Convert a throwable into an array of ThrowableDataPoint objects.
 * 
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class ThrowableProxyUtil {

  static final ThrowableDataPoint[] TEMPLATE_ARRAY = new ThrowableDataPoint[0];

  static public void build(ThrowableProxy nestedTP, Throwable nestedThrowable,
      ThrowableProxy parentTP) {

    StackTraceElement[] nestedSTE = nestedThrowable.getStackTrace();

    int commonFramesCount = -1;
    if (parentTP != null) {
      commonFramesCount = findNumberOfCommonFrames(nestedSTE, parentTP
          .getThrowableDataPointArray());
    }

    nestedTP.commonFrames = commonFramesCount;
    nestedTP.throwableDataPointArray = stea2tdpa(nestedSTE);
  }

  static ThrowableDataPoint[] stea2tdpa(StackTraceElement[] stea) {
    ThrowableDataPoint[] tdpa = new ThrowableDataPoint[stea.length];
    for (int i = 0; i < tdpa.length; i++) {
      tdpa[i] = new ThrowableDataPoint(stea[i]);
    }
    return tdpa;
  }

  static int findNumberOfCommonFrames(StackTraceElement[] steArray,
      ThrowableDataPoint[] parentTDPA) {
    if (parentTDPA == null) {
      return 0;
    }

    int steIndex = steArray.length - 1;
    int parentIndex = parentTDPA.length - 1;
    int count = 0;
    while (steIndex >= 0 && parentIndex >= 0) {
      StackTraceElement ste = steArray[steIndex];
      StackTraceElement otherSte = parentTDPA[parentIndex].step.ste;
      if (ste.equals(otherSte)) {
        count++;
      } else {
        break;
      }
      steIndex--;
      parentIndex--;
    }
    return count;
  }

  static String asString(IThrowableProxy tp) {
    StringBuilder sb = new StringBuilder();

    while (tp != null) {
     
      printFirstLine(sb, tp);
      printTDP(sb, tp);
      tp = tp.getCause();
    }
    return sb.toString();

  }

  static public void printTDP(StringBuilder sb, IThrowableProxy tp) {
    ThrowableDataPoint[] tdpa = tp.getThrowableDataPointArray();
    int commonFrames = tp.getCommonFrames();
    for (int i = 0; i < tdpa.length - commonFrames; i++) {
      ThrowableDataPoint tdp = tdpa[i];
      sb.append(tdp.toString()).append(CoreConstants.LINE_SEPARATOR);
    }
    
    if (commonFrames > 0) {
      sb.append("\t... " + commonFrames).append(" common frames omitted")
          .append(CoreConstants.LINE_SEPARATOR);
    }
    
    
  }

  static public void printFirstLine(StringBuilder sb, IThrowableProxy tp) {
    int commonFrames = tp.getCommonFrames();
    if (commonFrames > 0) {
      sb.append(CoreConstants.CAUSED_BY);
    }
    sb.append(tp.getClassName()).append(": ").append(tp.getMessage());
    sb.append(CoreConstants.LINE_SEPARATOR);
  }
}
