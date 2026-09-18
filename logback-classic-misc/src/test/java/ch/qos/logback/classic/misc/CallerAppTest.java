/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.classic.misc;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test that the withCallerData() method on the Logger works as expected.
 *
 * This test does not need to be logback-classic-misc and could be moved to logback-classic module.
 *
 */
public class CallerAppTest {

    LoggerContext lc = new LoggerContext();
    Logger logger = lc.getLogger(CallerAppTest.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<ILoggingEvent>();

    @BeforeEach
    public void setUp() {
        listAppender.setContext(lc);
        listAppender.start();
        logger.addAppender(listAppender);
        logger.setAdditive(false);
    }

    @Test
    public void withCallerDataIncludesCallerData() {
        logger.atInfo().withCallerData().log("Hello with caller data");

        assertEquals(1, listAppender.list.size());
        ILoggingEvent event = listAppender.list.get(0);
        assertTrue(event.hasCallerData());
        StackTraceElement[] callerData = event.getCallerData();
        assertTrue(callerData.length > 0);
        assertEquals(CallerAppTest.class.getName(), callerData[0].getClassName());
        assertEquals("withCallerDataIncludesCallerData", callerData[0].getMethodName());
    }

    @Test
    public void withoutCallerDataDoesNotIncludeCallerData() {
        logger.atInfo().log("Hello with no caller data");

        assertEquals(1, listAppender.list.size());
        ILoggingEvent event = listAppender.list.get(0);
        assertFalse(event.hasCallerData());
    }
}
