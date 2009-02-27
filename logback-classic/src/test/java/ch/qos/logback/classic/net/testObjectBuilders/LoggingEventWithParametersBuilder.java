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

public class LoggingEventWithParametersBuilder implements Builder {

  final String MSG = "aaaaabbbbbcccc {} cdddddaaaaabbbbbcccccdddddaaaa {}";

  private Logger logger = new LoggerContext()
      .getLogger(LoggerContext.ROOT_NAME);

  public Object build(int i) {

    LoggingEvent le = new LoggingEvent();
    le.setTimeStamp(System.currentTimeMillis());

    Object[] aa = new Object[] { i, "HELLO WORLD [========== ]" + i };

    le.setArgumentArray(aa);
    String msg = MSG + i;
    le.setMessage(msg);

    // compute formatted message
    // this forces le.formmatedMessage to be set (this is the whole point of the
    // exercise)
    le.getFormattedMessage();
    le.setLevel(Level.DEBUG);
    le.setLoggerName(logger.getName());
    le.setLoggerContextRemoteView(logger.getLoggerRemoteView().getLoggerContextView());
    le.setThreadName("threadName");

    return le;
  }
}
