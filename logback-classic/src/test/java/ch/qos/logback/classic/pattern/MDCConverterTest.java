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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.testUtil.RandomUtil;

public class MDCConverterTest {

    LoggerContext lc;
    MDCConverter converter;
    int diff = RandomUtil.getPositiveInt();

    @Before
    public void setUp() throws Exception {
        lc = new LoggerContext();
        converter = new MDCConverter();
        converter.start();
        MDC.clear();
    }

    @After
    public void tearDown() throws Exception {
        lc = null;
        converter.stop();
        converter = null;
        MDC.clear();
    }

    @Test
    public void testConvertWithOneEntry() {
        String k = "MDCConverterTest_k" + diff;
        String v = "MDCConverterTest_v" + diff;

        MDC.put(k, v);
        ILoggingEvent le = createLoggingEvent();
        String result = converter.convert(le);
        assertEquals(k + "=" + v, result);
    }

    @Test
    public void testConvertWithMultipleEntries() {
        MDC.put("testKey", "testValue");
        MDC.put("testKey2", "testValue2");
        ILoggingEvent le = createLoggingEvent();
        String result = converter.convert(le);
        boolean isConform = result.matches("testKey2?=testValue2?, testKey2?=testValue2?");
        assertTrue(result + " is not conform", isConform);
    }

    private ILoggingEvent createLoggingEvent() {
        return new LoggingEvent(this.getClass().getName(), lc.getLogger(Logger.ROOT_LOGGER_NAME), Level.DEBUG, "test message", null, null);
    }
}
