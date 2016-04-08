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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.MDC;

import ch.qos.logback.core.spi.FilterReply;

public class MDCFilterTest {

    private static String KEY = "myKey";

    private MDCFilter filter;

    @Before
    public void init() {
        filter = new MDCFilter();
        filter.setOnMatch("ACCEPT");
        filter.setOnMismatch("DENY");
        filter.setMDCKey(KEY);
        filter.start();
        MDC.clear();
    }

    @Test
    public void testNoValue() {
        filter.setMDCKey(null);
        MDC.remove(KEY);
        assertEquals(FilterReply.NEUTRAL, filter.decide(null, null, null, null, null, null));
        MDC.put(KEY, "key");
        assertEquals(FilterReply.NEUTRAL, filter.decide(null, null, null, null, null, null));
    }

    @Test
    public void testNoMdcValue() {
        MDC.remove(KEY);
        assertEquals(FilterReply.ACCEPT, filter.decide(null, null, null, null, null, null));
    }

    @Test
    public void testMdcValue() {
        MDC.put(KEY, "myKey");
        assertEquals(FilterReply.DENY, filter.decide(null, null, null, null, null, null));
    }

    @Test
    public void testMdcAndValue() {
        filter.setValue("correct");
        MDC.put(KEY, "wrong");
        assertEquals(FilterReply.DENY, filter.decide(null, null, null, null, null, null));
        MDC.put(KEY, "correct");
        assertEquals(FilterReply.ACCEPT, filter.decide(null, null, null, null, null, null));
    }
}

