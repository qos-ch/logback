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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * A minimal application making use of logback-classic. It uses the
 * configuration file logback-trivial.xml which makes use of
 * TivialLogbackAppender.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class LogbackMain {

  static Logger logger = LoggerFactory.getLogger(LogbackMain.class);

  public static void main(String[] args) throws JoranException {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

    JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(lc);
    lc.reset();
    configurator.doConfigure("src/main/java/chapter11/logback-trivial.xml");
    StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
    
    
    logger.debug("Hello world");
  }

}
