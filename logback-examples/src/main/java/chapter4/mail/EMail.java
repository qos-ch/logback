/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package chapter4.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.util.StatusPrinter;



/**
 * This application generates log messages in numbers specified by the
 * user. See
 * also configuration scripts rolling.properties and rolling.xml.
 * */
public class EMail {
  static public void main(String[] args) throws Exception {
    if (args.length != 2) {
      usage("Wrong number of arguments.");
    }

    int runLength = Integer.parseInt(args[0]);
    String configFile = args[1];

    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    JoranConfigurator configurator = new JoranConfigurator();
    lc.reset();
    configurator.setContext(lc);
    configurator.doConfigure(configFile);
    StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
    
    Logger logger = LoggerFactory.getLogger(EMail.class);

    for (int i = 1; i <= runLength; i++) {
      if ((i % 10) < 9) {
        logger.debug("This is a debug message. Message number: " + i);
      } else {
        logger.warn("This is a warning message. Message number: " + i);
      }
    }

    logger.error("At last an error.", new Exception("Just testing"));
    
    StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
  }

  static void usage(String msg) {
    System.err.println(msg);
    System.err.println("Usage: java " + EMail.class.getName() +
      " runLength configFile\n" +
      "   runLength (integer) the number of logs to generate\n" +
      "   configFile a logback configuration file in XML format." +
      " XML files must have a '.xml' extension.");
    System.exit(1);
  }
}
