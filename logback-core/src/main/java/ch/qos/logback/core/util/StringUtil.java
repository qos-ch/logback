/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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
 * @since 1.5.0
 */
public class StringUtil {

    /**
     * Returns true if input str is null or empty.
     *
     * @param str
     * @return
     */
    public static boolean isNullOrEmpty(String str) {
        return ((str == null) || str.isEmpty());
    }

    /**
     * Returns true if input str is not null nor empty.
     *
     * @param str
     * @return
     */
    public static boolean notNullNorEmpty(String str) {
        return !isNullOrEmpty(str);
    }

    public static String capitalizeFirstLetter(String name) {
        if (isNullOrEmpty(name))
            return name;

        if(name.length() == 1) {
            return name.toUpperCase();
        } else
            return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String lowercaseFirstLetter(String name) {
        if (isNullOrEmpty(name))
            return name;

        if(name.length() == 1) {
            return name.toLowerCase();
        } else
            return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

}
