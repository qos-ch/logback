/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

package ch.qos.logback.classic.issue.github876;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Github876Test {

    LoggerContext loggerContext = new LoggerContext();
    LogbackMDCAdapter logbackMDCAdapter = new LogbackMDCAdapter();
    ListAppender<ILoggingEvent> listAppender = new ListAppender<ILoggingEvent>();
    final Logger logger = loggerContext.getLogger(Github876Test.class);

    @BeforeEach
    public void setUp() {
        loggerContext.setMDCAdapter(logbackMDCAdapter);

        listAppender.setContext(loggerContext);
        listAppender.setName("list");
        listAppender.start();

        logger.addAppender(listAppender);

    }


    @Test
    public void traditionalTest() {
        Exception ex = new Exception("Some message");
        logger.error("Exception Message: {}", ex, ex);

        assertEquals(1, listAppender.list.size());
        ILoggingEvent iLoggingEvent0 = listAppender.list.get(0);

        String formattedMessage0 = iLoggingEvent0.getFormattedMessage();
        assertEquals("Exception Message: java.lang.Exception: Some message", formattedMessage0);
    }

    @Test
    public void fluentTest() {
        Exception ex = new Exception("Some message");
        logger.atError().addArgument(ex)
                        .setCause(ex).setMessage("Exception Message: {}")
                        .log();

        assertEquals(1, listAppender.list.size());
        ILoggingEvent iLoggingEvent0 = listAppender.list.get(0);

        String formattedMessage0 = iLoggingEvent0.getFormattedMessage();
        assertEquals("Exception Message: java.lang.Exception: Some message", formattedMessage0);
    }
}
