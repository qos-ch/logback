/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.testUtil;

public class VersionUtil {

    static final int DEFAULT_GUESS = 6;

    static public int getJavaMajorVersion() {
        String javaVersionString = System.getProperty("java.version");
        int result = getJavaMajorVersion(javaVersionString);
        return result;
    }

    static public int getJavaMajorVersion(String versionString) {
        if (versionString == null)
            return DEFAULT_GUESS;
        if (versionString.startsWith("1.")) {
            return versionString.charAt(2) - '0';
        } else {
            String firstDigits = extractFirstDigits(versionString);
            try {
               return Integer.parseInt(firstDigits);
            } catch(NumberFormatException e) {
                return DEFAULT_GUESS;
            }
        }
    }

    private static String extractFirstDigits(String versionString) {
        StringBuffer buf = new StringBuffer();
        for (char c : versionString.toCharArray()) {
            if (Character.isDigit(c))
                buf.append(c);
            else
                break;
        }
        return buf.toString();

    }
}
