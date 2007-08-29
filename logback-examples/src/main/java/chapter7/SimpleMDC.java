/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package chapter7;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.ConsoleAppender;

public class SimpleMDC {
  static public void main(String[] args) throws Exception {
    // You can put values in the MDC at any time. We first put the
    // first name
    MDC.put("first", "Dorothy");

    // Configure logback
    PatternLayout layout = new PatternLayout();
    layout.setPattern("%X{first} %X{last} - %m%n");
    layout.start();
    ConsoleAppender<LoggingEvent> appender = new ConsoleAppender<LoggingEvent>();
    appender.setLayout(layout);
    appender.start();
    // cast root logger to c.q.logback.classic.Logger so that we can attach an
    // appender to it
    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
        .getLogger("root");
    root.addAppender(appender);

    // get another logger
    Logger logger = LoggerFactory.getLogger(SimpleMDC.class);

    // We now put the last name
    MDC.put("last", "Parker");

    // The most beautiful two words in the English language according
    // to Dorothy Parker:
    logger.info("Check enclosed.");
    logger.debug("The most beautiful two words in English.");

    MDC.put("first", "Richard");
    MDC.put("last", "Nixon");
    logger.info("I am not a crook.");
    logger.info("Attributed to the former US president. 17 Nov 1973.");
  }
}
