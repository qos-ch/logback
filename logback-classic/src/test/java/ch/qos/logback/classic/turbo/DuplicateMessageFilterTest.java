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

import org.junit.Test;

import ch.qos.logback.core.spi.FilterReply;

public class DuplicateMessageFilterTest {

    @Test
    public void smoke() {
        final DuplicateMessageFilter dmf = new DuplicateMessageFilter();
        dmf.setAllowedRepetitions(0);
        dmf.start();
        assertEquals(FilterReply.NEUTRAL, dmf.decide(null, null, null, "x", null, null));
        assertEquals(FilterReply.NEUTRAL, dmf.decide(null, null, null, "y", null, null));
        assertEquals(FilterReply.DENY, dmf.decide(null, null, null, "x", null, null));
        assertEquals(FilterReply.DENY, dmf.decide(null, null, null, "y", null, null));
    }

    @Test
    public void memoryLoss() {
        final DuplicateMessageFilter dmf = new DuplicateMessageFilter();
        dmf.setAllowedRepetitions(1);
        dmf.setCacheSize(1);
        dmf.start();
        assertEquals(FilterReply.NEUTRAL, dmf.decide(null, null, null, "a", null, null));
        assertEquals(FilterReply.NEUTRAL, dmf.decide(null, null, null, "b", null, null));
        assertEquals(FilterReply.NEUTRAL, dmf.decide(null, null, null, "a", null, null));
    }

    @Test
    public void many() {
        final DuplicateMessageFilter dmf = new DuplicateMessageFilter();
        dmf.setAllowedRepetitions(0);
        final int cacheSize = 10;
        final int margin = 2;
        dmf.setCacheSize(cacheSize);
        dmf.start();
        for (int i = 0; i < cacheSize + margin; i++) {
            assertEquals(FilterReply.NEUTRAL, dmf.decide(null, null, null, "a" + i, null, null));
        }
        for (int i = cacheSize - 1; i >= margin; i--) {
            assertEquals(FilterReply.DENY, dmf.decide(null, null, null, "a" + i, null, null));
        }
        for (int i = margin - 1; i >= 0; i--) {
            assertEquals(FilterReply.NEUTRAL, dmf.decide(null, null, null, "a" + i, null, null));
        }
    }

    @Test
    // isXXXEnabled invokes decide with a null format
    // http://jira.qos.ch/browse/LBCLASSIC-134
    public void nullFormat() {
        final DuplicateMessageFilter dmf = new DuplicateMessageFilter();
        dmf.setAllowedRepetitions(0);
        dmf.setCacheSize(10);
        dmf.start();
        assertEquals(FilterReply.NEUTRAL, dmf.decide(null, null, null, null, null, null));
        assertEquals(FilterReply.NEUTRAL, dmf.decide(null, null, null, null, null, null));
    }

}
