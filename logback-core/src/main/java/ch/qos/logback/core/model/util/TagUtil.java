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
package ch.qos.logback.core.model.util;

import ch.qos.logback.core.model.Model;

public class TagUtil {

    public static String unifiedTag(Model aModel) {
        String tag = aModel.getTag();

        char first = tag.charAt(0);
        if (Character.isUpperCase(first)) {
            char lower = Character.toLowerCase(first);
            return lower + tag.substring(1);
        } else
            return tag;
    }
}
