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
package ch.qos.logback.classic.blackbox.joran.conditional;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.blackbox.BlackboxClassicTestConstants;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.sift.AppenderTracker;
import ch.qos.logback.core.status.StatusUtil;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.StatusPrinter;
import ch.qos.logback.core.util.StatusPrinter2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConditionalTest {

    LoggerContext loggerContext = new LoggerContext();
    LogbackMDCAdapter logbackMDCAdapter = new LogbackMDCAdapter();
    Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

    Logger logger = loggerContext.getLogger(this.getClass().getName());
    StatusPrinter2 statusPrinter2 = new StatusPrinter2();

    StatusUtil checker = new StatusUtil(loggerContext);
    int diff = RandomUtil.getPositiveInt();
    String randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/";

    @BeforeEach
    public void setUp() throws UnknownHostException {
        loggerContext.setMDCAdapter(logbackMDCAdapter);
        loggerContext.setName("c" + diff);
        loggerContext.putProperty("randomOutputDir", randomOutputDir);
    }

    @AfterEach
    public void tearDown() {
        StatusPrinter.printIfErrorsOccured(loggerContext);
    }

    void configure(String file) throws JoranException {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        jc.doConfigure(file);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void conditionalConsoleApp_IF_THEN_True() throws JoranException, IOException, InterruptedException {
        InetAddress localhost = InetAddress.getLocalHost();
        System.out.println("In conditionalConsoleApp_IF_THEN_True, canonicalHostName=\""
                + localhost.getCanonicalHostName() + "] and hostNmae=\"" + localhost.getHostName() + "\"");
        loggerContext.putProperty("aHost", localhost.getHostName());

        String configFileAsStr = BlackboxClassicTestConstants.JORAN_INPUT_PREFIX + "conditional/conditionalConsoleApp.xml";
        configure(configFileAsStr);
        FileAppender fileAppender = (FileAppender) root.getAppender("FILE");
        assertNotNull(fileAppender);

        ConsoleAppender consoleAppender = (ConsoleAppender) root.getAppender("CON");
        assertNotNull(consoleAppender);
        assertTrue(checker.isErrorFree(0));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void conditionalConsoleApp_IF_THEN_False() throws JoranException, IOException, InterruptedException {

        String configFileAsStr = BlackboxClassicTestConstants.JORAN_INPUT_PREFIX + "conditional/conditionalConsoleApp.xml";
        configure(configFileAsStr);
        FileAppender fileAppender = (FileAppender) root.getAppender("FILE");
        assertNotNull(fileAppender);

        ConsoleAppender consoleAppender = (ConsoleAppender) root.getAppender("CON");
        assertNull(consoleAppender);
        assertTrue(checker.isErrorFree(0));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void conditionalConsoleApp_IF_THEN_ELSE() throws JoranException, IOException, InterruptedException {

        String configFileAsStr = BlackboxClassicTestConstants.JORAN_INPUT_PREFIX + "conditional/conditionalConsoleApp_ELSE.xml";
        configure(configFileAsStr);

        FileAppender fileAppender = (FileAppender) root.getAppender("FILE");
        assertNotNull(fileAppender);

        ConsoleAppender consoleAppender = (ConsoleAppender) root.getAppender("CON");
        assertNull(consoleAppender);

        ListAppender listAppender = (ListAppender) root.getAppender("LIST");
        assertNotNull(listAppender);

        // StatusPrinter.printIfErrorsOccured(context);
        assertTrue(checker.isErrorFree(0));
    }

    @Test
    public void conditionalInclusionWithExistingFile() throws JoranException, IOException, InterruptedException {

        String configFileAsStr = BlackboxClassicTestConstants.JORAN_INPUT_PREFIX
                + "conditional/conditionalIncludeExistingFile.xml";
        configure(configFileAsStr);
        //statusPrinter2.print(loggerContext);

        ConsoleAppender<ILoggingEvent> consoleAppender = (ConsoleAppender<ILoggingEvent>) root.getAppender("CON");
        assertNotNull(consoleAppender);

        assertTrue(checker.isErrorFree(0));
    }

    @Test
    public void conditionalInclusionWithInexistentFile() throws JoranException, IOException, InterruptedException {

        String configFileAsStr = BlackboxClassicTestConstants.JORAN_INPUT_PREFIX
                + "conditional/conditionalIncludeInexistentFile.xml";
        configure(configFileAsStr);

        ConsoleAppender<ILoggingEvent> consoleAppender = (ConsoleAppender<ILoggingEvent>) root.getAppender("CON");
        assertNull(consoleAppender);
        assertTrue(checker.isErrorFree(0));
    }

    // https://jira.qos.ch/browse/LOGBACK-1732
    @Test
    public void conditionalInclusionWithVariableDefinition() throws JoranException, IOException, InterruptedException {

        String configFileAsStr = BlackboxClassicTestConstants.JORAN_INPUT_PREFIX
                + "conditional/includeWithVariableAndConditional.xml";
        configure(configFileAsStr);

        //statusPrinter2.print(loggerContext);

        ConsoleAppender<ILoggingEvent> consoleAppender = (ConsoleAppender<ILoggingEvent>) root.getAppender("CON");
        assertNotNull(consoleAppender);
        assertTrue(checker.isErrorFree(0));
    }


    // https://github.com/qos-ch/logback/issues/805
    @Test
    public void includedWithNestedConditional() throws JoranException {

        String configFileAsStr = BlackboxClassicTestConstants.JORAN_INPUT_PREFIX
                + "conditional/includeWithInnerConditional.xml";

        configure(configFileAsStr);
        //statusPrinter2.print(loggerContext);
        ConsoleAppender<ILoggingEvent> consoleAppender = (ConsoleAppender<ILoggingEvent>) root.getAppender("CON");
        assertNotNull(consoleAppender);
        assertTrue(checker.isErrorFree(0));
    }

    private AppenderTracker<ILoggingEvent> getAppenderTracker() {
        SiftingAppender ha = (SiftingAppender) root.getAppender("SIFT");
        return ha.getAppenderTracker();
    }

    // see also https://jira.qos.ch/browse/LOGBACK-1713
    @Test
    public void nestedWithinIfThen() throws JoranException {
        configure(BlackboxClassicTestConstants.JORAN_INPUT_PREFIX + "conditional/siftNestedWithinIfThen.xml");
        //statusPrinter2.print(loggerContext);
        String msg = "nestedWithinIfThen";
        logger.debug(msg);
        Appender<ILoggingEvent> appender = getAppenderTracker().find("ifThenDefault");
        assertNotNull(appender);
        ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) appender;
        List<ILoggingEvent> eventList = listAppender.list;
        assertEquals(1, listAppender.list.size());
        assertEquals(msg, eventList.get(0).getMessage());
        checker.isWarningOrErrorFree(0);
    }



}
