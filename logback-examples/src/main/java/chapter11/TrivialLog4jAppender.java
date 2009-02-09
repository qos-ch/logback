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

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class TrivialLog4jAppender extends AppenderSkeleton {

  protected void append(LoggingEvent loggingevent) {
    String s = this.layout.format(loggingevent);
    System.out.println(s);
  }

  public void close() {
    // nothing to do
  }

  public boolean requiresLayout() {
    return true;
  }

}
