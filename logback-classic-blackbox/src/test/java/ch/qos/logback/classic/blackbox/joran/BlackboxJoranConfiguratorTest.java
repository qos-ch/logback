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

package ch.qos.logback.classic.blackbox.joran;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.blackbox.BlackboxClassicTestConstants;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.jul.JULHelper;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.DefaultJoranConfigurator;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.testUtil.StringListAppender;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class BlackboxJoranConfiguratorTest {

    LoggerContext loggerContext = new LoggerContext();
    LogbackMDCAdapter logbackMDCAdapter = new LogbackMDCAdapter();
    Logger logger = loggerContext.getLogger(this.getClass().getName());
    Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
    StatusChecker checker = new StatusChecker(loggerContext);
    int diff = RandomUtil.getPositiveInt();

    void configure(String file) throws JoranException {
        loggerContext.setMDCAdapter(logbackMDCAdapter);
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        loggerContext.putProperty("diff", "" + diff);
        jc.doConfigure(file);
    }



    @Test
    public void eval() throws JoranException {
        configure(BlackboxClassicTestConstants.JORAN_INPUT_PREFIX + "callerData.xml");
        String msg = "hello world";
        logger.debug("toto");
        logger.debug(msg);

        StringListAppender<ILoggingEvent> slAppender = (StringListAppender<ILoggingEvent>) loggerContext
                .getLogger("root").getAppender("STR_LIST");
        assertNotNull(slAppender);
        assertEquals(2, slAppender.strList.size());
        assertTrue(slAppender.strList.get(0).contains(" DEBUG - toto"));

        String str1 = slAppender.strList.get(1);
        assertTrue(str1.contains("Caller+0"));
        assertTrue(str1.contains(" DEBUG - hello world"));
    }

    @Disabled
    @Test
    public void testEvaluatorFilter() throws JoranException {
        configure(BlackboxClassicTestConstants.JORAN_INPUT_PREFIX + "evaluatorFilter.xml");

        // StatusPrinter.print(loggerContext);

        logger.warn("hello");
        logger.error("to be ignored");

        ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) root.getAppender("LIST");

        assertNotNull(listAppender);
        assertEquals(1, listAppender.list.size());
        ILoggingEvent back = listAppender.list.get(0);
        assertEquals(Level.WARN, back.getLevel());
        assertEquals("hello", back.getMessage());
    }

    @Disabled
    @Test
    public void testEvaluatorFilterWithImports() throws JoranException {
        configure(BlackboxClassicTestConstants.JORAN_INPUT_PREFIX + "evaluatorFilterWithImports.xml");

        // StatusPrinter.print(loggerContext);

        logger.warn("hello");
        logger.error("to be ignored");

        ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) root.getAppender("LIST");

        assertNotNull(listAppender);
        assertEquals(1, listAppender.list.size());
        ILoggingEvent back = listAppender.list.get(0);
        assertEquals(Level.WARN, back.getLevel());
        assertEquals("hello", back.getMessage());
    }

    @Test
    public void conditional1673() throws JoranException  {
        loggerContext.putProperty("EXTRA", "true");
        String configFileAsStr = BlackboxClassicTestConstants.JORAN_INPUT_PREFIX + "issues/logback_1673.xml";
        configure(configFileAsStr);
    }

    @Test
    public void conditional1673bisWithActiveThen() throws JoranException  {
        loggerContext.putProperty("EXTRA", "true");
        String configFileAsStr = BlackboxClassicTestConstants.JORAN_INPUT_PREFIX + "issues/logback_1673bis.xml";
        configure(configFileAsStr);
        Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        ListAppender<ILoggingEvent> listThen = (ListAppender<ILoggingEvent>) root.getAppender("LIST_THEN");
        assertNotNull(listThen);

        ListAppender<ILoggingEvent> listElse = (ListAppender<ILoggingEvent>) root.getAppender("LIST_ELSE");
        assertNull(listElse);
    }

    @Test
    public void conditional1673bisWithActiveElse() throws JoranException  {
        String configFileAsStr = BlackboxClassicTestConstants.JORAN_INPUT_PREFIX + "issues/logback_1673bis.xml";
        configure(configFileAsStr);
        Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        ListAppender<ILoggingEvent> listThen = (ListAppender<ILoggingEvent>) root.getAppender("LIST_THEN");
        assertNull(listThen);

        ListAppender<ILoggingEvent> listElse = (ListAppender<ILoggingEvent>) root.getAppender("LIST_ELSE");
        assertNotNull(listElse);
    }

    @Test
    public void nestedIf() throws JoranException  {
        loggerContext.putProperty("EXTRA", "true");
        String configFileAsStr = BlackboxClassicTestConstants.JORAN_INPUT_PREFIX + "issues/logback_1678.xml";
        configure(configFileAsStr);
        StatusPrinter.print(loggerContext);

    }

    @Test
    public void levelChangePropagator0() throws JoranException, IOException, InterruptedException {
        String loggerName = "changePropagator0" + diff;
        java.util.logging.Logger.getLogger(loggerName).setLevel(java.util.logging.Level.INFO);
        String configFileAsStr = BlackboxClassicTestConstants.JORAN_INPUT_PREFIX + "/jul/levelChangePropagator0.xml";
        configure(configFileAsStr);

        checker.assertIsErrorFree();
        verifyJULLevel(loggerName, null);
        verifyJULLevel("a.b.c." + diff, Level.WARN);
        verifyJULLevel(Logger.ROOT_LOGGER_NAME, Level.TRACE);
    }

    @Test
    public void levelChangePropagator1() throws JoranException, IOException, InterruptedException {
        String loggerName = "changePropagator1" + diff;
        java.util.logging.Logger logger1 = java.util.logging.Logger.getLogger(loggerName);
        logger1.setLevel(java.util.logging.Level.INFO);
        verifyJULLevel(loggerName, Level.INFO);
        String configFileAsStr = BlackboxClassicTestConstants.JORAN_INPUT_PREFIX + "/jul/levelChangePropagator1.xml";
        configure(configFileAsStr);

        checker.assertIsErrorFree();
        verifyJULLevel(loggerName, Level.INFO); //
        verifyJULLevel("a.b.c." + diff, Level.WARN);
        verifyJULLevel(Logger.ROOT_LOGGER_NAME, Level.TRACE);
    }

    void verifyJULLevel(String loggerName, Level expectedLevel) {
        java.util.logging.Logger julLogger = JULHelper.asJULLogger(loggerName);
        java.util.logging.Level julLevel = julLogger.getLevel();

        if (expectedLevel == null) {
            assertNull(julLevel);
        } else {
            assertEquals(JULHelper.asJULLevel(expectedLevel), julLevel);
        }
    }

    // See https://github.com/qos-ch/logback/issues/1001
    // See https://github.com/qos-ch/logback/issues/997
    @Test
    public void fileAsResource() throws JoranException, IOException, InterruptedException {
        JoranConfigurator joranConfigurator = new JoranConfigurator();
        joranConfigurator.setContext(loggerContext);
        ClassLoader classLoader = Loader.getClassLoaderOfObject(joranConfigurator);
        String logbackConfigFile = "asResource/topFile.xml";
        URL aURL = Loader.getResource(logbackConfigFile, classLoader);
        InputStream inputStream = aURL.openStream();
        assertNotNull(inputStream);
        joranConfigurator.doConfigure(inputStream);
        StatusPrinter.print(loggerContext);
        checker.assertIsWarningOrErrorFree();
        fail();
    }

}
