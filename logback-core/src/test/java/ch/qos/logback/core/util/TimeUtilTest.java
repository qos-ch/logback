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
package ch.qos.logback.core.util;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

public class TimeUtilTest {

    @Test
    public void testSecond() {
        // Mon Nov 20 18:05:17,522 CET 2006
        long now = 1164042317522L;
        // Mon Nov 20 18:05:18,000 CET 2006
        long expected = 1164042318000L;
        long computed = TimeUtil.computeStartOfNextSecond(now);
        assertEquals(expected - now, 478);
        assertEquals(expected, computed);
    }

    @Test
    public void testMinute() {
        // Mon Nov 20 18:05:17,522 CET 2006
        long now = 1164042317522L;
        // Mon Nov 20 18:06:00 CET 2006
        long expected = 1164042360000L;

        long computed = TimeUtil.computeStartOfNextMinute(now);
        assertEquals(expected - now, 1000 * 42 + 478);
        assertEquals(expected, computed);
    }

    @Test
    public void testHour() {
        // Mon Nov 20 18:05:17,522 GMT 2006
        long now = 1164045917522L;
        now = correctBasedOnTimeZone(now);
        // Mon Nov 20 19:00:00 GMT 2006
        long expected = 1164049200000L;
        expected = correctBasedOnTimeZone(expected);

        long computed = TimeUtil.computeStartOfNextHour(now);
        assertEquals(expected - now, 1000 * (42 + 60 * 54) + 478);
        assertEquals(expected, computed);
    }

    @Test
    public void testDay() {
        // Mon Nov 20 18:05:17 GMT 2006
        long now = 1164045917522L;
        now = correctBasedOnTimeZone(now);
        // Tue Nov 21 00:00:00 GMT 2006
        long expected = 1164067200000L;
        expected = correctBasedOnTimeZone(expected);
        long computed = TimeUtil.computeStartOfNextDay(now);

        assertEquals(expected - now, 1000 * (3600 * 5 + 60 * 54 + 42) + 478);
        assertEquals(expected, computed);
    }

    @Test
    public void testWeek() {
        // Mon Nov 20 18:05:17 GMT 2006
        long now = 1164045917522L;
        now = correctBasedOnTimeZone(now);
        // Sun Nov 26 00:00:00 GMT 2006
        long expected = 1164499200000L;
        expected = correctBasedOnTimeZone(expected);

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));

        int dayOffset = cal.getFirstDayOfWeek() - Calendar.SUNDAY;
        if (dayOffset != 0) {
            expected += 24L * 3600 * 1000 * (cal.getFirstDayOfWeek() - Calendar.SUNDAY);
        }

        long computed = TimeUtil.computeStartOfNextWeek(now);
        // System.out.println("now "+new Date(now));
        // System.out.println("computed "+new Date(computed));
        // System.out.println("expected "+new Date(expected));
        assertEquals(expected - now, 1000 * (3600 * (5 + 24 * (5 + dayOffset)) + 60 * 54 + 42) + 478);
        assertEquals(expected, computed);
    }

    @Test
    public void testMonth() {
        // Mon Nov 20 18:05:17 GMT 2006
        long now = 1164045917522L;
        now = correctBasedOnTimeZone(now);
        // Fri Dec 01 00:00:00 GMT 2006
        long expected = 1164931200000L;
        expected = correctBasedOnTimeZone(expected);

        long computed = TimeUtil.computeStartOfNextMonth(now);
        assertEquals(expected - now, 1000 * (3600 * (5 + 24 * 10) + 60 * 54 + 42) + 478);
        assertEquals(expected, computed);
    }

    private long correctBasedOnTimeZone(long gmtLong) {
        int offset = TimeZone.getDefault().getRawOffset();
        return gmtLong - offset;
    }

}
