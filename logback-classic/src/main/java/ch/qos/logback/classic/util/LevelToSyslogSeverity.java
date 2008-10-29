/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.net.SyslogConstants;

public class LevelToSyslogSeverity {

  /*
   * Convert a level to equivalent syslog severity. Only levels for printing
   * methods i.e TRACE, DEBUG, WARN, INFO and ERROR are converted.
   * 
   */
  static public int convert(LoggingEvent event) {

    Level level = event.getLevel();

    switch (level.levelInt) {
    case Level.ERROR_INT:
      return SyslogConstants.ERROR_SEVERITY;
    case Level.WARN_INT:
      return SyslogConstants.WARNING_SEVERITY;
    case Level.INFO_INT:
      return SyslogConstants.INFO_SEVERITY;
    case Level.DEBUG_INT:
      return SyslogConstants.DEBUG_SEVERITY;
    case Level.TRACE_INT:
      return SyslogConstants.DEBUG_SEVERITY;
    default:
      throw new IllegalArgumentException("Level " + level
          + " is not a valid level for a printing method");
    }
  }
}
