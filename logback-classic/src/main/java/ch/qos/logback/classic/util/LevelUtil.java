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

package ch.qos.logback.classic.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.util.OptionHelper;

import static ch.qos.logback.core.joran.JoranConstants.NULL;

/**
 *
 * Utility methods for transforming string values to Level.
 *
 * @since 1.5.1
 */
public class LevelUtil {


    public static boolean isInheritedLevelString(String levelStr) {
        if (JoranConstants.INHERITED.equalsIgnoreCase(levelStr) || NULL.equalsIgnoreCase(levelStr)) {
            return true;
        } else
            return false;
    }

    public static Level levelStringToLevel(String levelStr) {
        if (!OptionHelper.isNullOrEmptyOrAllSpaces(levelStr)) {
            if (isInheritedLevelString(levelStr)) {
                return null;
            } else {
                Level level = Level.toLevel(levelStr);
                return level;
            }
        }
        return null;
    }

}
