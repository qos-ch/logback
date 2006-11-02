/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package chapter1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.BasicConfigurator;
import ch.qos.logback.classic.util.LoggerStatusPrinter;

public class HelloWorld3 {

  public static void main(String[] args) {
    Logger logger = LoggerFactory.getLogger("chapter1.HelloWorld3");

    BasicConfigurator.configureDefaultContext();
    logger.debug("Hello world.");
    LoggerStatusPrinter.printStatusInDefaultContext();
  }
}
