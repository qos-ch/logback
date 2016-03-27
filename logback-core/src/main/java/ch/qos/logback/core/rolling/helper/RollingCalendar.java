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

import static ch.qos.logback.core.CoreConstants.MILLIS_IN_ONE_HOUR;
import static ch.qos.logback.core.CoreConstants.MILLIS_IN_ONE_DAY;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * RollingCalendar is a helper class to
 * {@link ch.qos.logback.core.rolling.TimeBasedRollingPolicy } or similar
 * timed-based rolling policies. Given a periodicity type and the current time,
 * it computes the start of the next interval (i.e. the triggering date).
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class RollingCalendar extends GregorianCalendar {

    private static final long serialVersionUID = -5937537740925066161L;

    // The gmtTimeZone is used only in computeCheckPeriod() method.
    static final TimeZone GMT_TIMEZONE = TimeZone.getTimeZone("GMT");

    PeriodicityType periodicityType = PeriodicityType.ERRONEOUS;
    String datePattern;

    public RollingCalendar(String datePattern) {
        super();
        this.datePattern = datePattern;
        this.periodicityType = computePeriodicityType();
    }

    public RollingCalendar(String datePattern, TimeZone tz, Locale locale) {
        super(tz, locale);
        this.datePattern = datePattern;
        this.periodicityType = computePeriodicityType();
    }

    public PeriodicityType getPeriodicityType() {
        return periodicityType;
    }

    // This method computes the roll over period by looping over the
    // periods, starting with the shortest, and stopping when the r0 is
    // different from from r1, where r0 is the epoch formatted according
    // the datePattern (supplied by the user) and r1 is the
    // epoch+nextMillis(i) formatted according to datePattern. All date
    // formatting is done in GMT and not local format because the test
    // logic is based on comparisons relative to 1970-01-01 00:00:00
    // GMT (the epoch).
    public PeriodicityType computePeriodicityType() {

        GregorianCalendar calendar = new GregorianCalendar(GMT_TIMEZONE, Locale.getDefault());
                        

        // set sate to 1970-01-01 00:00:00 GMT
        Date epoch = new Date(0);

        if (datePattern != null) {
            for (PeriodicityType i : PeriodicityType.VALID_ORDERED_LIST) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
                simpleDateFormat.setTimeZone(GMT_TIMEZONE); // all date formatting done in GMT

                String r0 = simpleDateFormat.format(epoch);

                Date next = innerGetEndOfThisPeriod(calendar, i, epoch);
                String r1 = simpleDateFormat.format(next);

                // System.out.println("Type = "+i+", r0 = "+r0+", r1 = "+r1);
                if ((r0 != null) && (r1 != null) && !r0.equals(r1)) {
                    return i;
                }
            }
        }
        // we failed
        return PeriodicityType.ERRONEOUS;
    }

    public boolean isCollisionFree() {
        switch (periodicityType) {
        case TOP_OF_HOUR:
            // isolated hh or KK
            return !collision(12 * MILLIS_IN_ONE_HOUR);

        case TOP_OF_DAY:
            // EE or uu
            if (collision(7 * MILLIS_IN_ONE_DAY))
                return false;
            // isolated dd
            if (collision(31 * MILLIS_IN_ONE_DAY))
                return false;
            // DD
            if (collision(365 * MILLIS_IN_ONE_DAY))
                return false;
            return true;
        case TOP_OF_WEEK:
            // WW
            if (collision(34 * MILLIS_IN_ONE_DAY))
                return false;
            // isolated ww
            if (collision(366 * MILLIS_IN_ONE_DAY))
                return false;
            return true;
        default:
            return true;
        }
    }

    private boolean collision(long delta) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
        simpleDateFormat.setTimeZone(GMT_TIMEZONE); // all date formatting done in GMT
        Date epoch0 = new Date(0);
        String r0 = simpleDateFormat.format(epoch0);
        Date epoch12 = new Date(delta);
        String r12 = simpleDateFormat.format(epoch12);

        return r0.equals(r12);
    }

    public void printPeriodicity(ContextAwareBase cab) {
        switch (periodicityType) {
        case TOP_OF_MILLISECOND:
            cab.addInfo("Roll-over every millisecond.");
            break;

        case TOP_OF_SECOND:
            cab.addInfo("Roll-over every second.");
            break;

        case TOP_OF_MINUTE:
            cab.addInfo("Roll-over every minute.");
            break;

        case TOP_OF_HOUR:
            cab.addInfo("Roll-over at the top of every hour.");
            break;

        case HALF_DAY:
            cab.addInfo("Roll-over at midday and midnight.");
            break;

        case TOP_OF_DAY:
            cab.addInfo("Roll-over at midnight.");
            break;

        case TOP_OF_WEEK:
            cab.addInfo("Rollover at the start of week.");
            break;

        case TOP_OF_MONTH:
            cab.addInfo("Rollover at start of every month.");
            break;

        default:
            cab.addInfo("Unknown periodicity.");
        }
    }

    public long periodBarriersCrossed(long start, long end) {
        if (start > end)
            throw new IllegalArgumentException("Start cannot come before end");

        Date startFloored = getsStartOfCurrentPeriod(start);
        Date endFloored = getsStartOfCurrentPeriod(end);
        
        long diff = endFloored.getTime() - startFloored.getTime();
        
        switch (periodicityType) {

        case TOP_OF_MILLISECOND:
            return diff;
        case TOP_OF_SECOND:
            return diff / CoreConstants.MILLIS_IN_ONE_SECOND;
        case TOP_OF_MINUTE:
            return diff / CoreConstants.MILLIS_IN_ONE_MINUTE;
        case TOP_OF_HOUR:
            return (int) diff / CoreConstants.MILLIS_IN_ONE_HOUR;
        case TOP_OF_DAY:
            return diff / CoreConstants.MILLIS_IN_ONE_DAY;
        case TOP_OF_WEEK:
            return diff / CoreConstants.MILLIS_IN_ONE_WEEK;
        case TOP_OF_MONTH:
            return diffInMonths(start, end);
        default:
            throw new IllegalStateException("Unknown periodicity type.");
        }
    }

    public static int diffInMonths(long startTime, long endTime) {
        if (startTime > endTime)
            throw new IllegalArgumentException("startTime cannot be larger than endTime");
        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(startTime);
        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(endTime);
        int yearDiff = endCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR);
        int monthDiff = endCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH);
        return yearDiff * 12 + monthDiff;
    }

    static private Date innerGetEndOfThisPeriod(Calendar cal, PeriodicityType periodicityType, Date now) {
        return innerGetEndOfNextNthPeriod(cal, periodicityType, now, 1);
    }

    static private Date innerGetEndOfNextNthPeriod(Calendar cal, PeriodicityType periodicityType, Date now, int numPeriods) {
        cal.setTime(now);
        switch (periodicityType) {
        case TOP_OF_MILLISECOND:
            cal.add(Calendar.MILLISECOND, numPeriods);
            break;

        case TOP_OF_SECOND:
            cal.set(Calendar.MILLISECOND, 0);
            cal.add(Calendar.SECOND, numPeriods);
            break;

        case TOP_OF_MINUTE:
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.add(Calendar.MINUTE, numPeriods);
            break;

        case TOP_OF_HOUR:
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.add(Calendar.HOUR_OF_DAY, numPeriods);
            break;

        case TOP_OF_DAY:
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.add(Calendar.DATE, numPeriods);
            break;

        case TOP_OF_WEEK:
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.add(Calendar.WEEK_OF_YEAR, numPeriods);
            break;
            
        case TOP_OF_MONTH:
            cal.set(Calendar.DATE, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.add(Calendar.MONTH, numPeriods);
            break;

        default:
            throw new IllegalStateException("Unknown periodicity type.");
        }

        return cal.getTime();
    }

    public Date getEndOfNextNthPeriod(Date now, int periods) {
        return innerGetEndOfNextNthPeriod(this, this.periodicityType, now, periods);
    }

    public Date getNextTriggeringDate(Date now) {
        return getEndOfNextNthPeriod(now, 1);
    }
    
    public Date getsStartOfCurrentPeriod(long now) {
        Calendar aCal = Calendar.getInstance(getTimeZone());
        aCal.setTimeInMillis(now);
        return getEndOfNextNthPeriod(aCal.getTime(), 0);
    }
}
