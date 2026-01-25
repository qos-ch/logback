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

package ch.qos.logback.core.util;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;


class SimpleTimeBasedGuardTest {
    //  Nov 24 20:10:52 UTC 2025
    long UTC_2025_11_24H201052 = 1764015052000L;


    private SimpleTimeBasedGuard guard;

    @BeforeEach
    void setUp() {
        guard = new SimpleTimeBasedGuard();
    }

    @AfterEach
    void tearDown() {
        guard.clearCurrentTime();
    }

    @Test
    void allowsTwoPer30Minutes() {
        guard.setCurrentTimeMillis(UTC_2025_11_24H201052);

        assertTrue(guard.allow());
        assertTrue(guard.allow());
        assertFalse(guard.allow());
        assertFalse(guard.allow());

        assertEquals(0, guard.getAllowsRemaining());
    }

    @Test
    void resetsCompletelyAfter30Minutes() {
        guard.setCurrentTimeMillis(UTC_2025_11_24H201052);
        guard.allow();
        guard.allow(); // use both

        // Move exactly 30 minutes forward
        guard.incCurrentTimeMillis(30 * 60 * 1000);

        assertTrue(guard.allow());  // new window!
        assertTrue(guard.allow());
        assertFalse(guard.allow());
    }

    @Test
    void windowsAreFloorAlignedTo30MinuteBoundaries() {
        guard.setCurrentTimeMillis(10_000); // t=10s
        guard.allow();
        guard.allow();

        guard.incCurrentTimeMillis(29 * 60 * 1000L + 48_000L); // still same window
        assertFalse(guard.allow());

        guard.setCurrentTimeMillis(30 * 60 * 1000L+10_001); // exactly 30 min → new window
        assertTrue(guard.allow());
    }

    @Test
    void worksWithRealTimeWhenNotInjected() throws Exception {
        guard.clearCurrentTime();

        assertTrue(guard.allow());
        assertTrue(guard.allow());
        assertFalse(guard.allow());

        // Fast-forward real time by 30+ minutes (for CI, use fake time instead)
        // In real app: just wait 30 min
    }

    @Test
    void configurableLimitsWork() {
        int fiveMinutesMs = 5 * 60_000;
        SimpleTimeBasedGuard guard = new SimpleTimeBasedGuard(fiveMinutesMs, 3); // 3 allows every 5 minutes

        guard.setCurrentTimeMillis(10);
        assertTrue(guard.allow());
        assertTrue(guard.allow());
        assertTrue(guard.allow());
        assertFalse(guard.allow());

        guard.setCurrentTimeMillis(fiveMinutesMs + 10); // exactly 5 minutes later
        assertTrue(guard.allow()); // fresh window!
    }
}
