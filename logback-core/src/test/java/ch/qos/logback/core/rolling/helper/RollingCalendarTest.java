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

import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.util.EnvUtil;

public class RollingCalendarTest {

    String dailyPattern = "yyyy-MM-dd";

    @BeforeEach
    public void setUp() {

        // Most surprisingly, in certain environments (e.g. Windows 7), setting the
        // default locale
        // allows certain tests to pass which otherwise fail.
        //
        // These tests are:
        //
        // checkCollisionFreeness("yyyy-WW", false);
        // checkCollisionFreeness("yyyy-ww", true);
        // checkCollisionFreeness("ww", false);
        // {
        // RollingCalendar rc = new RollingCalendar("yyyy-ww");
        // assertEquals(PeriodicityType.TOP_OF_WEEK, rc.getPeriodicityType());
        // }
        //

        Locale oldLocale = Locale.getDefault();
        Locale.setDefault(oldLocale);
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testPeriodicity() {
        {
            RollingCalendar rc = new RollingCalendar("yyyy-MM-dd_HH_mm_ss");
            Assertions.assertEquals(PeriodicityType.TOP_OF_SECOND, rc.getPeriodicityType());
        }

        {
            RollingCalendar rc = new RollingCalendar("yyyy-MM-dd_HH_mm");
            Assertions.assertEquals(PeriodicityType.TOP_OF_MINUTE, rc.getPeriodicityType());
        }

        {
            RollingCalendar rc = new RollingCalendar("yyyy-MM-dd_HH");
            Assertions.assertEquals(PeriodicityType.TOP_OF_HOUR, rc.getPeriodicityType());
        }

        {
            RollingCalendar rc = new RollingCalendar("yyyy-MM-dd_hh");
            Assertions.assertEquals(PeriodicityType.TOP_OF_HOUR, rc.getPeriodicityType());
        }

        {
            RollingCalendar rc = new RollingCalendar("yyyy-MM-dd");
            Assertions.assertEquals(PeriodicityType.TOP_OF_DAY, rc.getPeriodicityType());
        }

        {
            RollingCalendar rc = new RollingCalendar("yyyy-MM");
            Assertions.assertEquals(PeriodicityType.TOP_OF_MONTH, rc.getPeriodicityType());
        }

        {
            RollingCalendar rc = new RollingCalendar("yyyy-ww");
            Assertions.assertEquals(PeriodicityType.TOP_OF_WEEK, rc.getPeriodicityType());
        }

        {
            RollingCalendar rc = new RollingCalendar("yyyy-W");
            Assertions.assertEquals(PeriodicityType.TOP_OF_WEEK, rc.getPeriodicityType());
        }
    }

    @Test
    public void testVaryingNumberOfHourlyPeriods() {
        RollingCalendar rc = new RollingCalendar("yyyy-MM-dd_HH");

        long MILLIS_IN_HOUR = 3600 * 1000;

        for (int p = 100; p > -100; p--) {
            long now = 1223325293589L; // Mon Oct 06 22:34:53 CEST 2008
            Instant result = rc.getEndOfNextNthPeriod(Instant.ofEpochMilli(now), p);
            long expected = now - (now % (MILLIS_IN_HOUR)) + p * MILLIS_IN_HOUR;
            Assertions.assertEquals(expected, result.toEpochMilli());
        }
    }

    @Test
    public void testVaryingNumberOfDailyPeriods() {
        RollingCalendar rc = new RollingCalendar("yyyy-MM-dd");
        final long MILLIS_IN_DAY = 24 * 3600 * 1000;

        for (int p = 20; p > -100; p--) {
            long now = 1223325293589L; // Mon Oct 06 22:34:53 CEST 2008
            Instant nowInstant = Instant.ofEpochMilli(now);
            Instant result = rc.getEndOfNextNthPeriod(nowInstant, p);
            long offset = rc.getTimeZone().getRawOffset() + rc.getTimeZone().getDSTSavings();

            long origin = now - ((now + offset) % (MILLIS_IN_DAY));
            long expected = origin + p * MILLIS_IN_DAY;
            Assertions.assertEquals(expected, result.toEpochMilli(), "p=" + p);
        }
    }

    // Wed Mar 23 23:07:05 CET 2016
    final long WED_2016_03_23_T_230705_CET = 1458770825333L;

    @Test
    public void testBarrierCrossingComputation() {
        checkPeriodBarriersCrossed("yyyy-MM-dd'T'HHmmss", WED_2016_03_23_T_230705_CET,
                WED_2016_03_23_T_230705_CET + 3 * CoreConstants.MILLIS_IN_ONE_SECOND, 3);
        checkPeriodBarriersCrossed("yyyy-MM-dd'T'HHmm", WED_2016_03_23_T_230705_CET,
                WED_2016_03_23_T_230705_CET + 3 * CoreConstants.MILLIS_IN_ONE_MINUTE, 3);
        checkPeriodBarriersCrossed("yyyy-MM-dd'T'HH", WED_2016_03_23_T_230705_CET,
                WED_2016_03_23_T_230705_CET + 3 * CoreConstants.MILLIS_IN_ONE_HOUR, 3);
        checkPeriodBarriersCrossed("yyyy-MM-dd", WED_2016_03_23_T_230705_CET,
                WED_2016_03_23_T_230705_CET + 3 * CoreConstants.MILLIS_IN_ONE_DAY, 3);
    }

    private void checkPeriodBarriersCrossed(String pattern, long start, long end, int count) {
        RollingCalendar rc = new RollingCalendar(pattern);
        Assertions.assertEquals(count, rc.periodBarriersCrossed(start, end));
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

        // 'u' is new to JDK 7
//        if (EnvUtil.isJDK7OrHigher()) {
//            checkCollisionFreeness("yyyy-MM-dd-uu", true);
//            checkCollisionFreeness("yyyy-MM-uu", false);
//        }

        // weekly
        checkCollisionFreeness("yyyy-MM-W", true);
        dumpCurrentLocale(Locale.getDefault());
        checkCollisionFreeness("yyyy-W", false);
        checkCollisionFreeness("yyyy-ww", true);
        checkCollisionFreeness("ww", false);
    }

    private void dumpCurrentLocale(Locale locale) {
        System.out.println("***Current default locale is " + locale);

    }

    private void checkCollisionFreeness(String pattern, boolean expected) {
        RollingCalendar rc = new RollingCalendar(pattern);
        if (expected) {
            Assertions.assertTrue(rc.isCollisionFree());
        } else {
            Assertions.assertFalse(rc.isCollisionFree());
        }
    }

    @Test
    public void basicPeriodBarriersCrossed() {
        RollingCalendar rc = new RollingCalendar(dailyPattern, TimeZone.getTimeZone("CET"), Locale.US);
        // Thu Jan 26 19:46:58 CET 2017, GMT offset = -1h
        long start = 1485456418969L;
        // Fri Jan 27 19:46:58 CET 2017, GMT offset = -1h
        long end = start + CoreConstants.MILLIS_IN_ONE_DAY;
        Assertions.assertEquals(1, rc.periodBarriersCrossed(start, end));
    }

    @Test
    public void testPeriodBarriersCrossedWhenGoingIntoDaylightSaving() {
        RollingCalendar rc = new RollingCalendar(dailyPattern, TimeZone.getTimeZone("CET"), Locale.US);
        // Sun Mar 26 00:02:03 CET 2017, GMT offset = -1h
        long start = 1490482923333L;
        // Mon Mar 27 00:02:03 CEST 2017, GMT offset = -2h
        long end = 1490565723333L;

        Assertions.assertEquals(1, rc.periodBarriersCrossed(start, end));
    }

    @Test
    public void testPeriodBarriersCrossedWhenLeavingDaylightSaving() {
        RollingCalendar rc = new RollingCalendar(dailyPattern, TimeZone.getTimeZone("CET"), Locale.US);
        // Sun Oct 29 00:02:03 CEST 2017, GMT offset = -2h
        long start = 1509228123333L;// 1490482923333L+217*CoreConstants.MILLIS_IN_ONE_DAY-CoreConstants.MILLIS_IN_ONE_HOUR;
        // Mon Oct 30 00:02:03 CET 2017, GMT offset = -1h
        long end = 1509228123333L + 25 * CoreConstants.MILLIS_IN_ONE_HOUR;
        Assertions.assertEquals(1, rc.periodBarriersCrossed(start, end));
    }

    @Test
    public void testPeriodBarriersCrossedJustBeforeEnteringDaylightSaving() {
        RollingCalendar rc = new RollingCalendar(dailyPattern, TimeZone.getTimeZone("CET"), Locale.US);
        // Sun Mar 26 22:18:38 CEST 2017, GMT offset = +2h
        long start = 1490559518333L;
        System.out.println(new Date(start));

        // Mon Mar 27 00:05:18 CEST 2017, GMT offset = +2h
        long end = 1490565918333L;
        System.out.println(new Date(end));
        Assertions.assertEquals(1, rc.periodBarriersCrossed(start, end));

    }
}