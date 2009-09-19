/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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

  public static final Integer OFF_INTEGER = new Integer(OFF_INT);
  public static final Integer ERROR_INTEGER = new Integer(ERROR_INT);
  public static final Integer WARN_INTEGER = new Integer(WARN_INT);
  public static final Integer INFO_INTEGER = new Integer(INFO_INT);
  public static final Integer DEBUG_INTEGER = new Integer(DEBUG_INT);
  public static final Integer TRACE_INTEGER = new Integer(DEBUG_INT);
  public static final Integer ALL_INTEGER = new Integer(ALL_INT);

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
  public final String toString() {
    return levelStr;
  }

  /**
   * Returns the integer representation of this Level.
   */
  public final int toInt() {
    return levelInt;
  }

  /**
   * Convert a Level to an Integer object.
   * 
   * @return This level's Integer mapping.
   */
  public final Integer toInteger() {
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
      throw new IllegalStateException("Level " + levelStr + ", " + levelInt
          + " is unknown.");
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
  public static Level toLevel(String sArg, Level defaultLevel) {
    if (sArg == null) {
      return defaultLevel;
    }

    if (sArg.equalsIgnoreCase("ALL")) {
      return Level.ALL;
    }
    if (sArg.equalsIgnoreCase("TRACE")) {
      return Level.TRACE;
    }
    if (sArg.equalsIgnoreCase("DEBUG")) {
      return Level.DEBUG;
    }
    if (sArg.equalsIgnoreCase("INFO")) {
      return Level.INFO;
    }
    if (sArg.equalsIgnoreCase("WARN")) {
      return Level.WARN;
    }
    if (sArg.equalsIgnoreCase("ERROR")) {
      return Level.ERROR;
    }
    if (sArg.equalsIgnoreCase("OFF")) {
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
}
