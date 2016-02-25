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
package ch.qos.logback.core.pattern;

public class SpacePadder {

    final static String[] SPACES = { " ", "  ", "    ", "        ", // 1,2,4,8
            // spaces
            "                ", // 16 spaces
            "                                " }; // 32 spaces

    final static public void leftPad(StringBuilder buf, String s, int desiredLength) {
        int actualLen = 0;
        if (s != null) {
            actualLen = s.length();
        }
        if (actualLen < desiredLength) {
            spacePad(buf, desiredLength - actualLen);
        }
        if (s != null) {
            buf.append(s);
        }
    }

    final static public void rightPad(StringBuilder buf, String s, int desiredLength) {
        int actualLen = 0;
        if (s != null) {
            actualLen = s.length();
        }
        if (s != null) {
            buf.append(s);
        }
        if (actualLen < desiredLength) {
            spacePad(buf, desiredLength - actualLen);
        }
    }

    /**
     * Fast space padding method.
     */
    final static public void spacePad(StringBuilder sbuf, int length) {
        while (length >= 32) {
            sbuf.append(SPACES[5]);
            length -= 32;
        }

        for (int i = 4; i >= 0; i--) {
            if ((length & (1 << i)) != 0) {
                sbuf.append(SPACES[i]);
            }
        }
    }
}
