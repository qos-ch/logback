/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package chapter11;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class TrivialLogbackAppender extends AppenderBase<LoggingEvent> {

  @Override
  public void start() {
    if (this.layout == null) {
      addError("No layout set for the appender named [" + name + "].");
      return;
    }
    super.start();
  }

  @Override
  protected void append(LoggingEvent loggingevent) {
    // note that AppenderBase.doAppend will invoke this method only if
    // this appender was successfully started.
    
    String s = this.layout.doLayout(loggingevent);
    System.out.println(s);
  }

}
