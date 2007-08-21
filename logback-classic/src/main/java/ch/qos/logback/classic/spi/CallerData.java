/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2005, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.spi;

import ch.qos.logback.core.CoreGlobal;

/**
 * The internal representation of caller location information.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class CallerData implements java.io.Serializable {

  private static final long serialVersionUID = 2473626903716082403L;

  /**
   * When caller information is not available this constant is used for file
   * name, method name, etc.
   */
  public static final String NA = "?";

  /**
   * When caller information is not available this constant is used for the line
   * number.
   */
  public static final int LINE_NA = -1;

  
  public static String CALLER_DATA_NA = "?#?:?"+CoreGlobal.LINE_SEPARATOR;

  /**
   * This value is returned in case no caller data could be extracted.
   */
  public static CallerData[] EMPTY_CALLER_DATA_ARRAY = new CallerData[0];
  
  /**
   * Caller's line number.
   */
  int lineNumber;

  /**
   * Caller's file name.
   */
  String fileName;

  /**
   * Caller's fully qualified class name.
   */
  String className;

  /**
   * Caller's method name.
   */
  String methodName;

  boolean nativeMethod = false;
  
  public CallerData(String fileName, String className, String methodName,
      int lineNumber) {
    this.fileName = fileName;
    this.className = className;
    this.methodName = methodName;
    this.lineNumber = lineNumber;
  }

  public CallerData(StackTraceElement ste) {
    className = ste.getClassName();
    fileName = ste.getFileName();
    methodName = ste.getMethodName();
    lineNumber = ste.getLineNumber();
    nativeMethod = ste.isNativeMethod();
  }

  /**
   * Extract caller data information as an array based on a Throwable passed as parameter
   */
  public static CallerData[] extract(Throwable t, String fqnOfInvokingClass) {
    if (t == null) {
      return null;
    }

    StackTraceElement[] steArray = t.getStackTrace();
    CallerData[] callerDataArray;
    
    int found = LINE_NA;
    for (int i = 0; i < steArray.length; i++) {
      if(steArray[i].getClassName().equals(fqnOfInvokingClass)) {
        // the caller is assumed to be the next stack frame, hence the +1.
        found = i + 1;
      } else {
        if(found != LINE_NA) {
          break;
        }
      }
    }

    // we failed to extract caller data
    if(found == LINE_NA) {
      return EMPTY_CALLER_DATA_ARRAY;
    }
    
    callerDataArray = new CallerData[steArray.length - found];
    for (int i = found; i < steArray.length; i++) {
      callerDataArray[i-found] = new CallerData(steArray[i]);
    }
    return callerDataArray;
  }
  
  
  public boolean equals(Object o) {
    // LogLog.info("equals called");
    if (this == o) {
      return true;
    }

    if (!(o instanceof CallerData)) {
      // LogLog.info("inequality point 1");
      return false;
    }

    CallerData r = (CallerData) o;

    if (!getClassName().equals(r.getClassName())) {
      // LogLog.info("inequality point 2");
      return false;
    }

    if (!getFileName().equals(r.getFileName())) {
      // LogLog.info("inequality point 3");
      return false;
    }

    if (!getMethodName().equals(r.getMethodName())) {
      // LogLog.info("inequality point 4");
      return false;
    }

    if (!(lineNumber == r.lineNumber)) {
      // LogLog.info("inequality point 5");
      return false;
    }

    return true;
  }

  /**
   * Return the fully qualified class name of the caller making the logging
   * request.
   */
  public String getClassName() {
    return className;
  }

  /**
   * Return the file name of the caller.
   * 
   * <p>
   * This information is not always available.
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Returns the line number of the caller.
   * 
   * <p>
   * This information is not always available.
   */
  public int getLineNumber() {
    return lineNumber;
  }

  /**
   * Returns the method name of the caller.
   */
  public String getMethodName() {
    return methodName;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(getClassName());
    buf.append('.');
    buf.append(getMethodName());
    buf.append('(');
    if (isNativeMethod()) {
      buf.append("Native Method");
    } else if (getFileName() == null) {
      buf.append("Unknown Source");
    } else {
      buf.append(getFileName());
      buf.append(':');
      buf.append(getLineNumber());
    }
    buf.append(')');
    return buf.toString();
  }

  public boolean isNativeMethod() {
    return nativeMethod;
  }
}
