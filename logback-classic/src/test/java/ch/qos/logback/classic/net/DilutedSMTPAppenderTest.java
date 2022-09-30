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
package ch.qos.logback.classic.net;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.helpers.CyclicBuffer;
import ch.qos.logback.core.spi.CyclicBufferTracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DilutedSMTPAppenderTest {

    SMTPAppender appender;
    CyclicBufferTracker<ILoggingEvent> cbTracker;
    CyclicBuffer<ILoggingEvent> cb;

    @BeforeEach
    public void setUp() throws Exception {
        LoggerContext lc = new LoggerContext();
        appender = new SMTPAppender();
        appender.setContext(lc);
        appender.setName("smtp");
        appender.setFrom("user@host.dom");
        appender.setLayout(buildLayout(lc));
        appender.setSMTPHost("mail2.qos.ch");
        appender.setSubject("logging report");
        appender.addTo("sebastien.nospam@qos.ch");
        appender.start();
        cbTracker = appender.getCyclicBufferTracker();
        cb = cbTracker.getOrCreate("", 0);

    }

    private static Layout<ILoggingEvent> buildLayout(LoggerContext lc) {
        PatternLayout layout = new PatternLayout();
        layout.setContext(lc);
        layout.setFileHeader("Some header\n");
        layout.setPattern("%-4relative [%thread] %-5level %class - %msg%n");
        layout.setFileFooter("Some footer");
        layout.start();
        return layout;
    }

    @AfterEach
    public void tearDown() throws Exception {
        appender = null;
    }

    @Test
    public void testStart() {
        assertEquals("sebastien.nospam@qos.ch%nopex", appender.getToAsListOfString().get(0));

        assertEquals("logging report", appender.getSubject());

        assertTrue(appender.isStarted());
    }

    @Test
    public void testAppendNonTriggeringEvent() {
        LoggingEvent event = new LoggingEvent();
        event.setThreadName("thead name");
        event.setLevel(Level.DEBUG);
        appender.subAppend(cb, event);
        assertEquals(1, cb.length());
    }

    @Test
    public void testEntryConditionsCheck() {
        appender.checkEntryConditions();
        assertEquals(0, appender.getContext().getStatusManager().getCount());
    }

    @Test
    public void testTriggeringPolicy() {
        appender.setEvaluator(null);
        appender.checkEntryConditions();
        assertEquals(1, appender.getContext().getStatusManager().getCount());
    }

    @Test
    public void testEntryConditionsCheckNoLayout() {
        appender.setLayout(null);
        appender.checkEntryConditions();
        assertEquals(1, appender.getContext().getStatusManager().getCount());
    }

}
