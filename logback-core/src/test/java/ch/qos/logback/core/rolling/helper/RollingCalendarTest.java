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
package ch.qos.logback.core.rolling.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import java.util.Date;

import org.junit.Test;

public class RollingCalendarTest {

    @Test
    public void testPeriodicity() {
        {
            RollingCalendar rc = new RollingCalendar("yyyy-MM-dd_HH_mm_ss");
            assertEquals(PeriodicityType.TOP_OF_SECOND, rc.getPeriodicityType());
        }

        {
            RollingCalendar rc = new RollingCalendar("yyyy-MM-dd_HH_mm");
            assertEquals(PeriodicityType.TOP_OF_MINUTE, rc.getPeriodicityType());
        }

        {
            RollingCalendar rc = new RollingCalendar("yyyy-MM-dd_HH");
            assertEquals(PeriodicityType.TOP_OF_HOUR, rc.getPeriodicityType());
        }

        {
            RollingCalendar rc = new RollingCalendar("yyyy-MM-dd_hh");
            assertEquals(PeriodicityType.TOP_OF_HOUR, rc.getPeriodicityType());
        }

        {
            RollingCalendar rc = new RollingCalendar("yyyy-MM-dd");
            assertEquals(PeriodicityType.TOP_OF_DAY, rc.getPeriodicityType());
        }

        {
            RollingCalendar rc = new RollingCalendar("yyyy-MM");
            assertEquals(PeriodicityType.TOP_OF_MONTH, rc.getPeriodicityType());
        }
    }

    @Test
    public void testVaryingNumberOfHourlyPeriods() {
        RollingCalendar rc = new RollingCalendar("yyyy-MM-dd_HH");

        long MILLIS_IN_HOUR = 3600 * 1000;

        for (int p = 100; p > -100; p--) {
            long now = 1223325293589L; // Mon Oct 06 22:34:53 CEST 2008
            Date result = rc.getEndOfNextNthPeriod(new Date(now), p);
            long expected = now - (now % (MILLIS_IN_HOUR)) + p * MILLIS_IN_HOUR;
            assertEquals(expected, result.getTime());
        }
    }

    @Test
    public void testVaryingNumberOfDailyPeriods() {
        RollingCalendar rc = new RollingCalendar("yyyy-MM-dd");
        final long MILLIS_IN_DAY = 24 * 3600 * 1000;

        for (int p = 20; p > -100; p--) {
            long now = 1223325293589L; // Mon Oct 06 22:34:53 CEST 2008
            Date nowDate = new Date(now);
            Date result = rc.getEndOfNextNthPeriod(nowDate, p);
            long offset = rc.getTimeZone().getRawOffset() + rc.getTimeZone().getDSTSavings();

            long origin = now - ((now + offset) % (MILLIS_IN_DAY));
            long expected = origin + p * MILLIS_IN_DAY;
            assertEquals("p=" + p, expected, result.getTime());
        }
    }

    @Test
    public void testCollisionFreenes() {
        // hourly
        checkCollisionFreeness("yyyy-MM-dd hh", false);
        checkCollisionFreeness("yyyy-MM-dd hh a", true);

        checkCollisionFreeness("yyyy-MM-dd HH", true);
        checkCollisionFreeness("yyyy-MM-dd kk", true);
        
        checkCollisionFreeness("yyyy-MM-dd KK", false);
        checkCollisionFreeness("yyyy-MM-dd KK a", true);
        
        // daily
        checkCollisionFreeness("yyyy-MM-dd", true);
        checkCollisionFreeness("yyyy-dd", false);
        checkCollisionFreeness("dd", false);
        checkCollisionFreeness("MM-dd", false);

        checkCollisionFreeness("yyyy-DDD", true);
        checkCollisionFreeness("DDD", false);

        checkCollisionFreeness("yyyy-MM-dd-uu", true);
        checkCollisionFreeness("yyyy-MM-uu", false);
    
        // weekly
        checkCollisionFreeness("yyyy-MM-WW", true);
        checkCollisionFreeness("yyyy-WW", false);
        checkCollisionFreeness("yyyy-ww", true);
        checkCollisionFreeness("ww", false);
    }

    private void checkCollisionFreeness(String pattern, boolean expected) {
        RollingCalendar rc = new RollingCalendar(pattern);
        if (expected) {
            assertTrue(rc.isCollisionFree());
        } else {
            assertFalse(rc.isCollisionFree());
        }
    }
}