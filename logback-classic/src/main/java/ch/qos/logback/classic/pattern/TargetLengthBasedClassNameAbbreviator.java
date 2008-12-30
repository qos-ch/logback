/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.pattern;

import ch.qos.logback.core.CoreConstants;

public class TargetLengthBasedClassNameAbbreviator implements Abbreviator {

  static private final int BUF_LIMIT = 256;
  static private final int MAX_DOTS = 12;

  final int targetLength;
  StringBuffer buf;

  public TargetLengthBasedClassNameAbbreviator(int targetLength) {
    this.targetLength = targetLength;
    buf = new StringBuffer(targetLength);
  }

  public String abbreviate(String fqClassName) {
    if (fqClassName == null) {
      throw new IllegalArgumentException("Class name may not be null");
    }

    int inLen = fqClassName.length();
    if (inLen < targetLength) {
      return fqClassName;
    }

    if (buf.capacity() > BUF_LIMIT) {
      buf = new StringBuffer(targetLength);
    }
    buf.setLength(0);

    int[] dotArray = new int[MAX_DOTS];
    int[] lengthArray = new int[MAX_DOTS];

    int dotCount = computeIndexes(fqClassName, dotArray);

    // System.out.println();
    // System.out.println("Dot count for [" + className + "] is " + dotCount);
    // if there are not dots than abbreviation is not possible
    if (dotCount == 0) {
      return fqClassName;
    }
    // printArray("dotArray: ", dotArray);
    computeLengthArray(fqClassName, dotArray, lengthArray, dotCount);
    // printArray("lengthArray: ", lengthArray);
    for (int i = 0; i <= dotCount; i++) {
      if (i == 0) {
        buf.append(fqClassName.substring(0, lengthArray[i] - 1));
      } else {
        buf.append(fqClassName.substring(dotArray[i - 1], dotArray[i - 1]
            + lengthArray[i]));
      }
      // System.out.println("i=" + i + ", buf=" + buf);
    }

    return buf.toString();
  }


  static int computeIndexes(final String className, int[] dotArray) {
    int dotCount = 0;
    int k = 0;
    while (true) {
      k = className.indexOf(CoreConstants.DOT, k);
      if (k != -1 && dotCount < MAX_DOTS) {
        dotArray[dotCount] = k;
        dotCount++;
        k++;
      } else {
        break;
      }
    }
    return dotCount;
  }

  void computeLengthArray(final String className, int[] dotArray,
      int[] lengthArray, int dotCount) {
    int toTrim = className.length() - targetLength;
    // System.out.println("toTrim=" + toTrim);

    // int toTrimAvarage = 0;

    int len;
    for (int i = 0; i < dotCount; i++) {
      int previousDotPosition = -1;
      if (i > 0) {
        previousDotPosition = dotArray[i - 1];
      }
      int available = dotArray[i] - previousDotPosition - 1;
      // System.out.println("i=" + i + ", available = " + available);

      len = (available < 1) ? available : 1;
      // System.out.println("i=" + i + ", toTrim = " + toTrim);

      if (toTrim > 0) {
        len = (available < 1) ? available : 1;
      } else {
        len = available;
      }
      toTrim -= (available - len);
      lengthArray[i] = len + 1;
    }

    int lastDotIndex = dotCount - 1;
    lengthArray[dotCount] = className.length() - dotArray[lastDotIndex];
  }

  static void printArray(String msg, int[] ia) {
    System.out.print(msg);
    for (int i = 0; i < ia.length; i++) {
      if (i == 0) {
        System.out.print(ia[i]);
      } else {
        System.out.print(", " + ia[i]);
      }
    }
    System.out.println();
  }
}