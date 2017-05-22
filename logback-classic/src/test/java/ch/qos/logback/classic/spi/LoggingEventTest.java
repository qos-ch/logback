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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

public class LoggingEventTest {

    LoggerContext loggerContext = new LoggerContext();
    Logger logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

    @Before
    public void setUp() {
    }

    @Test
    public void testFormattingOneArg() {
        String message = "x={}";
        Throwable throwable = null;
        Object[] argArray = new Object[] { 12 };

        LoggingEvent event = new LoggingEvent("", logger, Level.INFO, message, throwable, argArray);
        assertNull(event.formattedMessage);
        assertEquals("x=12", event.getFormattedMessage());
    }

    @Test
    public void testFormattingTwoArg() {
        String message = "{}-{}";
        Throwable throwable = null;
        Object[] argArray = new Object[] { 12, 13 };
        LoggingEvent event = new LoggingEvent("", logger, Level.INFO, message, throwable, argArray);

        assertNull(event.formattedMessage);
        assertEquals("12-13", event.getFormattedMessage());
    }

    @Test
    public void testNoFormattingWithArgs() {
        String message = "testNoFormatting";
        Throwable throwable = null;
        Object[] argArray = new Object[] { 12, 13 };
        LoggingEvent event = new LoggingEvent("", logger, Level.INFO, message, throwable, argArray);
        assertNull(event.formattedMessage);
        assertEquals(message, event.getFormattedMessage());
    }

    @Test
    public void testNoFormattingWithoutArgs() {
        String message = "testNoFormatting";
        Throwable throwable = null;
        Object[] argArray = null;
        LoggingEvent event = new LoggingEvent("", logger, Level.INFO, message, throwable, argArray);
        assertNull(event.formattedMessage);
        assertEquals(message, event.getFormattedMessage());
    }

    @Test
    public void testFormattingWithThrowableNotALastArgument() {
        String message = "{}-{}";
        Throwable throwable = null;
        Throwable meaningfulException = new MeaningfulException("description of exception");
        Object[] argArray = new Object[] { meaningfulException, 42, meaningfulException };
        LoggingEvent event = new LoggingEvent("", logger, Level.INFO, message, throwable, argArray);
        assertNull(event.formattedMessage);
        assertEquals("description of exception-42", event.getFormattedMessage() );
    }

    @Test
    // Seems strange while I've faced this use case in real codebase
    public void testFormattingWithThrowableAsLastArgument() {
        String message = "{}-{}";
        Throwable throwable = null;
        Throwable meaningfulException = new MeaningfulException("description of exception");
        Object[] argArray = new Object[] { 42, meaningfulException, meaningfulException };
        LoggingEvent event = new LoggingEvent("", logger, Level.INFO, message, throwable, argArray);
        assertNull(event.formattedMessage);
        assertEquals("42-description of exception", event.getFormattedMessage() );
    }

    // Sample class with some more-or-less meaningful toString
    private static final class MeaningfulException extends RuntimeException {
        private MeaningfulException(String message) {
            super(message);
        }

        @Override
        public String toString() {
            return getLocalizedMessage();
        }
    }
}
