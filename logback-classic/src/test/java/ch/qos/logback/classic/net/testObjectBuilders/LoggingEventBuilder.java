/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.net.testObjectBuilders;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;

public class LoggingEventBuilder implements Builder {

  private Logger logger = new LoggerContext()
      .getLogger(LoggerContext.ROOT_NAME);

  public Object build(int i) {
    LoggingEvent le = new LoggingEvent();
    le.setLevel(Level.DEBUG);
    le.setLoggerRemoteView(logger.getLoggerRemoteView());
    // le.setLogger(new LoggerContext().getLogger(LoggerContext.ROOT_NAME));
    le.setMessage(MSG_PREFIX);
    le.setThreadName("threadName");
    return le;
  }
}
