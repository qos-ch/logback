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


public class CreateLogger extends ControlAction {

  final String loggerName;

  public CreateLogger(String loggerName) {
    this.loggerName = loggerName;
  }

  public String getLoggerName() {
    return loggerName;
  }

  public String toString() {
    return "CreateLogger("+loggerName+")";
  }
}
