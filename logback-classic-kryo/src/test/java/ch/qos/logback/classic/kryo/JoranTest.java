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
package ch.qos.logback.classic.kryo;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.StatusPrinter2;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.spi.MDCAdapter;

import static ch.qos.logback.core.kryo.KryoCoreConstants.KRYO_UNSAFE_PROPERTY_KEY;

public class JoranTest {

    LoggerContext loggerContext = new LoggerContext();
    MDCAdapter mdcAdapter = new LogbackMDCAdapter();
    Logger logger = loggerContext.getLogger(this.getClass().getName());
    Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
    StatusPrinter2 statusPrinter2 = new StatusPrinter2();
    StatusChecker checker = new StatusChecker(loggerContext);
    int diff = RandomUtil.getPositiveInt();

    @BeforeAll
    public static void beforeAll() {
        System.setProperty(KRYO_UNSAFE_PROPERTY_KEY, "false");
    }

    @AfterAll
    public static void afterAll() {
        System.clearProperty(KRYO_UNSAFE_PROPERTY_KEY);
    }


    @BeforeEach
    public void setUp() {
        loggerContext.putProperty("destdir", KryoClassicStreamTestConstants.OUTPUT_DIR_PREFIX + "JoranTest/"+diff);
        loggerContext.setName("JoranTest"+diff);
    }

    void configure(String file) throws JoranException {
        loggerContext.setMDCAdapter(mdcAdapter);
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        loggerContext.putProperty("diff", "" + diff);
        jc.doConfigure(file);
    }

    @Test
    public void smoke() throws JoranException {
        configure(KryoClassicStreamTestConstants.JORAN_INPUT_PREFIX + "smoke.xml");
        Logger logger = loggerContext.getLogger(this.getClass().getName());
        Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        String msg = "hello world";
        logger.debug(msg);
        logger.debug(msg+1);
        logger.error(msg, new Exception("Exception message"));
        checker.assertIsWarningOrErrorFree();
    }

    @Test
    public void vanilla() throws JoranException {
        configure(KryoClassicStreamTestConstants.JORAN_INPUT_PREFIX + "vanilla.xml");
        Logger logger = loggerContext.getLogger(this.getClass().getName());
        Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        String msg = "hello world";
        logger.debug(msg);
        logger.debug(msg+1);
        logger.error(msg, new Exception("Exception message"));
        checker.assertIsWarningOrErrorFree();
    }
}
