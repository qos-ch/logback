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
package ch.qos.logback.classic.jul;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.testUtil.RandomUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LevelChangePropagatorTest {
    int rand = RandomUtil.getPositiveInt();
    LoggerContext loggerContext = new LoggerContext();
    LevelChangePropagator levelChangePropagator = new LevelChangePropagator();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        levelChangePropagator.setContext(loggerContext);
        loggerContext.addListener(levelChangePropagator);
    }

    void checkLevelChange(String loggerName, Level level) {
        Logger logger = loggerContext.getLogger(loggerName);
        logger.setLevel(level);
        java.util.logging.Logger julLogger = JULHelper.asJULLogger(logger);
        java.util.logging.Level julLevel = JULHelper.asJULLevel(level);

        assertEquals(julLevel, julLogger.getLevel());
    }

    @Test
    public void smoke() {
        checkLevelChange("a", Level.INFO);
        checkLevelChange("a.b", Level.DEBUG);
    }

    @Test
    public void root() {
        checkLevelChange(Logger.ROOT_LOGGER_NAME, Level.TRACE);
    }

    // see http://jira.qos.ch/browse/LBCLASSIC-256
    @Test
    public void gc() {
        Logger logger = loggerContext.getLogger("gc" + rand);
        logger.setLevel(Level.INFO);
        // invoke GC so that the relevant julLogger can be garbage collected.
        System.gc();
        java.util.logging.Logger julLogger = JULHelper.asJULLogger(logger);
        java.util.logging.Level julLevel = JULHelper.asJULLevel(Level.INFO);

        assertEquals(julLevel, julLogger.getLevel());
    }

    @Test
    public void julHelperAsJulLevelRejectsNull() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Unexpected level [null]");
        JULHelper.asJULLevel(null);
    }

    @Test
    public void settingLevelToNullGetsParentLevel() {
        // first set level of "a" (the parent) to DEBUG
        Logger parent = loggerContext.getLogger("a");
        parent.setLevel(Level.DEBUG);

        // then set level of "a.b" (child logger of a) to null
        // for b to inherit its parent's level
        Logger child = loggerContext.getLogger("a.b");
        child.setLevel(Level.INFO);
        child.setLevel(null);

        assertEquals(parent.getEffectiveLevel(), child.getEffectiveLevel());
        assertEquals(Level.DEBUG, child.getEffectiveLevel());
    }

    // test that a message logged at a given JUL level
    // will still be loggable when the SLF4J logger is set to that level
    // to satisfy the Basic Selection Rule
    // https://logback.qos.ch/manual/architecture.html#basic_selection
    private void testLoggableAtJulLevel(ListAppender<ILoggingEvent> listAppender, java.util.logging.Level level) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger("LevelChangePropagatorTest.testLoggableAtJulLevel");
        java.util.logging.Logger julLogger = JULHelper.asJULLogger(logger);
        logger.setLevel(Level.ALL);
        assertEquals(0, listAppender.list.size());
        julLogger.log(level, "hi");
        assertEquals(
            "logger at ALL -> jul " + julLogger.getLevel() + " should pass message at " + level,
            1, listAppender.list.size()
        );
        ILoggingEvent msg = listAppender.list.get(0);
        listAppender.list.clear();
        Level observedLevel = msg.getLevel();
        logger.setLevel(observedLevel);
        assertTrue(
            "logger at " + observedLevel + " -> jul " + julLogger.getLevel() + " should pass message at " + level,
            julLogger.isLoggable(level)
        );
        julLogger.log(level, "hi");
        assertEquals(
            "logger at " + observedLevel + " -> jul " + julLogger.getLevel() + " should pass message at " + level,
            1, listAppender.list.size()
        );
        listAppender.list.clear();
    }
    @Test
    public void intermediateValues() {
        // we have to use the global LoggerContext and jul LogManager
        // since LogManager does not support being constructed by user code
        SLF4JBridgeHandler.install();
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        LevelChangePropagator levelChangePropagator = new LevelChangePropagator();
        levelChangePropagator.setContext(loggerContext);
        loggerContext.addListener(levelChangePropagator);

        ListAppender<ILoggingEvent> listAppender = new ListAppender<ILoggingEvent>();
        Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(listAppender);

        java.util.logging.Level SLIGHTLY_DEBUG = new java.util.logging.Level(
            "SLIGHTLY_DEBUG",
            java.util.logging.Level.FINEST.intValue() + 1) {
        };
        java.util.logging.Level SLIGHTLY_INFO = new java.util.logging.Level(
            "SLIGHTLY_INFO",
            java.util.logging.Level.FINE.intValue() + 1) {
        };
        java.util.logging.Level SLIGHTLY_WARN = new java.util.logging.Level(
            "SLIGHTLY_WARN",
            java.util.logging.Level.INFO.intValue() + 1) {
        };
        java.util.logging.Level SLIGHTLY_ERROR = new java.util.logging.Level(
            "SLIGHTLY_ERROR",
            java.util.logging.Level.WARNING.intValue() + 1) {
        };
        listAppender.start();
        testLoggableAtJulLevel(listAppender, java.util.logging.Level.SEVERE);
        testLoggableAtJulLevel(listAppender, SLIGHTLY_ERROR);
        testLoggableAtJulLevel(listAppender, java.util.logging.Level.WARNING);
        testLoggableAtJulLevel(listAppender, SLIGHTLY_WARN);
        testLoggableAtJulLevel(listAppender, java.util.logging.Level.INFO);
        testLoggableAtJulLevel(listAppender, java.util.logging.Level.CONFIG);
        testLoggableAtJulLevel(listAppender, SLIGHTLY_INFO);
        testLoggableAtJulLevel(listAppender, java.util.logging.Level.FINE);
        testLoggableAtJulLevel(listAppender, java.util.logging.Level.FINER);
        testLoggableAtJulLevel(listAppender, SLIGHTLY_DEBUG);
        testLoggableAtJulLevel(listAppender, java.util.logging.Level.FINEST);
        testLoggableAtJulLevel(listAppender, java.util.logging.Level.ALL);

        // test that all messages (including an message at ALL level)
        // are isLoggable for a logger at TRACE
        Logger logger = loggerContext.getLogger("LevelChangePropagatorTest.isLoggable");
        java.util.logging.Logger julLogger = JULHelper.asJULLogger(logger);
        logger.setLevel(Level.TRACE);
        assertTrue(julLogger.isLoggable(java.util.logging.Level.SEVERE));
        assertTrue(julLogger.isLoggable(SLIGHTLY_ERROR));
        assertTrue(julLogger.isLoggable(java.util.logging.Level.WARNING));
        assertTrue(julLogger.isLoggable(SLIGHTLY_WARN));
        assertTrue(julLogger.isLoggable(java.util.logging.Level.INFO));
        assertTrue(julLogger.isLoggable(java.util.logging.Level.CONFIG));
        assertTrue(julLogger.isLoggable(SLIGHTLY_INFO));
        assertTrue(julLogger.isLoggable(java.util.logging.Level.FINE));
        assertTrue(julLogger.isLoggable(java.util.logging.Level.FINER));
        assertTrue(julLogger.isLoggable(SLIGHTLY_DEBUG));
        assertTrue(julLogger.isLoggable(java.util.logging.Level.FINEST));
        assertTrue(julLogger.isLoggable(java.util.logging.Level.ALL));

        // test that FINER and custom debug-like messages
        // are isLoggable for a logger at DEBUG
        logger.setLevel(Level.DEBUG);
        assertTrue(julLogger.isLoggable(java.util.logging.Level.SEVERE));
        assertTrue(julLogger.isLoggable(SLIGHTLY_ERROR));
        assertTrue(julLogger.isLoggable(java.util.logging.Level.WARNING));
        assertTrue(julLogger.isLoggable(SLIGHTLY_WARN));
        assertTrue(julLogger.isLoggable(java.util.logging.Level.INFO));
        assertTrue(julLogger.isLoggable(java.util.logging.Level.CONFIG));
        assertTrue(julLogger.isLoggable(SLIGHTLY_INFO));
        assertTrue(julLogger.isLoggable(java.util.logging.Level.FINE));
        assertTrue(julLogger.isLoggable(java.util.logging.Level.FINER));
        assertTrue(julLogger.isLoggable(SLIGHTLY_DEBUG));
        assertFalse(julLogger.isLoggable(java.util.logging.Level.FINEST));
        assertFalse(julLogger.isLoggable(java.util.logging.Level.ALL));

        // test that CONFIG and custom info-like messages
        // are isLoggable for a logger at INFO
        logger.setLevel(Level.INFO);
        assertTrue(julLogger.isLoggable(java.util.logging.Level.SEVERE));
        assertTrue(julLogger.isLoggable(SLIGHTLY_ERROR));
        assertTrue(julLogger.isLoggable(java.util.logging.Level.WARNING));
        assertTrue(julLogger.isLoggable(SLIGHTLY_WARN));
        assertTrue(julLogger.isLoggable(java.util.logging.Level.INFO));
        assertTrue(julLogger.isLoggable(java.util.logging.Level.CONFIG));
        assertTrue(julLogger.isLoggable(SLIGHTLY_INFO));
        assertFalse(julLogger.isLoggable(java.util.logging.Level.FINE));
        assertFalse(julLogger.isLoggable(java.util.logging.Level.FINER));
        assertFalse(julLogger.isLoggable(SLIGHTLY_DEBUG));
        assertFalse(julLogger.isLoggable(java.util.logging.Level.FINEST));
        assertFalse(julLogger.isLoggable(java.util.logging.Level.ALL));

        // test that custom warn-like messages are isLoggable for a logger at WARN
        logger.setLevel(Level.WARN);
        assertTrue(julLogger.isLoggable(java.util.logging.Level.SEVERE));
        assertTrue(julLogger.isLoggable(SLIGHTLY_ERROR));
        assertTrue(julLogger.isLoggable(java.util.logging.Level.WARNING));
        assertTrue(julLogger.isLoggable(SLIGHTLY_WARN));
        assertFalse(julLogger.isLoggable(java.util.logging.Level.INFO));
        assertFalse(julLogger.isLoggable(java.util.logging.Level.CONFIG));
        assertFalse(julLogger.isLoggable(SLIGHTLY_INFO));
        assertFalse(julLogger.isLoggable(java.util.logging.Level.FINE));
        assertFalse(julLogger.isLoggable(java.util.logging.Level.FINER));
        assertFalse(julLogger.isLoggable(SLIGHTLY_DEBUG));
        assertFalse(julLogger.isLoggable(java.util.logging.Level.FINEST));
        assertFalse(julLogger.isLoggable(java.util.logging.Level.ALL));

        // test that custom error-like messages are isLoggable for a logger at ERROR
        logger.setLevel(Level.ERROR);
        assertTrue(julLogger.isLoggable(java.util.logging.Level.SEVERE));
        assertTrue(julLogger.isLoggable(SLIGHTLY_ERROR));
        assertFalse(julLogger.isLoggable(java.util.logging.Level.WARNING));
        assertFalse(julLogger.isLoggable(SLIGHTLY_WARN));
        assertFalse(julLogger.isLoggable(java.util.logging.Level.INFO));
        assertFalse(julLogger.isLoggable(java.util.logging.Level.CONFIG));
        assertFalse(julLogger.isLoggable(SLIGHTLY_INFO));
        assertFalse(julLogger.isLoggable(java.util.logging.Level.FINE));
        assertFalse(julLogger.isLoggable(java.util.logging.Level.FINER));
        assertFalse(julLogger.isLoggable(SLIGHTLY_DEBUG));
        assertFalse(julLogger.isLoggable(java.util.logging.Level.FINEST));
        assertFalse(julLogger.isLoggable(java.util.logging.Level.ALL));
    }
}
