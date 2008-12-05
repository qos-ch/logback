/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package chapter2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.util.LoggerStatusPrinter;

public class HelloWorld2 {

  public static void main(String[] args) {
    Logger logger = LoggerFactory.getLogger("chapter2.HelloWorld2");
    logger.debug("Hello world.");
    LoggerStatusPrinter.printStatusInDefaultContext();
  }
}
