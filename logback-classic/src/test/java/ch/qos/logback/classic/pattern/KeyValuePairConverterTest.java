/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2021, QOS.ch. All rights reserved.
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.event.KeyValuePair;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;

public class KeyValuePairConverterTest {
    LoggerContext lc;
    KeyValuePairConverter converter;
    LoggingEvent event;
    @Before
    public void setUp() throws Exception {
        lc = new LoggerContext();
        converter = new KeyValuePairConverter();
        converter.start();
        event = createLoggingEvent();
    }

    @After
    public void tearDown() throws Exception {
        lc = null;
        converter.stop();
        converter = null;
    }

    @Test
    public void testWithNullKVPList() {
    	//event.getKeyValuePairs().add(new KeyValuePair("k", "v"));
        String result = converter.convert(event);
        assertEquals("", result);
    }
    

    @Test
    public void testWithOnelKVP() {
    	event.addKeyValuePair(new KeyValuePair("k", "v"));
        String result = converter.convert(event);
        assertEquals("k=\"v\"", result);
    }
    
    private LoggingEvent createLoggingEvent() {
        LoggingEvent le = new LoggingEvent(this.getClass().getName(), lc.getLogger(Logger.ROOT_LOGGER_NAME), Level.DEBUG, "test message", null, null);
        return le;
    }
}
