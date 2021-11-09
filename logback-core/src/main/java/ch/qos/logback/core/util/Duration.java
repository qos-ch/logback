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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Duration instances represent a lapse of time. Internally, the duration is
 * stored in milliseconds. However, whenever a parameter of type Duration is expected, Joran
 * (logback's configuration system) will automatically convert strings such as "20 seconds"
 * "3.5 minutes" or "5 hours" into Duration instances.
 *
 * <p>The recognized units of time are the "millisecond", "second", "minute" "hour" and "day".
 * The unit name may be followed by an "s". Thus, "2 day" and "2 days" are equivalent. In the
 * absence of a time unit specification, milliseconds are assumed.
 *
 * <p>Note: the conversion magic is entirely due to the fact that this class follows the
 * {@link #valueOf} convention.
 *
 * @author Ceki Gulcu
 */
public class Duration {

	private final static String DOUBLE_PART = "([0-9]*(.[0-9]+)?)";
	private final static int DOUBLE_GROUP = 1;

	private final static String UNIT_PART = "(|milli(second)?|second(e)?|minute|hour|day)s?";
	private final static int UNIT_GROUP = 3;

	private static final Pattern DURATION_PATTERN = Pattern.compile(DOUBLE_PART + "\\s*" + UNIT_PART, Pattern.CASE_INSENSITIVE);

	static final long SECONDS_COEFFICIENT = 1000;
	static final long MINUTES_COEFFICIENT = 60 * SECONDS_COEFFICIENT;
	static final long HOURS_COEFFICIENT = 60 * MINUTES_COEFFICIENT;
	static final long DAYS_COEFFICIENT = 24 * HOURS_COEFFICIENT;

	final long millis;

	public Duration(final long millis) {
		this.millis = millis;
	}

	public static Duration buildByMilliseconds(final double value) {
		return new Duration((long) value);
	}

	public static Duration buildBySeconds(final double value) {
		return new Duration((long) (SECONDS_COEFFICIENT * value));
	}

	public static Duration buildByMinutes(final double value) {
		return new Duration((long) (MINUTES_COEFFICIENT * value));
	}

	public static Duration buildByHours(final double value) {
		return new Duration((long) (HOURS_COEFFICIENT * value));
	}

	public static Duration buildByDays(final double value) {
		return new Duration((long) (DAYS_COEFFICIENT * value));
	}

	public static Duration buildUnbounded() {
		return new Duration(Long.MAX_VALUE);
	}

	public long getMilliseconds() {
		return millis;
	}

	public static Duration valueOf(final String durationStr) {
		final Matcher matcher = DURATION_PATTERN.matcher(durationStr);

		if (!matcher.matches()) {
			throw new IllegalArgumentException("String value [" + durationStr + "] is not in the expected format.");
		}
		final String doubleStr = matcher.group(DOUBLE_GROUP);
		final String unitStr = matcher.group(UNIT_GROUP);

		final double doubleValue = Double.parseDouble(doubleStr);
		if (unitStr.equalsIgnoreCase("milli") || unitStr.equalsIgnoreCase("millisecond") || unitStr.length() == 0) {
			return buildByMilliseconds(doubleValue);
		} else if (unitStr.equalsIgnoreCase("second") || unitStr.equalsIgnoreCase("seconde")) {
			return buildBySeconds(doubleValue);
		} else if (unitStr.equalsIgnoreCase("minute")) {
			return buildByMinutes(doubleValue);
		} else if (unitStr.equalsIgnoreCase("hour")) {
			return buildByHours(doubleValue);
		} else if (unitStr.equalsIgnoreCase("day")) {
			return buildByDays(doubleValue);
		} else {
			throw new IllegalStateException("Unexpected " + unitStr);
		}
	}

	@Override
	public String toString() {
		if (millis < SECONDS_COEFFICIENT) {
			return millis + " milliseconds";
		}
		if (millis < MINUTES_COEFFICIENT) {
			return millis / SECONDS_COEFFICIENT + " seconds";
		} else if (millis < HOURS_COEFFICIENT) {
			return millis / MINUTES_COEFFICIENT + " minutes";
		} else {
			return millis / HOURS_COEFFICIENT + " hours";
		}

	}
}
