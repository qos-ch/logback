/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import ch.qos.logback.classic.control.ControlLogger;
import ch.qos.logback.classic.control.ControlLoggerContext;
import ch.qos.logback.classic.control.CreateLogger;
import ch.qos.logback.classic.control.ScenarioAction;
import ch.qos.logback.classic.control.ScenarioMaker;
import ch.qos.logback.classic.control.SetLevel;
import ch.qos.logback.classic.control.Scenario;

public class ScenarioBasedLoggerContextTest {
    LoggerContext lc;

    @Test
    public void testLen3() {
        doScenarioedTest(3);
    }

    @Test
    public void testLength_30() {
        doScenarioedTest(30);
    }

    @Test
    public void testLength_20000() {
        doScenarioedTest(20 * 1000);
    }

    @Test
    @Ignore
    public void testLengthLong() {
        doScenarioedTest(100 * 1000);
    }

    private void doScenarioedTest(int len) {
        LoggerContext lc = new LoggerContext();
        ControlLoggerContext controlContext = new ControlLoggerContext();
        Scenario s = ScenarioMaker.makeRealisticCreationScenario(len);
        List<ScenarioAction> actionList = s.getActionList();
        int size = actionList.size();
        for (int i = 0; i < size; i++) {
            ScenarioAction action = (ScenarioAction) actionList.get(i);
            if (action instanceof CreateLogger) {
                CreateLogger cl = (CreateLogger) action;
                lc.getLogger(cl.getLoggerName());
                controlContext.getLogger(cl.getLoggerName());
            } else if (action instanceof SetLevel) {
                SetLevel sl = (SetLevel) action;
                Logger l = lc.getLogger(sl.getLoggerName());
                ControlLogger controlLogger = controlContext.getLogger(sl.getLoggerName());
                l.setLevel(sl.getLevel());
                controlLogger.setLevel(sl.getLevel());
            }
        }

        compareLoggerContexts(controlContext, lc);
    }

    void compareLoggerContexts(ControlLoggerContext controlLC, LoggerContext lc) {
        Map<String, ControlLogger> controlLoggerMap = controlLC.getLoggerMap();

        assertEquals(controlLoggerMap.size() + 1, lc.size());

        for (String loggerName : controlLoggerMap.keySet()) {

            Logger logger = lc.exists(loggerName);
            ControlLogger controlLogger = (ControlLogger) controlLoggerMap.get(loggerName);
            if (logger == null) {
                throw new IllegalStateException("logger" + loggerName + " should exist");
            }
            assertEquals(loggerName, logger.getName());
            assertEquals(loggerName, controlLogger.getName());

            compareLoggers(controlLogger, logger);
        }
    }

    void compareLoggers(ControlLogger controlLogger, Logger logger) {
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