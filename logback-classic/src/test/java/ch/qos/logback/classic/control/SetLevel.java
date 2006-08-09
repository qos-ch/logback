/** 
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2005, QOS.ch, LOGBack.com
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.control;

import ch.qos.logback.classic.Level;

public class SetLevel extends TestAction {
  final String loggerName;
  final Level level;

  public SetLevel(Level level, String loggerName) {
    this.level = level;
    this.loggerName = loggerName;
  }

  public Level getLevel() {
    return level;
  }

  public String getLoggerName() {
    return loggerName;
  }
  public String toString() {
    return "SetLevel("+level+", "+loggerName+")";
  }
}
