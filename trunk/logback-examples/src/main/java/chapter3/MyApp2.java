/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package chapter3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

public class MyApp2 {
  final static Logger logger = LoggerFactory.getLogger(MyApp2.class);

  public static void main(String[] args) {
    // assume SLF4J is bound to logback in the current environment
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    // print logback's internal status
    StatusPrinter.print(lc);
    
    logger.info("Entering application.");
    Foo foo = new Foo();
    foo.doIt();
    logger.info("Exiting application.");
  }
}
