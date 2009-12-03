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

import ch.qos.logback.classic.HLoggerContext;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.control.ControlLoggerContext;
import ch.qos.logback.classic.control.CreateLogger;
import ch.qos.logback.classic.control.Scenario;
import ch.qos.logback.classic.control.ScenarioMaker;



public class RetreivalOfExistingLoggerSpeed {
  static final LoggerContext listLoggerContext = new LoggerContext();
  static final HLoggerContext hashLoggerContext = new HLoggerContext();
  static final ControlLoggerContext controlContext = new ControlLoggerContext();
  static final Hierarchy log4jHierarchy = new Hierarchy(new RootLogger(org.apache.log4j.Level.OFF));

  static String X;

  public static void main(String[] args) throws IOException {

    Scenario s = ScenarioMaker.makeTypeBScenario(2000);

    X = ((CreateLogger) s.get(1000)).getLoggerName();
    System.out.println("name:"+X);
    System.err.print("Press a key to continue: ");
    System.in.read();
    int x1 = 100000;
    for (int i = 0; i < 1; i++) {
      x1 *= 2;
      getLoggerSpeed(x1);
      //getHLoggerSpeed(x1);
      getLog4jLoggerSpeed(x1);
      getContolLoggerSpeed(x1);
      getJULSpeed(x1);
    }
  }

  static void getLoggerSpeed(final int len) {
    long start = System.nanoTime();
    for (int i = 0; i < len; i++) {
      listLoggerContext.getLogger(X);
    }
    long result = System.nanoTime() - start;
    System.out.println(("Logger ") + (result / len));
  }

  static void getHLoggerSpeed(final int len) {
    long start = System.nanoTime();
    for (int i = 0; i < len; i++) {
      hashLoggerContext.getLogger(X);
    }
    long result = System.nanoTime() - start;
    System.out.println(("HLogger ") + (result / len));
  }
  static void getContolLoggerSpeed(final int len) {
    long start = System.nanoTime();
    for (int i = 0; i < len; i++) {
      controlContext.getLogger(X);
    }
    long result = System.nanoTime() - start;
    System.out.println(("ControlLogger ") + (result / len));
  }
    static void getJULSpeed(final int len) {
    long start = System.nanoTime();
    for (int i = 0; i < len; i++) {
      java.util.logging.Logger.getLogger(X);
    }
    long result = System.nanoTime() - start;
    System.out.println(("JUL     ") + (result / len));
  }
  static void getLog4jLoggerSpeed(final int len) {
    long start = System.nanoTime();
    for (int i = 0; i < len; i++) {
      log4jHierarchy.getLogger(X);
    }
    long result = System.nanoTime() - start;
    System.out.println(("Log4j Logger ") + (result / len));
  }
}
