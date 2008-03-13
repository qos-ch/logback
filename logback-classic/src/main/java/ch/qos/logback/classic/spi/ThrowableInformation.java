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

import java.util.Arrays;

import ch.qos.logback.classic.ClassicGlobal;

public class ThrowableInformation implements java.io.Serializable {

  private static final long serialVersionUID = 6307784764626694851L;
  private String[] sa;
  private transient final Throwable throwable;

  public ThrowableInformation(Throwable throwable) {
    this.throwable = throwable;
    sa = extractStringRep(throwable, null);
  }

  public Throwable getThrowable() {
    return throwable;
  }
  
  public String[] extractStringRep(Throwable t, StackTraceElement[] parentSTE) {
    String[] result;

    StackTraceElement[] ste = t.getStackTrace();
    final int commonFrames = findCommonFrames(ste, parentSTE);

    final String[] firstArray;
    if (commonFrames == 0) {
      firstArray = new String[ste.length + 1];
    } else {
      firstArray = new String[ste.length - commonFrames + 2];
    }

    String prefix = "";
    if (parentSTE != null) {
      prefix = ClassicGlobal.CAUSED_BY;
    }

    firstArray[0] = prefix + t.getClass().getName();
    if (t.getMessage() != null) {
      firstArray[0] += ": " + t.getMessage();
    }

    for (int i = 0; i < (ste.length - commonFrames); i++) {
      firstArray[i + 1] = ste[i].toString();
    }

    if (commonFrames != 0) {
      firstArray[firstArray.length - 1] = commonFrames
          + " common frames omitted";
    }

    Throwable cause = t.getCause();
    if (cause != null) {
      final String[] causeArray = extractStringRep(cause, ste);
      String[] tmp = new String[firstArray.length + causeArray.length];
      System.arraycopy(firstArray, 0, tmp, 0, firstArray.length);
      System
          .arraycopy(causeArray, 0, tmp, firstArray.length, causeArray.length);
      result = tmp;
    } else {
      result = firstArray;
    }
    return result;
  }

  private int findCommonFrames(StackTraceElement[] ste,
      StackTraceElement[] parentSTE) {
    if (parentSTE == null) {
      return 0;
    }

    int steIndex = ste.length - 1;
    int parentIndex = parentSTE.length - 1;
    int count = 0;
    while (steIndex >= 0 && parentIndex >= 0) {
      if (ste[steIndex].equals(parentSTE[parentIndex])) {
        count++;
      } else {
        break;
      }
      steIndex--;
      parentIndex--;
    }
    return count;
  }

  /**
   * The string representation of the exceptopn (throwable) that this object
   * represents.
   */
  public String[] getThrowableStrRep() {
    return sa;
  }

  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + Arrays.hashCode(sa);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final ThrowableInformation other = (ThrowableInformation) obj;
    if (!Arrays.equals(sa, other.sa))
      return false;
    return true;
  }
  
  
}
