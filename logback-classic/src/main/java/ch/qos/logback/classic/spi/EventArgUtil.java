/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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

public class EventArgUtil {


  public static final Throwable extractThrowable(Object[] argArray) {
    if (argArray == null || argArray.length == 0) {
      return null;
    }

    final Object lastEntry = argArray[argArray.length - 1];
    if (lastEntry instanceof Throwable) {
      return (Throwable) lastEntry;
    }
    return null;
  }

  /**
   * This method should be called only if {@link #successfulExtraction(Throwable)} returns true.
   *
   * @param argArray
   * @return
   */
  public static Object[] trimmedCopy(Object[] argArray) {
    if (argArray == null || argArray.length == 0) {
      throw new IllegalStateException("non-sensical empty or null argument array");
    }
    final int trimemdLen = argArray.length - 1;
    Object[] trimmed = new Object[trimemdLen];
    System.arraycopy(argArray, 0, trimmed, 0, trimemdLen);
    return trimmed;
  }

  public static Object[] arrangeArguments(Object[] argArray) {
    return argArray;
  }

  public static boolean successfulExtraction(Throwable throwable) {
    return throwable != null;
  }
}
