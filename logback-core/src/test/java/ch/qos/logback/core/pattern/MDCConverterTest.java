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
package ch.qos.logback.core.pattern;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ch.qos.logback.core.spi.MDCAware;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MDCConverterTest {
    MDCConverter converter;

    @Before
    public void setUp() throws Exception {
        converter = new MDCConverter();
        converter.start();
    }

    @After
    public void tearDown() throws Exception {
        converter.stop();
        converter = null;
    }

    @Test
    public void testConvertWithOneEntry() {
        String k = "MDCConverterTest_k";
        String v = "MDCConverterTest_v";

        Event event = new Event();
        event.putMDC(k, v);

        String result = converter.convert(event);
        assertEquals(k + "=" + v, result);
    }

    @Test
    public void testConvertWithMultipleEntries() {
        Event event = new Event();
        event.putMDC("testKey", "testValue");
        event.putMDC("testKey2", "testValue2");

        String result = converter.convert(event);
        boolean isConform = result.matches("testKey2?=testValue2?, testKey2?=testValue2?");
        assertTrue(result + " is not conform", isConform);
    }

    private static class Event implements MDCAware {

        private Map<String, String> mdc = new HashMap<String, String>();

        public void putMDC(String key, String value) {
            mdc.put(key, value);
        }

        @Override
        public Map<String, String> getMDCPropertyMap() {
            return mdc;
        }
    }
}
