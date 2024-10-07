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

package ch.qos.logback.classic.joran;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.util.StatusPrinter2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertiesConfiguratorTest {

    LoggerContext lc = new LoggerContext();
    Properties props = new Properties();
    PropertiesConfigurator pc = new PropertiesConfigurator();
    StatusPrinter2 statusPrinter2 = new StatusPrinter2();
    @BeforeEach
    public void setup() throws Exception {
        pc.setContext(lc);
    }

    @Test
    public void smoke() {
        String TOTO_STR = "toto";
        props.setProperty(PropertiesConfigurator.LOGBACK_ROOT_LOGGER_PREFIX, Level.INFO.levelStr);
        props.setProperty(PropertiesConfigurator.LOGBACK_LOGGER_PREFIX + TOTO_STR, Level.ERROR.levelStr);
        pc.doConfigure(props);

        Logger rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
        Logger totoLogger = lc.getLogger(TOTO_STR);

        assertEquals(Level.INFO, rootLogger.getLevel());

        assertEquals(Level.ERROR, totoLogger.getLevel());

    }

    @Test
    public void withVariables() {
        String TOTO_STR = "toto";
        String ROOT_LEVEL_STR = "rootLevel";
        String TOTO_LEVEL_STR = "totoLevel";

        props.setProperty(ROOT_LEVEL_STR, Level.INFO.levelStr);
        System.setProperty("totoLevel", Level.ERROR.levelStr);
        props.setProperty(PropertiesConfigurator.LOGBACK_ROOT_LOGGER_PREFIX, asVar(ROOT_LEVEL_STR));
        props.setProperty(PropertiesConfigurator.LOGBACK_LOGGER_PREFIX + TOTO_STR, asVar(TOTO_LEVEL_STR));
        pc.doConfigure(props);

        Logger rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
        Logger totoLogger = lc.getLogger(TOTO_STR);
        statusPrinter2.print(lc);
        assertEquals(Level.INFO, rootLogger.getLevel());
        assertEquals(Level.ERROR, totoLogger.getLevel());

    }

    @Test
    void inheritedLevelString() {
        String loggerName0 = "com.abc.some0";
        String loggerName1 = "com.abc.some1";

        Logger aLogger0 = lc.getLogger(loggerName0);
        aLogger0.setLevel(Level.ERROR);

        Logger aLogger1 = lc.getLogger(loggerName1);
        aLogger1.setLevel(Level.WARN);


        props.setProperty(PropertiesConfigurator.LOGBACK_LOGGER_PREFIX + loggerName0, JoranConstants.INHERITED);
        props.setProperty(PropertiesConfigurator.LOGBACK_LOGGER_PREFIX + loggerName1, JoranConstants.NULL);
        pc.doConfigure(props);

        //statusPrinter2.print(lc);
        assertEquals(null, aLogger0.getLevel());
        assertEquals(null, aLogger1.getLevel());
    }

    String asVar(String v) {
        return "${"+v+"}";
    }
}