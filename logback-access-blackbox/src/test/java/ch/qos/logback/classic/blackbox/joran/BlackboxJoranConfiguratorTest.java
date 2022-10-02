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

package ch.qos.logback.classic.blackbox.joran;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.blackbox.BlackboxClassicTestConstants;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.testUtil.StringListAppender;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlackboxJoranConfiguratorTest {
    LoggerContext loggerContext = new LoggerContext();
    Logger logger = loggerContext.getLogger(this.getClass().getName());
    Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
    //StatusChecker checker = new StatusChecker(loggerContext);
    int diff = RandomUtil.getPositiveInt();

    void configure(String file) throws JoranException {
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

}
