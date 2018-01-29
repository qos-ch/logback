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
package ch.qos.logback.classic;

import org.slf4j.spi.LocationAwareLogger;

/**
 * Defines the set of levels recognized by logback-classic, that is {@link #OFF},
 * {@link #ERROR}, {@link #WARN}, {@link #INFO}, {@link #DEBUG},
 * {@link #TRACE} and {@link #ALL}. <p/> The <code>Level</code> class is
 * final and cannot be sub-classed.
 * </p>
 */
public final class Level implements java.io.Serializable {

    private static final long serialVersionUID = -814092767334282137L;

    public static final int OFF_INT = Integer.MAX_VALUE;
    public static final int ERROR_INT = 40000;
    public static final int WARN_INT = 30000;
    public static final int INFO_INT = 20000;
    public static final int DEBUG_INT = 10000;
    public static final int TRACE_INT = 5000;
    public static final int ALL_INT = Integer.MIN_VALUE;

    public static final Integer OFF_INTEGER = OFF_INT;
    public static final Integer ERROR_INTEGER = ERROR_INT;
    public static final Integer WARN_INTEGER = WARN_INT;
    public static final Integer INFO_INTEGER = INFO_INT;
    public static final Integer DEBUG_INTEGER = DEBUG_INT;
    public static final Integer TRACE_INTEGER = TRACE_INT;
    public static final Integer ALL_INTEGER = ALL_INT;

    /**
     * The <code>OFF</code> is used to turn off logging.
     */
    public static final Level OFF = new Level(OFF_INT, "OFF");

    /**
     * The <code>ERROR</code> level designates error events which may or not
     * be fatal to the application.
     */
    public static final Level ERROR = new Level(ERROR_INT, "ERROR");

    /**
     * The <code>WARN</code> level designates potentially harmful situations.
     */
    public static final Level WARN = new Level(WARN_INT, "WARN");

    /**
     * The <code>INFO</code> level designates informational messages
     * highlighting overall progress of the application.
     */
    public static final Level INFO = new Level(INFO_INT, "INFO");

    /**
     * The <code>DEBUG</code> level designates informational events of lower
     * importance.
     */
    public static final Level DEBUG = new Level(DEBUG_INT, "DEBUG");

    /**
     * The <code>TRACE</code> level designates informational events of very low
     * importance.
     */
    public static final Level TRACE = new Level(TRACE_INT, "TRACE");

    /**
     * The <code>ALL</code> is used to turn on all logging.
     */
    public static final Level ALL = new Level(ALL_INT, "ALL");

    public final int levelInt;
    public final String levelStr;

    /**
     * Instantiate a Level object.
     */
    private Level(int levelInt, String levelStr) {
        this.levelInt = levelInt;
        this.levelStr = levelStr;
    }

    /**
     * Returns the string representation of this Level.
     */
    public String toString() {
        return levelStr;
    }

    /**
     * Returns the integer representation of this Level.
     */
    public int toInt() {
        return levelInt;
    }

    /**
     * Convert a Level to an Integer object.
     *
     * @return This level's Integer mapping.
     */
    public Integer toInteger() {
        switch (levelInt) {
        case ALL_INT:
            return ALL_INTEGER;
        case TRACE_INT:
            return TRACE_INTEGER;
        case DEBUG_INT:
            return DEBUG_INTEGER;
        case INFO_INT:
            return INFO_INTEGER;
        case WARN_INT:
            return WARN_INTEGER;
        case ERROR_INT:
            return ERROR_INTEGER;
        case OFF_INT:
            return OFF_INTEGER;
        default:
            throw new IllegalStateException("Level " + levelStr + ", " + levelInt + " is unknown.");
        }
    }

    /**
     * Returns <code>true</code> if this Level has a higher or equal Level than
     * the Level passed as argument, <code>false</code> otherwise.
     */
    public boolean isGreaterOrEqual(Level r) {
        return levelInt >= r.levelInt;
    }

    /**
     * Convert the string passed as argument to a Level. If the conversion fails,
     * then this method returns {@link #DEBUG}.
     */
    public static Level toLevel(String sArg) {
        return toLevel(sArg, Level.DEBUG);
    }

    /**
     * This method exists in order to comply with Joran's valueOf convention.
     *
     * @param sArg
     * @return
     */
    public static Level valueOf(String sArg) {
        return toLevel(sArg, Level.DEBUG);
    }

    /**
     * Convert an integer passed as argument to a Level. If the conversion fails,
     * then this method returns {@link #DEBUG}.
     */
    public static Level toLevel(int val) {
        return toLevel(val, Level.DEBUG);
    }

    /**
     * Convert an integer passed as argument to a Level. If the conversion fails,
     * then this method returns the specified default.
     */
    public static Level toLevel(int val, Level defaultLevel) {
        switch (val) {
        case ALL_INT:
            return ALL;
        case TRACE_INT:
            return TRACE;
        case DEBUG_INT:
            return DEBUG;
        case INFO_INT:
            return INFO;
        case WARN_INT:
            return WARN;
        case ERROR_INT:
            return ERROR;
        case OFF_INT:
            return OFF;
        default:
            return defaultLevel;
        }
    }

    /**
     * Convert the string passed as argument to a Level. If the conversion fails,
     * then this method returns the value of <code>defaultLevel</code>.
     */
    public static Level toLevel(final String sArg, Level defaultLevel) {
        if (sArg == null) {
            return defaultLevel;
        }

        // see LOGBACK-1288
        final String in = sArg.trim();
        
        if (in.equalsIgnoreCase("ALL")) {
            return Level.ALL;
        }
        if (in.equalsIgnoreCase("TRACE")) {
            return Level.TRACE;
        }
        if (in.equalsIgnoreCase("DEBUG")) {
            return Level.DEBUG;
        }
        if (in.equalsIgnoreCase("INFO")) {
            return Level.INFO;
        }
        if (in.equalsIgnoreCase("WARN")) {
            return Level.WARN;
        }
        if (in.equalsIgnoreCase("ERROR")) {
            return Level.ERROR;
        }
        if (in.equalsIgnoreCase("OFF")) {
            return Level.OFF;
        }
        return defaultLevel;
    }

    /**
     * Return the flyweight instance of the level received through serizalization,
     * i.e. 'this'.
     *
     * @return The appropriate flyweight instance
     */
    private Object readResolve() {
        return toLevel(this.levelInt);
    }

    /**
     * Convert one of the integer values defined in {@link LocationAwareLogger}
     * interface to an instance of this class, i.e. a Level.
     *
     * @param levelInt An integer value representing a level as defined in LocationAwareLogger
     * @return an instance of this class, i.e. a Level.
     * @since 1.0.1
     */
    public static Level fromLocationAwareLoggerInteger(int levelInt) {
        Level level;
        switch (levelInt) {
        case LocationAwareLogger.TRACE_INT:
            level = TRACE;
            break;
        case LocationAwareLogger.DEBUG_INT:
            level = DEBUG;
            break;
        case LocationAwareLogger.INFO_INT:
            level = INFO;
            break;
        case LocationAwareLogger.WARN_INT:
            level = WARN;
            break;
        case LocationAwareLogger.ERROR_INT:
            level = ERROR;
            break;
        default:
            throw new IllegalArgumentException(levelInt + " not a valid level value");
        }
        return level;
    }

    /**
     * Convert this level instance to an integer  value defined in the
     * {@link LocationAwareLogger} interface.
     *
     * @param level The level to convert to LocationAwareLogger integer
     * @return int An integer corresponding to this level as defined in LocationAwareLogger
     * @since 1.0.1
     */
    public static int toLocationAwareLoggerInteger(Level level) {
        if (level == null)
            throw new IllegalArgumentException("null level parameter is not admitted");
        switch (level.toInt()) {
        case Level.TRACE_INT:
            return LocationAwareLogger.TRACE_INT;
        case Level.DEBUG_INT:
            return LocationAwareLogger.DEBUG_INT;
        case Level.INFO_INT:
            return LocationAwareLogger.INFO_INT;
        case Level.WARN_INT:
            return LocationAwareLogger.WARN_INT;
        case Level.ERROR_INT:
            return LocationAwareLogger.ERROR_INT;
        default:
            throw new IllegalArgumentException(level + " not a valid level value");
        }
    }
}
