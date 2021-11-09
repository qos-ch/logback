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
package ch.qos.logback.classic.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class LoggingEventTest {

    LoggerContext loggerContext = new LoggerContext();
    Logger logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

    @Before
    public void setUp() {
    }

    @Test
    public void testFormattingOneArg() {
        final String message = "x={}";
        final Throwable throwable = null;
        final Object[] argArray = { 12 };

        final LoggingEvent event = new LoggingEvent("", logger, Level.INFO, message, throwable, argArray);
        assertNull(event.formattedMessage);
        assertEquals("x=12", event.getFormattedMessage());
    }

    @Test
    public void testFormattingTwoArg() {
        final String message = "{}-{}";
        final Throwable throwable = null;
        final Object[] argArray = { 12, 13 };
        final LoggingEvent event = new LoggingEvent("", logger, Level.INFO, message, throwable, argArray);

        assertNull(event.formattedMessage);
        assertEquals("12-13", event.getFormattedMessage());
    }

    @Test
    public void testNoFormattingWithArgs() {
        final String message = "testNoFormatting";
        final Throwable throwable = null;
        final Object[] argArray = { 12, 13 };
        final LoggingEvent event = new LoggingEvent("", logger, Level.INFO, message, throwable, argArray);
        assertNull(event.formattedMessage);
        assertEquals(message, event.getFormattedMessage());
    }

    @Test
    public void testNoFormattingWithoutArgs() {
        final String message = "testNoFormatting";
        final Throwable throwable = null;
        final Object[] argArray = null;
        final LoggingEvent event = new LoggingEvent("", logger, Level.INFO, message, throwable, argArray);
        assertNull(event.formattedMessage);
        assertEquals(message, event.getFormattedMessage());
    }
}
