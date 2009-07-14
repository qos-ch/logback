/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.issue.lbclassic135;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.contention.RunnableWithCounterAndDone;

public class LoggingRunnable extends RunnableWithCounterAndDone {

  Logger logger;
  
  public LoggingRunnable(Logger logger) {
    this.logger = logger;
  }
  
  public void run() {
    while(!isDone()) {
      logger.info("hello world ABCDEFGHI");
      counter++;
    }
  }

}
