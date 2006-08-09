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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.control.ControlLogger;
import ch.qos.logback.classic.control.ControlLoggerContext;
import ch.qos.logback.classic.control.CreateLogger;
import ch.qos.logback.classic.control.Scenario;
import ch.qos.logback.classic.control.ScenarioMaker;
import ch.qos.logback.classic.control.SetLevel;
import ch.qos.logback.classic.control.TestAction;


public class DynamicLoggerContextTest extends TestCase {
  LoggerContext lc;


  public void test3() {
    dynaTest(3);
  }

  public void test30() {
    dynaTest(30);
  }

  public void test1000() {
    dynaTest(1000);
  }
  //public void test50000() {
    //dynaTest(50000);
  //}


  private void dynaTest(int len) {
    LoggerContext lc = new LoggerContext();
    ControlLoggerContext controlContext = new ControlLoggerContext();
    Scenario s = ScenarioMaker.makeTypeBScenario(len);
    List actionList = s.getActionList();
    int size = actionList.size();
    for (int i = 0; i < size; i++) {
      TestAction action = (TestAction) actionList.get(i);
      if (action instanceof CreateLogger) {
        CreateLogger cl = (CreateLogger) action;
        lc.getLogger(cl.getLoggerName());
        controlContext.getLogger(cl.getLoggerName());
      } else {
        SetLevel sl = (SetLevel) action;
        Logger l = lc.getLogger(sl.getLoggerName());
        ControlLogger controlLogger = controlContext.getLogger(sl.getLoggerName());
        l.setLevel(sl.getLevel());
        controlLogger.setLevel(sl.getLevel());
      }
    }

    compare(controlContext, lc);
  }

  void compare(ControlLoggerContext controlLC, LoggerContext lc) {
    Map controlLoggerMap = controlLC.getLoggerMap();

    assertEquals(controlLoggerMap.size()+1, lc.size());

    for (Iterator i = controlLoggerMap.keySet().iterator(); i.hasNext();) {
      String loggerName = (String) i.next();
      Logger logger = lc.exists(loggerName);
      ControlLogger controlLogger = (ControlLogger) controlLoggerMap.get(loggerName);
      if (logger == null) {
        throw new IllegalStateException("HLoggerr" + loggerName + " should exist");
      }
      assertEquals(loggerName, logger.getName());
      assertEquals(loggerName, controlLogger.getName());

      assertCompare(controlLogger, logger);
    }
  }

  void assertCompare(ControlLogger controlLogger, Logger logger) {
    assertEquals(controlLogger.getName(), logger.getName());
    assertEquals(controlLogger.getEffectiveLevel(), logger.getEffectiveLevel());

    Level controlLevel = controlLogger.getLevel();
    Level level = logger.getLevel();

    if (controlLevel == null) {
      assertNull(level);
    } else {
      assertEquals(controlLevel, level);
    }
  }
}