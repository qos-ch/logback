/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.blackbox.joran.conditional;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.blackbox.BlackboxClassicTestConstants;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.status.StatusUtil;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConditionalTest {

    LoggerContext context = new LoggerContext();
    Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);

    StatusUtil checker = new StatusUtil(context);
    int diff = RandomUtil.getPositiveInt();
    String randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/";

    @BeforeEach
    public void setUp() throws UnknownHostException {
        context.setName("c" + diff);
        context.putProperty("randomOutputDir", randomOutputDir);
    }

    @AfterEach
    public void tearDown() {
        StatusPrinter.printIfErrorsOccured(context);
    }

    void configure(String file) throws JoranException {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(context);
        jc.doConfigure(file);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void conditionalConsoleApp_IF_THEN_True() throws JoranException, IOException, InterruptedException {
        InetAddress localhost = InetAddress.getLocalHost();
        System.out.println("In conditionalConsoleApp_IF_THEN_True, canonicalHostName=\""
                + localhost.getCanonicalHostName() + "] and hostNmae=\"" + localhost.getHostName() + "\"");
        context.putProperty("aHost", localhost.getHostName());

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
        StatusPrinter.print(context);

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

}
