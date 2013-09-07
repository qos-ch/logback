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
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.core.CoreConstants;

public class TargetLengthBasedClassNameAbbreviator implements Abbreviator {

  final int targetLength;

  public TargetLengthBasedClassNameAbbreviator(int targetLength) {
    this.targetLength = targetLength;
  }

  public String abbreviate(String fqClassName) {
    StringBuilder buf = new StringBuilder(targetLength);
    if (fqClassName == null) {
      throw new IllegalArgumentException("Class name may not be null");
    }

    int inLen = fqClassName.length();
    if (inLen < targetLength) {
      return fqClassName;
    }

    int[] dotIndexesArray = new int[ClassicConstants.MAX_DOTS];
    // a.b.c contains 2 dots but 2+1 parts.
    // see also http://jira.qos.ch/browse/LBCLASSIC-110
    int[] lengthArray = new int[ClassicConstants.MAX_DOTS + 1];

    int dotCount = computeDotIndexes(fqClassName, dotIndexesArray);

    // System.out.println();
    // System.out.println("Dot count for [" + className + "] is " + dotCount);
    // if there are not dots than abbreviation is not possible
    if (dotCount == 0) {
      return fqClassName;
    }
    // printArray("dotArray: ", dotArray);
    computeLengthArray(fqClassName, dotIndexesArray, lengthArray, dotCount);
    // printArray("lengthArray: ", lengthArray);
    for (int i = 0; i <= dotCount; i++) {
      if (i == 0) {
        buf.append(fqClassName.substring(0, lengthArray[i] - 1));
      } else {
        buf.append(fqClassName.substring(dotIndexesArray[i - 1],
            dotIndexesArray[i - 1] + lengthArray[i]));
      }
      // System.out.println("i=" + i + ", buf=" + buf);
    }

    return buf.toString();
  }

  static int computeDotIndexes(final String className, int[] dotArray) {
    int dotCount = 0;
    int k = 0;
    while (true) {
      // ignore the $ separator in our computations. This is both convenient
      // and sensible.
      k = className.indexOf(CoreConstants.DOT, k);
      if (k != -1 && dotCount < ClassicConstants.MAX_DOTS) {
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