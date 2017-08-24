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
package ch.qos.logback.core.util;

/**
 * Various utility methods for processing strings representing context types.
 * 
 * @author Ceki Gulcu
 * 
 */
public class ContentTypeUtil {

    public static boolean isTextual(String contextType) {
        if (contextType == null) {
            return false;
        }
        return contextType.startsWith("text");
    }

    public static String getSubType(String contextType) {
        if (contextType == null) {
            return null;
        }
        int index = contextType.indexOf('/');
        if (index == -1) {
            return null;
        } else {
            int subTypeStartIndex = index + 1;
            if (subTypeStartIndex < contextType.length()) {
                return contextType.substring(subTypeStartIndex);
            } else {
                return null;
            }
        }
    }
}
