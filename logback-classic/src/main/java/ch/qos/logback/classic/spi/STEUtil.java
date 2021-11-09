/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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

public class STEUtil {

    static int UNUSED_findNumberOfCommonFrames(final StackTraceElement[] steArray, final StackTraceElement[] otherSTEArray) {
        if (otherSTEArray == null) {
            return 0;
        }

        int steIndex = steArray.length - 1;
        int parentIndex = otherSTEArray.length - 1;
        int count = 0;
        while (steIndex >= 0 && parentIndex >= 0) {
            if (!steArray[steIndex].equals(otherSTEArray[parentIndex])) {
                break;
            }
            count++;
            steIndex--;
            parentIndex--;
        }
        return count;
    }

    static int findNumberOfCommonFrames(final StackTraceElement[] steArray, final StackTraceElementProxy[] otherSTEPArray) {
        if (otherSTEPArray == null) {
            return 0;
        }

        int steIndex = steArray.length - 1;
        int parentIndex = otherSTEPArray.length - 1;
        int count = 0;
        while (steIndex >= 0 && parentIndex >= 0) {
            if (!steArray[steIndex].equals(otherSTEPArray[parentIndex].ste)) {
                break;
            }
            count++;
            steIndex--;
            parentIndex--;
        }
        return count;
    }
}
