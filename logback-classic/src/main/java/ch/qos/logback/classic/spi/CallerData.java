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
 * This class computes caller data returning the result in the form
 * of a StackTraceElement array.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class CallerData {


  /**
   * When caller information is not available this constant is used for file
   * name, method name, etc.
   */
  public static final String NA = "?";

  // All logger call's in log4j-over-slf4j use the Category class
  private static final String LOG4J_CATEGORY = "org.apache.log4j.Category";

  /**
   * When caller information is not available this constant is used for the line
   * number.
   */
  public static final int LINE_NA = -1;

  public static String CALLER_DATA_NA = "?#?:?" + CoreConstants.LINE_SEPARATOR;

  /**
   * This value is returned in case no caller data could be extracted.
   */
  public static StackTraceElement[] EMPTY_CALLER_DATA_ARRAY = new StackTraceElement[0];


  /**
   * Extract caller data information as an array based on a Throwable passed as
   * parameter
   */
  public static StackTraceElement[] extract(Throwable t,
      String fqnOfInvokingClass, final int maxDepth) {
    if (t == null) {
      return null;
    }

    StackTraceElement[] steArray = t.getStackTrace();
    StackTraceElement[] callerDataArray;

    int found = LINE_NA;
    for (int i = 0; i < steArray.length; i++) {
      if (isDirectlyInvokingClass(steArray[i].getClassName(),
          fqnOfInvokingClass)) {
        // the caller is assumed to be the next stack frame, hence the +1.
        found = i + 1;
      } else {
        if (found != LINE_NA) {
          break;
        }
      }
    }

    // we failed to extract caller data
    if (found == LINE_NA) {
      return EMPTY_CALLER_DATA_ARRAY;
    }

    int availableDepth = steArray.length - found;
    int desiredDepth = maxDepth < (availableDepth) ? maxDepth : availableDepth;

    callerDataArray = new StackTraceElement[desiredDepth];
    for (int i = 0; i < desiredDepth; i++) {
      callerDataArray[i] = steArray[found + i];
    }
    return callerDataArray;
  }

  public static boolean isDirectlyInvokingClass(String currentClass,
      String fqnOfInvokingClass) {
    // the check for org.apachje.log4j.Category class is intended to support
    // log4j-over-slf4j
    // it solves http://bugzilla.slf4j.org/show_bug.cgi?id=66
    if (currentClass.equals(fqnOfInvokingClass)
        || currentClass.equals(LOG4J_CATEGORY)) {
      return true;
    } else {
      return false;
    }
  }

}
