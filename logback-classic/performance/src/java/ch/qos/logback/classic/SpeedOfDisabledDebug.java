/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2005, QOS.ch, LOGBack.com
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic;

import java.io.IOException;

import org.apache.log4j.Hierarchy;
import org.apache.log4j.spi.RootLogger;
//import org.slf4j.impl.JDK14LoggerFactory;

import ch.qos.logback.classic.HLogger;
import ch.qos.logback.classic.HLoggerContext;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.control.ControlLogger;
import ch.qos.logback.classic.control.ControlLoggerContext;
import ch.qos.logback.classic.control.CreateLogger;
import ch.qos.logback.classic.control.Scenario;
import ch.qos.logback.classic.control.ScenarioMaker;



public class SpeedOfDisabledDebug {
  static LoggerContext loggerContext = new LoggerContext();
  static HLoggerContext hashLoggerContext = new HLoggerContext();
  static ControlLoggerContext controlContext = new ControlLoggerContext();
  static Hierarchy log4jHierarchy = new Hierarchy(new RootLogger(org.apache.log4j.Level.OFF));
  //static JDK14LoggerFactory jdk14FA = new JDK14LoggerFactory();
  static String loggerName;

  public static void main(String[] args) throws IOException {
    loggerContext.getLogger(LoggerContext.ROOT_NAME).setLevel(Level.OFF);
    hashLoggerContext.getRootLogger().setLevel(Level.OFF);
    controlContext.getRootLogger().setLevel(Level.OFF);
    //LogManager
    //jdk14FA.getLogger("").setLevel(java.util.logging.Level.OFF);

    Scenario s = ScenarioMaker.makeTypeBScenario(1000);
    loggerName = ((CreateLogger) s.get(1000)).getLoggerName();
    System.out.println("Logger name is "+loggerName);
    
    final org.slf4j.Logger slf4jLogger = loggerContext.getLogger(loggerName);
    final HLogger hashLogger = hashLoggerContext.getLogger(loggerName);
    final ControlLogger controlLogger = controlContext.getLogger(loggerName);
    //final org.apache.log4j.Logger log4jLogger = log4jHierarchy.getLogger(loggerName);
    //final java.util.logging.Logger jdk14Logger = java.util.logging.Logger.getLogger(loggerName);
    int x1 = 1000*1000;
    for (int i = 0; i < 2; i++) {
      x1 *= 2;
      System.out.println("======= len=" + x1);

      speedTestSLF4JLogger(hashLogger, x1, "Hash logger ");
      speedTestSLF4JLogger(slf4jLogger, x1, "Logback logger ");

      speedTestSLF4JLogger(controlLogger, x1, "Control logger ");
      //speedTestSLF4JLogger(log4jLogger, x1, "Log4j logger ");
      //speedTestSLF4JLogger(jdk14Logger, x1, "JDK14 Logger ");
      speedTestHashLogger(x1);
      speedTestListLogger(x1);
      speedTestControlLogger(x1);
      speedTestLOG4JLogger(x1);
      speedTestJULLogger(x1);
    }
  }


  static void speedTestSLF4JLogger(final org.slf4j.Logger logger, final int len, String loggerType) {
    logger.debug("some message");
    long start = System.nanoTime();
    for (int i = 0; i < len; i++) {
      logger.debug("some message");
    }
    long result = System.nanoTime() - start;
    System.out.println(("SLF4J: " + loggerType) + (result / len));
  }

  static void speedTestListLogger(final int len) {
    final Logger logger = loggerContext.getLogger(loggerName);

    long start = System.nanoTime();
    for (int i = 0; i < len; i++) {
      logger.debug("some message");
    }
    long result = System.nanoTime() - start;
    System.out.println("DIRECT logback logger: " + (result / len));
  }

  static void speedTestHashLogger(final int len) {
    final HLogger logger = hashLoggerContext.getLogger(loggerName);

    long start = System.nanoTime();
    for (int i = 0; i < len; i++) {
      logger.debug("some message");
    }
    long result = System.nanoTime() - start;
    System.out.println("DIRECT Hash logger: " + (result / len));
  }

  static void speedTestLOG4JLogger(final int len) {
    final org.apache.log4j.Logger logger = log4jHierarchy.getLogger(loggerName);
    log4jHierarchy.getLogger("xgssmieubdshsdty");
    log4jHierarchy.getLogger("xgssmieubdshsdty.aqvdsmxzraszxybtrcslsvatbvswq");
    log4jHierarchy.getLogger("xgssmieubdshsdty.aqvdsmxzraszxybtrcslsvatbvswq.yvchlwo");
    log4jHierarchy.getLogger("xgssmieubdshsdty.aqvdsmxzraszxybtrcslsvatbvswq.yvchlwo.xlmoezu");
    long start = System.nanoTime();
    for (int i = 0; i < len; i++) {
      logger.debug("some message");
    }
    long result = System.nanoTime() - start;
    System.out.println("DIRECT LOG4J logger " + result / len);
  }

  static void speedTestJULLogger(final int len) {
    final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(loggerName);
    final java.util.logging.Logger root = java.util.logging.Logger.getLogger("");
    root.setLevel(java.util.logging.Level.OFF);
    long start = System.nanoTime();
    for (int i = 0; i < len; i++) {
      logger.fine("some message");
    }
    long result = System.nanoTime() - start;
    System.out.println("DIRECT JUL logger " + result / len);
  }

  static void speedTestControlLogger(final int len) {
    final ControlLogger logger = controlContext.getLogger(loggerName);

    long start = System.nanoTime();
    for (int i = 0; i < len; i++) {
      logger.debug("some message");
    }
    long result = System.nanoTime() - start;
    System.out.println("DIRECT Control logger: " + result / len);
  }

}
