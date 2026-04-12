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
package ch.qos.logback.classic.scoped;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScopedMDCConverterTest {

    LoggerContext loggerContext;
    ScopedMDCConverter converter;

    @BeforeEach
    public void setUp() {
        loggerContext = new LoggerContext();
        loggerContext.setMDCAdapter(new LogbackMDCAdapter());
        converter = new ScopedMDCConverter();
    }

    @AfterEach
    public void tearDown() {
        converter.stop();
        converter = null;
        loggerContext = null;
    }

    @Test
    public void convertWithNoScopedContext() {
        converter.start();
        ILoggingEvent event = createLoggingEvent();
        assertEquals("", converter.convert(event));
    }

    @Test
    public void convertWithSingleEntry() {
        converter.start();
        ScopedMDC.put("key1", "value1").run(() -> {
            ILoggingEvent event = createLoggingEvent();
            String result = converter.convert(event);
            assertEquals("key1=value1", result);
        });
    }

    @Test
    public void convertWithMultipleEntries() {
        converter.start();
        ScopedMDC.put("key1", "value1").put("key2", "value2").run(() -> {
            ILoggingEvent event = createLoggingEvent();
            String result = converter.convert(event);
            // Order is not guaranteed, so check both possible orderings
            boolean isConform = result.matches("key[12]=value[12], key[12]=value[12]");
            assertTrue(isConform, result + " is not conform");
        });
    }

    @Test
    public void convertWithSpecificKey() {
        setConverterOptions("key1");
        converter.start();
        ScopedMDC.put("key1", "value1").put("key2", "value2").run(() -> {
            ILoggingEvent event = createLoggingEvent();
            assertEquals("value1", converter.convert(event));
        });
    }

    @Test
    public void convertWithMissingKeyReturnsEmpty() {
        setConverterOptions("missing");
        converter.start();
        ScopedMDC.put("key1", "value1").run(() -> {
            ILoggingEvent event = createLoggingEvent();
            assertEquals("", converter.convert(event));
        });
    }

    @Test
    public void convertWithDefaultValue() {
        setConverterOptions("missing:-N/A");
        converter.start();
        ScopedMDC.put("key1", "value1").run(() -> {
            ILoggingEvent event = createLoggingEvent();
            assertEquals("N/A", converter.convert(event));
        });
    }

    @Test
    public void convertWithKeyPresentIgnoresDefault() {
        setConverterOptions("key1:-fallback");
        converter.start();
        ScopedMDC.put("key1", "value1").run(() -> {
            ILoggingEvent event = createLoggingEvent();
            assertEquals("value1", converter.convert(event));
        });
    }

    @Test
    public void convertWhenUnboundWithDefaultValue() {
        setConverterOptions("key1:-fallback");
        converter.start();
        ILoggingEvent event = createLoggingEvent();
        assertEquals("fallback", converter.convert(event));
    }

    private ILoggingEvent createLoggingEvent() {
        return new LoggingEvent(
                this.getClass().getName(),
                loggerContext.getLogger(Logger.ROOT_LOGGER_NAME),
                Level.DEBUG,
                "test message",
                null,
                null
        );
    }

    private void setConverterOptions(String option) {
        List<String> options = new ArrayList<>();
        options.add(option);
        converter.setOptionList(options);
    }
}
