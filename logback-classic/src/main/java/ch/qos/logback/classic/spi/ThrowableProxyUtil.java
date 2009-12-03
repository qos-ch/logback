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
package ch.qos.logback.classic.spi;

import ch.qos.logback.core.CoreConstants;

/**
 * Convert a throwable into an array of ThrowableDataPoint objects.
 * 
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class ThrowableProxyUtil {

  static public void build(ThrowableProxy nestedTP, Throwable nestedThrowable,
      ThrowableProxy parentTP) {

    StackTraceElement[] nestedSTE = nestedThrowable.getStackTrace();

    int commonFramesCount = -1;
    if (parentTP != null) {
      commonFramesCount = findNumberOfCommonFrames(nestedSTE, parentTP
          .getStackTraceElementProxyArray());
    }

    nestedTP.commonFrames = commonFramesCount;
    nestedTP.stackTraceElementProxyArray = steArrayToStepArray(nestedSTE);
  }

  static StackTraceElementProxy[] steArrayToStepArray(StackTraceElement[] stea) {
    StackTraceElementProxy[] stepa = new StackTraceElementProxy[stea.length];
    for (int i = 0; i < stepa.length; i++) {
      stepa[i] = new StackTraceElementProxy(stea[i]);
    }
    return stepa;
  }

  static int findNumberOfCommonFrames(StackTraceElement[] steArray,
      StackTraceElementProxy[] parentSTEPArray) {
    if (parentSTEPArray == null) {
      return 0;
    }

    int steIndex = steArray.length - 1;
    int parentIndex = parentSTEPArray.length - 1;
    int count = 0;
    while (steIndex >= 0 && parentIndex >= 0) {
      StackTraceElement ste = steArray[steIndex];
      StackTraceElement otherSte = parentSTEPArray[parentIndex].ste;
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

  static public String asString(IThrowableProxy tp) {
    StringBuilder sb = new StringBuilder();

    while (tp != null) {
      printFirstLine(sb, tp);
      sb.append(CoreConstants.LINE_SEPARATOR);
      printSTEPArray(sb, tp);
      tp = tp.getCause();
    }
    return sb.toString();
  }

  static void appendPackagingData(StringBuilder builder, StackTraceElementProxy step) {
    if (step != null) {
      ClassPackagingData cpd = step.getClassPackagingData();
      if (cpd != null) {
        if (!cpd.isExact()) {
          builder.append(" ~[");
        } else {
          builder.append(" [");
        }
   
        builder.append(cpd.getCodeLocation()).append(':').append(
            cpd.getVersion()).append(']');
      }
    }
  }
  
  static public void printSTEP(StringBuilder sb, StackTraceElementProxy step) {
    sb.append(step.toString());
    appendPackagingData(sb, step);
  }
  
  static public void printSTEPArray(StringBuilder sb, IThrowableProxy tp) {
    StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
    int commonFrames = tp.getCommonFrames();

    for (int i = 0; i < stepArray.length - commonFrames; i++) {
      StackTraceElementProxy step = stepArray[i];
      sb.append(CoreConstants.TAB);
      printSTEP(sb, step);
      sb.append(CoreConstants.LINE_SEPARATOR);
    }
    
    if (commonFrames > 0) {
      sb.append("\t... " + commonFrames).append(" common frames omitted")
          .append(CoreConstants.LINE_SEPARATOR);
    }
    
  }

  static public void printFirstLine(StringBuilder buf, IThrowableProxy tp) {
    int commonFrames = tp.getCommonFrames();
    if (commonFrames > 0) {
      buf.append(CoreConstants.CAUSED_BY);
    }
    buf.append(tp.getClassName()).append(": ").append(tp.getMessage());
  }
}
