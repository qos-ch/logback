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
import java.util.List;

import org.apache.log4j.Hierarchy;
import org.apache.log4j.spi.RootLogger;

import ch.qos.logback.classic.HLoggerContext;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.control.ControlLoggerContext;
import ch.qos.logback.classic.control.CreateLogger;
import ch.qos.logback.classic.control.Scenario;
import ch.qos.logback.classic.control.ScenarioMaker;
import ch.qos.logback.classic.control.TestAction;



public class LoggerCreation {
  static LoggerContext listLoggerContext = new LoggerContext();
  static HLoggerContext hashLoggerContext = new HLoggerContext();
  static ControlLoggerContext controlContext = new ControlLoggerContext();
  static Hierarchy log4jHierarchy = new Hierarchy(new RootLogger(org.apache.log4j.Level.OFF));

  public static void main(String[] args) throws IOException, InterruptedException {

    Scenario s = ScenarioMaker.makeTypeBScenario(3000);
    System.err.print("Press a key to continue: ");
    System.in.read();


    testEmptyLoggers(s);
    testListLoggers(s);
    testJULLoggers(s);
    tesLOG4JLoggers(s);
    testControlLoggers(s);
    testHashLoggers(s);

  }


  static void testEmptyLoggers(Scenario s) {

    List actionList = s.getActionList();
    int size = actionList.size();
    long start = System.nanoTime();
    CreateLogger cl = null;
    for (int i = 0; i < size; i++) {
      TestAction action = (TestAction) actionList.get(i);
      if (action instanceof CreateLogger) {
        cl = (CreateLogger) action;
      }
    }
    if(cl == null) {
      // bogus if block to keep eclipse happy
    }
    long result = System.nanoTime() - start;
    System.out.println("Average (in nanos) Emtpy logger creation: " + (result / s.size()));

  }

  static void testListLoggers(Scenario s) {

    List actionList = s.getActionList();
    int size = actionList.size();
    long start = System.nanoTime();
    for (int i = 0; i < size; i++) {
      TestAction action = (TestAction) actionList.get(i);
      if (action instanceof CreateLogger) {
        CreateLogger cl = (CreateLogger) action;
        listLoggerContext.getLogger(cl.getLoggerName());
      }
    }
    long result = System.nanoTime() - start;
    System.out.println("Average (in nanos) List logger creation: " + (result / s.size()));

  }

  static void testHashLoggers(Scenario s) {
    List actionList = s.getActionList();
    int size = actionList.size();
    long start = System.nanoTime();
    for (int i = 0; i < size; i++) {
      TestAction action = (TestAction) actionList.get(i);
      if (action instanceof CreateLogger) {
        CreateLogger cl = (CreateLogger) action;
        hashLoggerContext.getLogger(cl.getLoggerName());
      }
    }
    long result = System.nanoTime() - start;
    System.out.println("Average (in nanos) Hash logger creation: " + (result / s.size()));

  }

  static void testControlLoggers(Scenario s) {

    List actionList = s.getActionList();
    int size = actionList.size();
    long start = System.nanoTime();
    for (int i = 0; i < size; i++) {
      TestAction action = (TestAction) actionList.get(i);
      if (action instanceof CreateLogger) {
        CreateLogger cl = (CreateLogger) action;
        controlContext.getLogger(cl.getLoggerName());
      }
    }
    long result = System.nanoTime() - start;
    System.out.println("Average (in nanos) Control logger creation: " + (result / s.size()));
  }

  static void tesLOG4JLoggers(Scenario s) {

    List actionList = s.getActionList();
    int size = actionList.size();
    long start = System.nanoTime();
    for (int i = 0; i < size; i++) {
      TestAction action = (TestAction) actionList.get(i);
      if (action instanceof CreateLogger) {
        CreateLogger cl = (CreateLogger) action;
        log4jHierarchy.getLogger(cl.getLoggerName());
      }
    }
    long result = System.nanoTime() - start;
    System.out.println("Average (in nanos) LOG4J logger creation: " + (result / s.size()));
  }

  static void testJULLoggers(Scenario s) {

    List actionList = s.getActionList();
    int size = actionList.size();
    long start = System.nanoTime();
    for (int i = 0; i < size; i++) {
      TestAction action = (TestAction) actionList.get(i);
      if (action instanceof CreateLogger) {
        CreateLogger cl = (CreateLogger) action;
        java.util.logging.Logger.getLogger(cl.getLoggerName());
      }
    }
    long result = System.nanoTime() - start;
    System.out.println("Average (in nanos) JUL logger creation: " + (result / s.size()));
  }

}
