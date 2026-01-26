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

package ch.qos.logback.classic.joran.sanity;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.boolex.StubEventEvaluator;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.MDCAdapter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EvaluatorStubTest {
    LoggerContext loggerContext = new LoggerContext();
    JoranConfigurator jc = new JoranConfigurator();
    StatusPrinter2 statusPrinter2 = new StatusPrinter2();
    StatusChecker statusChecker = new StatusChecker(loggerContext);
    MDCAdapter mdcAdapter = new LogbackMDCAdapter();

    @BeforeEach
    void setUp() {
        loggerContext.setMDCAdapter(mdcAdapter);
    }

    @Test
    public void standaloneEventEvaluatorTest() throws JoranException {
        jc.setContext(loggerContext);
        jc.doConfigure(ClassicTestConstants.JORAN_INPUT_PREFIX + "simpleEvaluator.xml");
        statusChecker.assertContainsMatch(StubEventEvaluator.MSG_0);
        statusChecker.assertContainsMatch(StubEventEvaluator.MSG_1);
        statusChecker.assertContainsMatch(StubEventEvaluator.MSG_2);
        //statusPrinter2.print(loggerContext);
    }

    @Test
    public void eventEvaluatorEmbeddedInFilterTest() throws JoranException {
        jc.setContext(loggerContext);
        jc.doConfigure(ClassicTestConstants.JORAN_INPUT_PREFIX + "basicEventEvaluator.xml");
        statusChecker.assertContainsMatch(StubEventEvaluator.MSG_0);
        statusChecker.assertContainsMatch(StubEventEvaluator.MSG_1);
        statusChecker.assertContainsMatch(StubEventEvaluator.MSG_2);

        Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

        ListAppender listAppender = (ListAppender) root.getAppender("LIST");
        List<ILoggingEvent> eventList = listAppender.list;

        String message = "hello";
        Logger logger = loggerContext.getLogger(this.getClass());
        logger.warn(message);
        assertEquals(1, eventList.size());
        assertEquals(message, eventList.get(0).getMessage());
        statusPrinter2.print(loggerContext);
    }

}
