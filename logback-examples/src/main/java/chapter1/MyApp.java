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

// Import SLF4J classes.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.BasicConfigurator;

public class MyApp {

  public static void main(String[] args) {
    // Set up a simple configuration that logs on the console.
    BasicConfigurator.configureDefaultContext();

    Logger logger = LoggerFactory.getLogger(MyApp.class);

    logger.info("Entering application.");
    Bar bar = new Bar();
    bar.doIt();
    logger.info("Exiting application.");
  }
}
