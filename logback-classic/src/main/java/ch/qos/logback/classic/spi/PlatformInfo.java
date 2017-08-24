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

/**
 * This class provides information about the runtime platform.
 *
 * @author Ceki Gulcu
 * */
public class PlatformInfo {

    private static final int UNINITIALIZED = -1;

    private static int hasJMXObjectName = UNINITIALIZED;

    public static boolean hasJMXObjectName() {
        if (hasJMXObjectName == UNINITIALIZED) {
            try {
                Class.forName("javax.management.ObjectName");
                hasJMXObjectName = 1;
            } catch (Throwable e) {
                hasJMXObjectName = 0;
            }
        }
        return (hasJMXObjectName == 1);
    }
}
