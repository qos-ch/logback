/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.net.SyslogConstants;

public class LevelToSyslogSeverity {

  /*
   * Convert a level to equivalent syslog severity. Only levels for printing
   * methods i.e TRACE, DEBUG, WARN, INFO and ERROR are converted.
   * 
   */
  static public int convert(ILoggingEvent event) {

    Level level = event.getLevel();

    switch (level.levelInt) {
    case Level.ERROR_INT:
      return SyslogConstants.ERROR_SEVERITY;
    case Level.WARN_INT:
      return SyslogConstants.WARNING_SEVERITY;
    case Level.INFO_INT:
      return SyslogConstants.INFO_SEVERITY;
    case Level.DEBUG_INT:
    case Level.TRACE_INT:
      return SyslogConstants.DEBUG_SEVERITY;
    default:
      throw new IllegalArgumentException("Level " + level
          + " is not a valid level for a printing method");
    }
  }
}
