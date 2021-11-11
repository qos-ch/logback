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
package ch.qos.logback.classic.jul;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class JULHelper {

    static public final boolean isRegularNonRootLogger(final java.util.logging.Logger julLogger) {
        if (julLogger == null) {
            return false;
        }
        return !"".equals(julLogger.getName());
    }

    static public final boolean isRoot(final java.util.logging.Logger julLogger) {
        if (julLogger == null) {
            return false;
        }
        return "".equals(julLogger.getName());
    }

    static public java.util.logging.Level asJULLevel(final Level lbLevel) {
        if (lbLevel == null) {
            throw new IllegalArgumentException("Unexpected level [null]");
        }

        switch (lbLevel.levelInt) {
        case Level.ALL_INT:
            return java.util.logging.Level.ALL;
        case Level.TRACE_INT:
            return java.util.logging.Level.FINEST;
        case Level.DEBUG_INT:
            return java.util.logging.Level.FINE;
        case Level.INFO_INT:
            return java.util.logging.Level.INFO;
        case Level.WARN_INT:
            return java.util.logging.Level.WARNING;
        case Level.ERROR_INT:
            return java.util.logging.Level.SEVERE;
        case Level.OFF_INT:
            return java.util.logging.Level.OFF;
        default:
            throw new IllegalArgumentException("Unexpected level [" + lbLevel + "]");
        }
    }

    static public String asJULLoggerName(final String loggerName) {
        if (org.slf4j.Logger.ROOT_LOGGER_NAME.equals(loggerName)) {
            return "";
        }
        return loggerName;
    }

    static public java.util.logging.Logger asJULLogger(final String loggerName) {
        final String julLoggerName = asJULLoggerName(loggerName);
        return java.util.logging.Logger.getLogger(julLoggerName);
    }

    static public java.util.logging.Logger asJULLogger(final Logger logger) {
        return asJULLogger(logger.getName());
    }

}
