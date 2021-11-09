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
package ch.qos.logback.classic.turbo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.testUtil.RandomUtil;

public class MDCFilterTest {

    int diff = RandomUtil.getPositiveInt();
    String key = "myKey" + diff;
    String value = "val" + diff;

    private MDCFilter filter;

    @Before
    public void setUp() {
        filter = new MDCFilter();
        filter.setOnMatch("ACCEPT");
        filter.setOnMismatch("DENY");
        filter.setMDCKey(key);
        filter.setValue(value);

        MDC.clear();
    }

    @After
    public void tearDown() {
        MDC.clear();
    }

    @Test
    public void smoke() {
        filter.start();
        MDC.put(key, "other" + diff);
        assertEquals(FilterReply.DENY, filter.decide(null, null, null, null, null, null));
        MDC.put(key, null);
        assertEquals(FilterReply.DENY, filter.decide(null, null, null, null, null, null));
        MDC.put(key, value);
        assertEquals(FilterReply.ACCEPT, filter.decide(null, null, null, null, null, null));
    }

    @Test
    public void testNoValueOption() {

        filter.setValue(null);
        filter.start();
        assertFalse(filter.isStarted());
        MDC.put(key, null);
        assertEquals(FilterReply.NEUTRAL, filter.decide(null, null, null, null, null, null));
        MDC.put(key, value);
        assertEquals(FilterReply.NEUTRAL, filter.decide(null, null, null, null, null, null));
    }

    @Test
    public void testNoMDCKeyOption() {
        filter.setMDCKey(null);
        filter.start();
        assertFalse(filter.isStarted());
        MDC.put(key, null);
        assertEquals(FilterReply.NEUTRAL, filter.decide(null, null, null, null, null, null));
        MDC.put(key, value);
        assertEquals(FilterReply.NEUTRAL, filter.decide(null, null, null, null, null, null));
    }

}
