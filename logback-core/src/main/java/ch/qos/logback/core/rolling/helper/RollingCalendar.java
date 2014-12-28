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

  public RollingCalendar() {
    super();
  }

  public RollingCalendar(TimeZone tz, Locale locale) {
    super(tz, locale);
  }

  public void init(String datePattern) {
    periodicityType = computePeriodicityType(datePattern);
  }

  private void setPeriodicityType(PeriodicityType periodicityType) {
    this.periodicityType = periodicityType;
  }

  public PeriodicityType getPeriodicityType() {
    return periodicityType;
  }

  public long getNextTriggeringMillis(Date now) {
    return getNextTriggeringDate(now).getTime();
  }

  // This method computes the roll over period by looping over the
  // periods, starting with the shortest, and stopping when the r0 is
  // different from from r1, where r0 is the epoch formatted according
  // the datePattern (supplied by the user) and r1 is the
  // epoch+nextMillis(i) formatted according to datePattern. All date
  // formatting is done in GMT and not local format because the test
  // logic is based on comparisons relative to 1970-01-01 00:00:00
  // GMT (the epoch).
  public PeriodicityType computePeriodicityType(String datePattern) {
    RollingCalendar rollingCalendar = new RollingCalendar(GMT_TIMEZONE, Locale
            .getDefault());

    // set sate to 1970-01-01 00:00:00 GMT
    Date epoch = new Date(0);

    if (datePattern != null) {
      for (PeriodicityType i : PeriodicityType.VALID_ORDERED_LIST) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
        simpleDateFormat.setTimeZone(GMT_TIMEZONE); // all date formatting done
        // in GMT

        String r0 = simpleDateFormat.format(epoch);
        rollingCalendar.setPeriodicityType(i);

        Date next = new Date(rollingCalendar.getNextTriggeringMillis(epoch));
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

  public long periodsElapsed(long start, long end) {
    if (start > end)
      throw new IllegalArgumentException("Start cannot come before end");

    long diff = end - start;
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

  public Date getRelativeDate(Date now, int periods) {
    this.setTime(now);

    switch (periodicityType) {
      case TOP_OF_MILLISECOND:
        this.add(Calendar.MILLISECOND, periods);
        break;

      case TOP_OF_SECOND:
        this.set(Calendar.MILLISECOND, 0);
        this.add(Calendar.SECOND, periods);
        break;

      case TOP_OF_MINUTE:
        this.set(Calendar.SECOND, 0);
        this.set(Calendar.MILLISECOND, 0);
        this.add(Calendar.MINUTE, periods);
        break;

      case TOP_OF_HOUR:
        this.set(Calendar.MINUTE, 0);
        this.set(Calendar.SECOND, 0);
        this.set(Calendar.MILLISECOND, 0);
        this.add(Calendar.HOUR_OF_DAY, periods);
        break;

      case TOP_OF_DAY:
        this.set(Calendar.HOUR_OF_DAY, 0);
        this.set(Calendar.MINUTE, 0);
        this.set(Calendar.SECOND, 0);
        this.set(Calendar.MILLISECOND, 0);
        this.add(Calendar.DATE, periods);
        break;

      case TOP_OF_WEEK:
        this.set(Calendar.DAY_OF_WEEK, getFirstDayOfWeek());
        this.set(Calendar.HOUR_OF_DAY, 0);
        this.set(Calendar.MINUTE, 0);
        this.set(Calendar.SECOND, 0);
        this.set(Calendar.MILLISECOND, 0);
        this.add(Calendar.WEEK_OF_YEAR, periods);
        break;

      case TOP_OF_MONTH:
        this.set(Calendar.DATE, 1);
        this.set(Calendar.HOUR_OF_DAY, 0);
        this.set(Calendar.MINUTE, 0);
        this.set(Calendar.SECOND, 0);
        this.set(Calendar.MILLISECOND, 0);
        this.add(Calendar.MONTH, periods);
        break;

      default:
        throw new IllegalStateException("Unknown periodicity type.");
    }

    return getTime();
  }

  public Date getNextTriggeringDate(Date now) {
    return getRelativeDate(now, 1);
  }
}
