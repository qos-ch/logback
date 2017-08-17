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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.util.EnvUtil;

public class RollingCalendarTest {

   String dailyPattern = "yyyy-MM-dd";
   private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS zzz";

   @Before
   public void setUp() {
       
       // Most surprisingly, in certain environments (e.g. Windows 7), setting the default locale
       // allows certain tests to pass which otherwise fail.
       //
       // These tests are:
       //
       //  checkCollisionFreeness("yyyy-WW", false);
       //  checkCollisionFreeness("yyyy-ww", true);
       //  checkCollisionFreeness("ww", false);
       //  {
       //    RollingCalendar rc = new RollingCalendar("yyyy-ww");
       //    assertEquals(PeriodicityType.TOP_OF_WEEK, rc.getPeriodicityType());
       //  }
       // 
       
       Locale oldLocale = Locale.getDefault();
       Locale.setDefault(oldLocale);
   }

   @After
   public void tearDown() {
   }

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

        {
            RollingCalendar rc = new RollingCalendar("yyyy-ww");
            assertEquals(PeriodicityType.TOP_OF_WEEK, rc.getPeriodicityType());
        }

        {
            RollingCalendar rc = new RollingCalendar("yyyy-WW");
            assertEquals(PeriodicityType.TOP_OF_WEEK, rc.getPeriodicityType());
        }

        {
            RollingCalendar rc = new RollingCalendar("yyyy-MM-dd-a");
            assertEquals(PeriodicityType.HALF_DAY, rc.getPeriodicityType());
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
    public void testHalfDailyWithZeroPeriod() {
        testHalfDailyWithPeriod("2017-08-15T12:00:00.000 GMT", "2017-08-15T12:00:00.001 GMT", 0);
        testHalfDailyWithPeriod("2017-08-15T12:00:00.000 GMT", "2017-08-15T12:00:00.000 GMT", 0);
        testHalfDailyWithPeriod("2017-08-15T12:00:00.000 GMT", "2017-08-15T23:59:59.999 GMT", 0);
        testHalfDailyWithPeriod("2017-08-15T00:00:00.000 GMT", "2017-08-15T00:00:00.000 GMT", 0);
        testHalfDailyWithPeriod("2017-08-15T00:00:00.000 GMT", "2017-08-15T00:00:00.001 GMT", 0);
        testHalfDailyWithPeriod("2017-08-15T00:00:00.000 GMT", "2017-08-15T11:59:59.999 GMT", 0);
    }

    private void testHalfDailyWithPeriod(String expected, String baseDate, int period) {
        RollingCalendar rc = new RollingCalendar("yyyy-MM-dd-a");
        Date date = stringToDate(baseDate);
        Date result = rc.getEndOfNextNthPeriod(date, period);
        String message = String.format("When period is %d, %s should result in %s", period, baseDate, expected);
        Assert.assertEquals(message, expected, dateToString(result));
    }

    @Test
    public void testHalfDailyWithPositivePeriods() {
        String noon = "2017-08-15T12:00:00.000 GMT";
        String oneMiliBeforeNoon = "2017-08-15T11:59:59.999 GMT";
        String oneMiliBeforeMidnight = "2017-08-15T23:59:59.999 GMT";
        String midnight = "2017-08-15T00:00:00.000 GMT";

        testHalfDailyWithPeriod("2017-08-16T00:00:00.000 GMT", noon, 1);
        testHalfDailyWithPeriod("2017-08-15T12:00:00.000 GMT", oneMiliBeforeNoon, 1);
        testHalfDailyWithPeriod("2017-08-16T00:00:00.000 GMT", oneMiliBeforeMidnight, 1);
        testHalfDailyWithPeriod("2017-08-15T12:00:00.000 GMT", midnight, 1);

        testHalfDailyWithPeriod("2017-08-16T12:00:00.000 GMT", noon, 2);
        testHalfDailyWithPeriod("2017-08-16T00:00:00.000 GMT", oneMiliBeforeNoon, 2);
        testHalfDailyWithPeriod("2017-08-16T12:00:00.000 GMT", oneMiliBeforeMidnight, 2);
        testHalfDailyWithPeriod("2017-08-16T00:00:00.000 GMT", midnight, 2);

        testHalfDailyWithPeriod("2017-08-17T00:00:00.000 GMT", noon, 3);
        testHalfDailyWithPeriod("2017-08-16T12:00:00.000 GMT", oneMiliBeforeNoon, 3);
        testHalfDailyWithPeriod("2017-08-17T00:00:00.000 GMT", oneMiliBeforeMidnight, 3);
        testHalfDailyWithPeriod("2017-08-16T12:00:00.000 GMT", midnight, 3);
    }

    @Test
    public void testHalfDailyWithNegativePeriods() {
        testHalfDailyWithPeriod("2017-08-15T00:00:00.000 GMT", "2017-08-15T12:00:00.001 GMT", -1);
        testHalfDailyWithPeriod("2017-08-15T00:00:00.000 GMT", "2017-08-15T12:00:00.000 GMT", -1);
        testHalfDailyWithPeriod("2017-08-14T12:00:00.000 GMT", "2017-08-15T11:59:59.999 GMT", -1);

        testHalfDailyWithPeriod("2017-08-14T12:00:00.000 GMT", "2017-08-15T12:00:00.001 GMT", -2);
        testHalfDailyWithPeriod("2017-08-14T12:00:00.000 GMT", "2017-08-15T12:00:00.000 GMT", -2);
        testHalfDailyWithPeriod("2017-08-14T00:00:00.000 GMT", "2017-08-15T11:59:59.999 GMT", -2);

        testHalfDailyWithPeriod("2017-08-14T00:00:00.000 GMT", "2017-08-15T12:00:00.001 GMT", -3);
        testHalfDailyWithPeriod("2017-08-14T00:00:00.000 GMT", "2017-08-15T12:00:00.000 GMT", -3);
        testHalfDailyWithPeriod("2017-08-13T12:00:00.000 GMT", "2017-08-15T11:59:59.999 GMT", -3);
    }

    @Test
    public void testHalfDailyAtTheEdges() {
        testHalfDailyWithPeriod("2017-09-01T00:00:00.000 GMT","2017-08-31T12:00:00.000 GMT", 1 );
        testHalfDailyWithPeriod("2017-07-31T12:00:00.000 GMT","2017-08-01T00:00:00.000 GMT", -1 );

        testHalfDailyWithPeriod("2018-01-01T00:00:00.000 GMT", "2017-12-31T12:00:00.000 GMT", 1);
        testHalfDailyWithPeriod("2016-12-31T12:00:00.000 GMT", "2017-01-01T00:00:00.000 GMT", -1);
    }

    @Test
    public void testHalfDailyBarriersCrossed() {
        RollingCalendar rc = new RollingCalendar("yyyy-MM-dd-a");
        long barriersCrossed = rc.periodBarriersCrossed(stringToDate("2017-08-13T12:00:00.000 GMT").getTime(),
                stringToDate("2017-08-14T00:00:00.000 GMT").getTime());

        long barriersCrossed2 = rc.periodBarriersCrossed(stringToDate("2017-08-13T23:59:59.999 GMT").getTime(),
                stringToDate("2017-08-14T00:00:00.001 GMT").getTime());

        long barriersCrossed3 = rc.periodBarriersCrossed(stringToDate("2017-08-13T23:59:59.999 GMT").getTime(),
                stringToDate("2017-08-14T12:00:00.001 GMT").getTime());

        long barriersCrossed4 = rc.periodBarriersCrossed(stringToDate("2017-08-13T12:00:00.000 GMT").getTime(),
                stringToDate("2017-08-14T12:00:00.001 GMT").getTime());

        long barriersCrossed5 = rc.periodBarriersCrossed(stringToDate("2017-08-13T12:00:00.000 GMT").getTime(),
                stringToDate("2017-08-13T23:59:59.999 GMT").getTime());

        Assert.assertEquals(1, barriersCrossed);
        Assert.assertEquals(1, barriersCrossed2);
        Assert.assertEquals(2, barriersCrossed3);
        Assert.assertEquals(2, barriersCrossed4);
        Assert.assertEquals(0, barriersCrossed5);
    }

    private String dateToString(Date date) {
        final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        final TimeZone utc = TimeZone.getTimeZone("GMT");
        sdf.setTimeZone(utc);
        return sdf.format(date);
    }

    private Date stringToDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    // Wed Mar 23 23:07:05 CET 2016
    final long WED_2016_03_23_T_230705_CET = 1458770825333L;

    @Test
    public void testBarrierCrossingComputation() {
        checkPeriodBarriersCrossed("yyyy-MM-dd'T'HHmmss", WED_2016_03_23_T_230705_CET, WED_2016_03_23_T_230705_CET + 3*CoreConstants.MILLIS_IN_ONE_SECOND, 3);
        checkPeriodBarriersCrossed("yyyy-MM-dd'T'HHmm", WED_2016_03_23_T_230705_CET, WED_2016_03_23_T_230705_CET + 3*CoreConstants.MILLIS_IN_ONE_MINUTE, 3);
        checkPeriodBarriersCrossed("yyyy-MM-dd'T'HH", WED_2016_03_23_T_230705_CET, WED_2016_03_23_T_230705_CET + 3*CoreConstants.MILLIS_IN_ONE_HOUR, 3);
        checkPeriodBarriersCrossed("yyyy-MM-dd", WED_2016_03_23_T_230705_CET, WED_2016_03_23_T_230705_CET + 3*CoreConstants.MILLIS_IN_ONE_DAY, 3);
    }

    private void checkPeriodBarriersCrossed(String pattern, long start, long end, int count) {
        RollingCalendar rc = new RollingCalendar(pattern);
        assertEquals(count, rc.periodBarriersCrossed(start, end));
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
        if (EnvUtil.isJDK7OrHigher()) {
            checkCollisionFreeness("yyyy-MM-dd-uu", true);
            checkCollisionFreeness("yyyy-MM-uu", false);
        }

        // weekly
        checkCollisionFreeness("yyyy-MM-WW", true);
        dumpCurrentLocale(Locale.getDefault());
        checkCollisionFreeness("yyyy-WW", false);
        checkCollisionFreeness("yyyy-ww", true);
        checkCollisionFreeness("ww", false);
    }

    private void dumpCurrentLocale(Locale locale) {
       System.out.println("***Current default locale is "+locale);
        
    }

    private void checkCollisionFreeness(String pattern, boolean expected) {
        RollingCalendar rc = new RollingCalendar(pattern);
        if (expected) {
            assertTrue(rc.isCollisionFree());
        } else {
            assertFalse(rc.isCollisionFree());
        }
    }

    @Test
    public void basicPeriodBarriersCrossed() {
        RollingCalendar rc = new RollingCalendar(dailyPattern, TimeZone.getTimeZone("CET"), Locale.US);
        // Thu Jan 26 19:46:58 CET 2017, GMT offset = -1h
        long start = 1485456418969L;
        // Fri Jan 27 19:46:58 CET 2017,  GMT offset = -1h
        long end = start+CoreConstants.MILLIS_IN_ONE_DAY;
        assertEquals(1, rc.periodBarriersCrossed(start, end));
    }
    
    @Test
    public void testPeriodBarriersCrossedWhenGoingIntoDaylightSaving() {
        RollingCalendar rc = new RollingCalendar(dailyPattern, TimeZone.getTimeZone("CET"), Locale.US);
        // Sun Mar 26 00:02:03 CET  2017, GMT offset = -1h
        long start = 1490482923333L;
        // Mon Mar 27 00:02:03 CEST 2017,  GMT offset = -2h
        long end = 1490565723333L;
        
        assertEquals(1, rc.periodBarriersCrossed(start, end));
    }

    @Test
    public void testPeriodBarriersCrossedWhenLeavingDaylightSaving() {
        RollingCalendar rc = new RollingCalendar(dailyPattern, TimeZone.getTimeZone("CET"), Locale.US);
        // Sun Oct 29 00:02:03 CEST 2017, GMT offset = -2h
        long start = 1509228123333L;//1490482923333L+217*CoreConstants.MILLIS_IN_ONE_DAY-CoreConstants.MILLIS_IN_ONE_HOUR;
        // Mon Oct 30 00:02:03 CET  2017,  GMT offset = -1h
        long end = 1509228123333L+25*CoreConstants.MILLIS_IN_ONE_HOUR;
        assertEquals(1, rc.periodBarriersCrossed(start, end));
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
        assertEquals(1, rc.periodBarriersCrossed(start, end));
        
        
    }
}