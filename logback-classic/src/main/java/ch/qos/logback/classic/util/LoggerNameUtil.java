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
package ch.qos.logback.classic.util;

import ch.qos.logback.core.CoreConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for analysing logger names.
 */
public class LoggerNameUtil {

    public static int getFirstSeparatorIndexOf(String name) {
        return getSeparatorIndexOf(name, 0);
    }

    /**
     * Get the position of the separator character, if any, starting at position
     * 'fromIndex'.
     *
     * @param name
     * @param fromIndex
     * @return
     */
    public static int getSeparatorIndexOf(String name, int fromIndex) {
        int dotIndex = name.indexOf(CoreConstants.DOT, fromIndex);
        int dollarIndex = name.indexOf(CoreConstants.DOLLAR, fromIndex);

        if (dotIndex == -1 && dollarIndex == -1)
            return -1;
        if (dotIndex == -1)
            return dollarIndex;
        if (dollarIndex == -1)
            return dotIndex;

        return dotIndex < dollarIndex ? dotIndex : dollarIndex;
    }

    public static List<String> computeNameParts(String loggerName) {
        List<String> partList = new ArrayList<String>();

        int fromIndex = 0;
        while (true) {
            int index = getSeparatorIndexOf(loggerName, fromIndex);
            if (index == -1) {
                partList.add(loggerName.substring(fromIndex));
                break;
            }
            partList.add(loggerName.substring(fromIndex, index));
            fromIndex = index + 1;
        }
        return partList;
    }
}
