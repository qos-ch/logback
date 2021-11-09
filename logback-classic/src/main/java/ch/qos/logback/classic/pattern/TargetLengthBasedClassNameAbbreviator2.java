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
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.core.CoreConstants;

public class TargetLengthBasedClassNameAbbreviator2 implements Abbreviator {

    final int targetLength;

    public TargetLengthBasedClassNameAbbreviator2(final int targetLength) {
        this.targetLength = targetLength;
    }

    @Override
    public String abbreviate(final String fqClassName) {
        final StringBuilder buf = new StringBuilder(targetLength);
        if (fqClassName == null) {
            throw new IllegalArgumentException("Class name may not be null");
        }

        final int inLen = fqClassName.length();
        if (inLen < targetLength) {
            return fqClassName;
        }

        final int[] dotIndexesArray = new int[ClassicConstants.MAX_DOTS];
        // a.b.c contains 2 dots but 2+1 parts.
        // see also http://jira.qos.ch/browse/LOGBACK-437
        final int[] lengthArray = new int[ClassicConstants.MAX_DOTS + 1];

        final int dotCount = computeDotIndexes(fqClassName, dotIndexesArray);

        // System.out.println();
        // System.out.println("Dot count for [" + className + "] is " + dotCount);
        // if there are no dots than abbreviation is not possible
        if (dotCount == 0) {
            return fqClassName;
        }
        //printArray("dotArray: ", dotIndexesArray);
        computeLengthArray(fqClassName, dotIndexesArray, lengthArray, dotCount);
        //printArray("lengthArray: ", lengthArray);
        for (int i = 0; i <= dotCount; i++) {
            if (i == 0) {
                buf.append(fqClassName.substring(0, lengthArray[i] - 1));
            } else {
                buf.append(fqClassName.substring(dotIndexesArray[i - 1], dotIndexesArray[i - 1] + lengthArray[i]));
            }
            //System.out.println("i=" + i + ", buf=" + buf);
        }

        return buf.toString();
    }

    /**
     * Populate dotArray with the positions of the DOT character in className.
     * Leftmost dot is placed at index 0 of dotArray.
     *
     * @param className
     * @param dotArray
     * @return the number of dots found
     */
    static int computeDotIndexes(final String className, final int[] dotArray) {
        int dotCount = 0;
        int k = 0;
        while (true) {
            // ignore the $ separator in our computations. This is both convenient
            // and sensible.
            k = className.indexOf(CoreConstants.DOT, k);
            if (k == -1 || dotCount >= ClassicConstants.MAX_DOTS) {
                break;
            }
            dotArray[dotCount] = k;
            dotCount++;
            k++; // move past the last found DOT
        }
        return dotCount;
    }

    void computeLengthArray(final String className, final int[] dotArray, final int[] lengthArray, final int dotCount) {
        int toTrim = className.length() - targetLength;
        //System.out.println("dotCount=" + dotCount);

        int len;
        for (int i = 0; i < dotCount; i++) {
            //System.out.println("i=" + i + ", toTrim = " + toTrim);


            // if i==0, previousDotPosition = -1, otherwise dotArray[i - 1]
            final int previousDotPosition = i == 0 ? -1 : dotArray[i - 1];
            //System.out.println("i="+i+ " previousDotPosition="+previousDotPosition);

            // number of characters within the segment, i.e/ within the previous dot position and the current dot position
            final int charactersInSegment = dotArray[i] - previousDotPosition - 1;
            //System.out.println("i=" + i + ", charactersInSegment = " + charactersInSegment);


            if (toTrim > 0) {
                len = charactersInSegment < 1 ? charactersInSegment : 1;
            } else {
                len = charactersInSegment;
            }
            //System.out.println("i=" + i + ", len = " + len);

            toTrim -= charactersInSegment - len;
            lengthArray[i] = len + 1;
        }

        final int lastDotIndex = dotCount - 1;
        lengthArray[dotCount] = className.length() - dotArray[lastDotIndex];
    }

    static void printArray(final String msg, final int[] ia) {
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