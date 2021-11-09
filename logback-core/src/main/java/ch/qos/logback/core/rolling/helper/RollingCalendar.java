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

import static ch.qos.logback.core.CoreConstants.MILLIS_IN_ONE_DAY;
import static ch.qos.logback.core.CoreConstants.MILLIS_IN_ONE_HOUR;
import static ch.qos.logback.core.CoreConstants.MILLIS_IN_ONE_MINUTE;
import static ch.qos.logback.core.CoreConstants.MILLIS_IN_ONE_SECOND;
import static ch.qos.logback.core.CoreConstants.MILLIS_IN_ONE_WEEK;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

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

	public RollingCalendar(final String datePattern) {
		this.datePattern = datePattern;
		periodicityType = computePeriodicityType();
	}

	public RollingCalendar(final String datePattern, final TimeZone tz, final Locale locale) {
		super(tz, locale);
		this.datePattern = datePattern;
		periodicityType = computePeriodicityType();
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

		final GregorianCalendar calendar = new GregorianCalendar(GMT_TIMEZONE, Locale.getDefault());

		// set sate to 1970-01-01 00:00:00 GMT
		final Date epoch = new Date(0);

		if (datePattern != null) {
			for (final PeriodicityType i : PeriodicityType.VALID_ORDERED_LIST) {
				final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
				simpleDateFormat.setTimeZone(GMT_TIMEZONE); // all date formatting done in GMT

				final String r0 = simpleDateFormat.format(epoch);

				final Date next = innerGetEndOfThisPeriod(calendar, i, epoch);
				final String r1 = simpleDateFormat.format(next);

				// System.out.println("Type = "+i+", r0 = "+r0+", r1 = "+r1);
				if (r0 != null && r1 != null && !r0.equals(r1)) {
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
			
			// isolated dd
			// DD
			if (collision(7 * MILLIS_IN_ONE_DAY) || collision(31 * MILLIS_IN_ONE_DAY) || collision(365 * MILLIS_IN_ONE_DAY)) {
				return false;
			}
			return true;
		case TOP_OF_WEEK:
			// WW
			if (collision(34 * MILLIS_IN_ONE_DAY)) {
				return false;
			}
			// isolated ww
			if (collision(366 * MILLIS_IN_ONE_DAY)) {
				return false;
			}
			return true;
		default:
			return true;
		}
	}

	private boolean collision(final long delta) {
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
		simpleDateFormat.setTimeZone(GMT_TIMEZONE); // all date formatting done in GMT
		final Date epoch0 = new Date(0);
		final String r0 = simpleDateFormat.format(epoch0);
		final Date epoch12 = new Date(delta);
		final String r12 = simpleDateFormat.format(epoch12);

		return r0.equals(r12);
	}

	public void printPeriodicity(final ContextAwareBase cab) {
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

	public long periodBarriersCrossed(final long start, final long end) {
		if (start > end) {
			throw new IllegalArgumentException("Start cannot come before end");
		}

		final long startFloored = getStartOfCurrentPeriodWithGMTOffsetCorrection(start, getTimeZone());
		final long endFloored = getStartOfCurrentPeriodWithGMTOffsetCorrection(end, getTimeZone());

		final long diff = endFloored - startFloored;

		switch (periodicityType) {

		case TOP_OF_MILLISECOND:
			return diff;
		case TOP_OF_SECOND:
			return diff / MILLIS_IN_ONE_SECOND;
		case TOP_OF_MINUTE:
			return diff / MILLIS_IN_ONE_MINUTE;
		case TOP_OF_HOUR:
			return diff / MILLIS_IN_ONE_HOUR;
		case TOP_OF_DAY:
			return diff / MILLIS_IN_ONE_DAY;
		case TOP_OF_WEEK:
			return diff / MILLIS_IN_ONE_WEEK;
		case TOP_OF_MONTH:
			return diffInMonths(start, end);
		default:
			throw new IllegalStateException("Unknown periodicity type.");
		}
	}

	public static int diffInMonths(final long startTime, final long endTime) {
		if (startTime > endTime) {
			throw new IllegalArgumentException("startTime cannot be larger than endTime");
		}
		final Calendar startCal = Calendar.getInstance();
		startCal.setTimeInMillis(startTime);
		final Calendar endCal = Calendar.getInstance();
		endCal.setTimeInMillis(endTime);
		final int yearDiff = endCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR);
		final int monthDiff = endCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH);
		return yearDiff * 12 + monthDiff;
	}

	static private Date innerGetEndOfThisPeriod(final Calendar cal, final PeriodicityType periodicityType, final Date now) {
		return innerGetEndOfNextNthPeriod(cal, periodicityType, now, 1);
	}

	static private Date innerGetEndOfNextNthPeriod(final Calendar cal, final PeriodicityType periodicityType, final Date now, final int numPeriods) {
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

	public Date getEndOfNextNthPeriod(final Date now, final int periods) {
		return innerGetEndOfNextNthPeriod(this, periodicityType, now, periods);
	}

	public Date getNextTriggeringDate(final Date now) {
		return getEndOfNextNthPeriod(now, 1);
	}

	public long getStartOfCurrentPeriodWithGMTOffsetCorrection(final long now, final TimeZone timezone) {
		Date toppedDate;

		// there is a bug in Calendar which prevents it from
		// computing the correct DST_OFFSET when the time changes
		{
			final Calendar aCal = Calendar.getInstance(timezone);
			aCal.setTimeInMillis(now);
			toppedDate = getEndOfNextNthPeriod(aCal.getTime(), 0);
		}
		final Calendar secondCalendar = Calendar.getInstance(timezone);
		secondCalendar.setTimeInMillis(toppedDate.getTime());
		final long gmtOffset = secondCalendar.get(Calendar.ZONE_OFFSET) + secondCalendar.get(Calendar.DST_OFFSET);
		return toppedDate.getTime() + gmtOffset;
	}
}
