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

import static ch.qos.logback.core.status.StatusUtil.filterStatusListByTimeThreshold;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.helpers.ThrowableToStringArray;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.StatusUtil;

public class StatusPrinter {

	private static PrintStream ps = System.out;

	static CachingDateFormatter cachingDateFormat = new CachingDateFormatter("HH:mm:ss,SSS");

	public static void setPrintStream(final PrintStream printStream) {
		ps = printStream;
	}

	/**
	 * Print the contents of the context statuses, but only if they contain
	 * warnings or errors.
	 *
	 * @param context
	 */
	public static void printInCaseOfErrorsOrWarnings(final Context context) {
		printInCaseOfErrorsOrWarnings(context, 0);
	}

	/**
	 * Print the contents of the context status, but only if they contain
	 * warnings or errors occurring later then the threshold.
	 *
	 * @param context
	 */
	public static void printInCaseOfErrorsOrWarnings(final Context context, final long threshold) {
		if (context == null) {
			throw new IllegalArgumentException("Context argument cannot be null");
		}

		final StatusManager sm = context.getStatusManager();
		if (sm == null) {
			ps.println("WARN: Context named \"" + context.getName() + "\" has no status manager");
		} else {
			final StatusUtil statusUtil = new StatusUtil(context);
			if (statusUtil.getHighestLevel(threshold) >= Status.WARN) {
				print(sm, threshold);
			}
		}
	}

	/**
	 * Print the contents of the context statuses, but only if they contain
	 * errors.
	 *
	 * @param context
	 */
	public static void printIfErrorsOccured(final Context context) {
		if (context == null) {
			throw new IllegalArgumentException("Context argument cannot be null");
		}

		final StatusManager sm = context.getStatusManager();
		if (sm == null) {
			ps.println("WARN: Context named \"" + context.getName() + "\" has no status manager");
		} else {
			final StatusUtil statusUtil = new StatusUtil(context);
			if (statusUtil.getHighestLevel(0) == Status.ERROR) {
				print(sm);
			}
		}
	}

	/**
	 * Print the contents of the context's status data.
	 *
	 * @param context
	 */
	public static void print(final Context context) {
		print(context, 0);
	}

	/**
	 * Print context's status data with a timestamp higher than the threshold.
	 * @param context
	 */
	public static void print(final Context context, final long threshold) {
		if (context == null) {
			throw new IllegalArgumentException("Context argument cannot be null");
		}

		final StatusManager sm = context.getStatusManager();
		if (sm == null) {
			ps.println("WARN: Context named \"" + context.getName() + "\" has no status manager");
		} else {
			print(sm, threshold);
		}
	}

	public static void print(final StatusManager sm) {
		print(sm, 0);
	}

	public static void print(final StatusManager sm, final long threshold) {
		final StringBuilder sb = new StringBuilder();
		final List<Status> filteredList = filterStatusListByTimeThreshold(sm.getCopyOfStatusList(), threshold);
		buildStrFromStatusList(sb, filteredList);
		ps.println(sb.toString());
	}

	public static void print(final List<Status> statusList) {
		final StringBuilder sb = new StringBuilder();
		buildStrFromStatusList(sb, statusList);
		ps.println(sb.toString());
	}

	private static void buildStrFromStatusList(final StringBuilder sb, final List<Status> statusList) {
		if (statusList == null) {
			return;
		}
		for (final Status s : statusList) {
			buildStr(sb, "", s);
		}
	}

	// private static void buildStrFromStatusManager(StringBuilder sb, StatusManager sm) {
	// }

	private static void appendThrowable(final StringBuilder sb, final Throwable t) {
		final String[] stringRep = ThrowableToStringArray.convert(t);

		for (final String s : stringRep) {
			if (s.startsWith(CoreConstants.CAUSED_BY)) {
				// nothing
			} else if (Character.isDigit(s.charAt(0))) {
				// if line resembles "48 common frames omitted"
				sb.append("\t... ");
			} else {
				// most of the time. just add a tab+"at"
				sb.append("\tat ");
			}
			sb.append(s).append(CoreConstants.LINE_SEPARATOR);
		}
	}

	public static void buildStr(final StringBuilder sb, final String indentation, final Status s) {
		String prefix;
		if (s.hasChildren()) {
			prefix = indentation + "+ ";
		} else {
			prefix = indentation + "|-";
		}

		if (cachingDateFormat != null) {
			final String dateStr = cachingDateFormat.format(s.getDate());
			sb.append(dateStr).append(" ");
		}
		sb.append(prefix).append(s).append(CoreConstants.LINE_SEPARATOR);

		if (s.getThrowable() != null) {
			appendThrowable(sb, s.getThrowable());
		}
		if (s.hasChildren()) {
			final Iterator<Status> ite = s.iterator();
			while (ite.hasNext()) {
				final Status child = ite.next();
				buildStr(sb, indentation + "  ", child);
			}
		}
	}

}
