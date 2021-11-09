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
package ch.qos.logback.core.helpers;

import java.util.LinkedList;
import java.util.List;

import ch.qos.logback.core.CoreConstants;

public class ThrowableToStringArray {

    public static String[] convert(final Throwable t) {
        final List<String> strList = new LinkedList<>();
        extract(strList, t, null);
        return strList.toArray(new String[0]);

    }

    private static void extract(final List<String> strList, final Throwable t, final StackTraceElement[] parentSTE) {

        final StackTraceElement[] ste = t.getStackTrace();
        final int numberOfcommonFrames = findNumberOfCommonFrames(ste, parentSTE);

        strList.add(formatFirstLine(t, parentSTE));
        for (int i = 0; i < ste.length - numberOfcommonFrames; i++) {
            strList.add("\tat " + ste[i].toString());
        }

        if (numberOfcommonFrames != 0) {
            strList.add("\t... " + numberOfcommonFrames + " common frames omitted");
        }

        final Throwable cause = t.getCause();
        if (cause != null) {
            ThrowableToStringArray.extract(strList, cause, ste);
        }
    }

    private static String formatFirstLine(final Throwable t, final StackTraceElement[] parentSTE) {
        String prefix = "";
        if (parentSTE != null) {
            prefix = CoreConstants.CAUSED_BY;
        }

        final StringBuilder result = new StringBuilder().append(prefix).append(t.getClass().getName());
        if (t.getMessage() != null) {
            result.append(": ").append(t.getMessage());
        }
        return result.toString();
    }

    private static int findNumberOfCommonFrames(final StackTraceElement[] ste, final StackTraceElement[] parentSTE) {
        if (parentSTE == null) {
            return 0;
        }

        int steIndex = ste.length - 1;
        int parentIndex = parentSTE.length - 1;
        int count = 0;
        while (steIndex >= 0 && parentIndex >= 0) {
            if (!ste[steIndex].equals(parentSTE[parentIndex])) {
                break;
            }
            count++;
            steIndex--;
            parentIndex--;
        }
        return count;
    }

}
