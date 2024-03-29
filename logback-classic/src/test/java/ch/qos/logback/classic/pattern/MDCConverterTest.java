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
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.testUtil.RandomUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MDCConverterTest {

    LoggerContext loggerContext;
    LogbackMDCAdapter logbackMDCAdapter = new LogbackMDCAdapter();
    MDCConverter converter;
    int diff = RandomUtil.getPositiveInt();

    @BeforeEach
    public void setUp() throws Exception {
        loggerContext = new LoggerContext();
        loggerContext.setMDCAdapter(logbackMDCAdapter);
        converter = new MDCConverter();
        converter.start();
    }

    @AfterEach
    public void tearDown() throws Exception {
        loggerContext = null;
        converter.stop();
        converter = null;
    }

    @Test
    public void testConvertWithOneEntry() {
        String k = "MDCConverterTest_k" + diff;
        String v = "MDCConverterTest_v" + diff;

        logbackMDCAdapter.put(k, v);
        ILoggingEvent le = createLoggingEvent();
        String result = converter.convert(le);
        assertEquals(k + "=" + v, result);
    }

    @Test
    public void testConvertWithMultipleEntries() {
        logbackMDCAdapter.put("testKey", "testValue");
        logbackMDCAdapter.put("testKey2", "testValue2");
        ILoggingEvent le = createLoggingEvent();
        String result = converter.convert(le);
        boolean isConform = result.matches("testKey2?=testValue2?, testKey2?=testValue2?");
        assertTrue( isConform, result + " is not conform");
    }

    private ILoggingEvent createLoggingEvent() {
        return new LoggingEvent(this.getClass().getName(), loggerContext.getLogger(Logger.ROOT_LOGGER_NAME), Level.DEBUG,
                "test message", null, null);
    }
}
