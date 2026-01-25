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

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.helpers.ThrowableToStringArray;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.StatusUtil;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import static ch.qos.logback.core.status.StatusUtil.filterStatusListByTimeThreshold;

/**
 *
 * Same as StatusPrinter but with instance methods instead of static.
 *
 * @since 1.5.4
 */
public class StatusPrinter2 {


    private PrintStream ps = System.out;

    static CachingDateFormatter cachingDateFormat = new CachingDateFormatter("HH:mm:ss,SSS");

    public void setPrintStream(PrintStream printStream) {
        ps = printStream;
    }

    /**
     * Print the contents of the context statuses, but only if they contain warnings
     * or errors.
     *
     * @param context a context to print
     */
    public void printInCaseOfErrorsOrWarnings(Context context) {
        printInCaseOfErrorsOrWarnings(context, 0);
    }

    /**
     * Print the contents of the context status, but only if they contain warnings
     * or errors occurring later than the threshold.
     *
     * @param context a context to print
     * @param threshold filter events later than the threshold
     */
    public void printInCaseOfErrorsOrWarnings(Context context, long threshold) {
        if (context == null) {
            throw new IllegalArgumentException("Context argument cannot be null");
        }

        StatusManager sm = context.getStatusManager();
        if (sm == null) {
            ps.println("WARN: Context named \"" + context.getName() + "\" has no status manager");
        } else {
            StatusUtil statusUtil = new StatusUtil(context);
            if (statusUtil.getHighestLevel(threshold) >= ErrorStatus.WARN) {
                print(sm, threshold);
            }
        }
    }

    /**
     * Print the contents of the context statuses, but only if they contain errors.
     *
     * @param context  a context to print
     */
    public void printIfErrorsOccured(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context argument cannot be null");
        }

        StatusManager sm = context.getStatusManager();
        if (sm == null) {
            ps.println("WARN: Context named \"" + context.getName() + "\" has no status manager");
        } else {
            StatusUtil statusUtil = new StatusUtil(context);
            if (statusUtil.getHighestLevel(0) == ErrorStatus.ERROR) {
                print(sm);
            }
        }
    }

    /**
     * Print the contents of the context's status data.
     *
     * @param context a context to print
     */
    public void print(Context context) {
        print(context, 0);
    }

    /**
     * Print context's status data with a timestamp higher than the threshold.
     *
     * @param context a context to print
     */
    public  void print(Context context, long threshold) {
        if (context == null) {
            throw new IllegalArgumentException("Context argument cannot be null");
        }

        StatusManager sm = context.getStatusManager();
        if (sm == null) {
            ps.println("WARN: Context named \"" + context.getName() + "\" has no status manager");
        } else {
            print(sm, threshold);
        }
    }

    public void print(StatusManager sm) {
        print(sm, 0);
    }

    public  void print(StatusManager sm, long threshold) {
        StringBuilder sb = new StringBuilder();
        List<Status> filteredList = filterStatusListByTimeThreshold(sm.getCopyOfStatusList(), threshold);
        buildStrFromStatusList(sb, filteredList);
        ps.println(sb.toString());
    }

    public void print(List<Status> statusList) {
        StringBuilder sb = new StringBuilder();
        buildStrFromStatusList(sb, statusList);
        ps.println(sb.toString());
    }

    private void buildStrFromStatusList(StringBuilder sb, List<Status> statusList) {
        if (statusList == null)
            return;
        for (Status s : statusList) {
            buildStr(sb, "", s);
        }
    }

    private void appendThrowable(StringBuilder sb, Throwable t) {
        String[] stringRep = ThrowableToStringArray.convert(t);

        for (String s : stringRep) {
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

    public void buildStr(StringBuilder sb, String indentation, Status s) {
        String prefix;
        if (s.hasChildren()) {
            prefix = indentation + "+ ";
        } else {
            prefix = indentation + "|-";
        }

        if (cachingDateFormat != null) {
            String dateStr = cachingDateFormat.format(s.getTimestamp());
            sb.append(dateStr).append(" ");
        }
        sb.append(prefix).append(s).append(CoreConstants.LINE_SEPARATOR);

        if (s.getThrowable() != null) {
            appendThrowable(sb, s.getThrowable());
        }
        if (s.hasChildren()) {
            Iterator<Status> ite = s.iterator();
            while (ite.hasNext()) {
                Status child = ite.next();
                buildStr(sb, indentation + "  ", child);
            }
        }
    }
}
