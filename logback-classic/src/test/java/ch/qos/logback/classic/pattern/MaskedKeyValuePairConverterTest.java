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

package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.KeyValuePair;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MaskedKeyValuePairConverterTest {

    LoggerContext lc = new LoggerContext();
    MaskedKeyValuePairConverter converter;
    LoggingEvent event;

    StatusChecker statusChecker = new StatusChecker(lc);
    StatusPrinter2 statusPrinter2 = new StatusPrinter2();

    @BeforeEach
    public void setUp() throws Exception {
        converter = new MaskedKeyValuePairConverter();
        converter.setContext(lc);
    }

    @AfterEach
    public void tearDown() throws Exception {
        lc = null;
        converter.stop();
        converter = null;
    }

    @Test
    public void smoke() {
        event = createLoggingEvent();
        converter.setOptionList(List.of("k1"));
        converter.start();

        event.addKeyValuePair(new KeyValuePair("k1", "v1"));
        event.addKeyValuePair(new KeyValuePair("k2", "v2"));

        String result = converter.convert(event);
        assertEquals("k1=\""+MaskedKeyValuePairConverter.MASK+"\" k2=\"v2\"", result);
    }

    @Test
    public void smokeSingle() {
        event = createLoggingEvent();
        converter.setOptionList(List.of("SINGLE", "k1"));
        converter.start();

        event.addKeyValuePair(new KeyValuePair("k1", "v1"));
        event.addKeyValuePair(new KeyValuePair("k2", "v2"));

        String result = converter.convert(event);
        assertEquals("k1='"+MaskedKeyValuePairConverter.MASK+"' k2='v2'", result);
    }

    @Test
    public void wrongOrder() {
        event = createLoggingEvent();
        converter.setOptionList(List.of("k1", "SINGLE"));
        converter.start();

        event.addKeyValuePair(new KeyValuePair("k1", "v1"));
        event.addKeyValuePair(new KeyValuePair("k2", "v2"));

        statusPrinter2.print(lc);
        statusChecker.assertContainsMatch(Status.WARN, "extra quote spec SINGLE found in the wrong order");
        String result = converter.convert(event);
        assertEquals("k1=\""+MaskedKeyValuePairConverter.MASK+"\" k2=\"v2\"", result);
    }

    @Test
    public void testWithOnelKVP() {
        event = createLoggingEvent();
        converter.setOptionList(List.of("k"));
        converter.start();
        event.addKeyValuePair(new KeyValuePair("k", "v"));
        String result = converter.convert(event);
        assertEquals("k=\""+MaskedKeyValuePairConverter.MASK+"\"", result);
    }



    private LoggingEvent createLoggingEvent() {
        LoggingEvent le = new LoggingEvent(this.getClass().getName(), lc.getLogger(Logger.ROOT_LOGGER_NAME),
                Level.DEBUG, "test message", null, null);
        return le;
    }

}
