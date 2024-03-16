/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;

import java.io.PrintStream;
import java.util.List;

/**
 * This class print status messages of a given {@link Context}. However, all its methods are
 * static. Use {@link StatusPrinter2} instead
 *
 * @deprecated replaced by {@link StatusPrinter2}
 */
public class StatusPrinter {

    private final static StatusPrinter2 SINGLETON = new StatusPrinter2();

    public static void setPrintStream(PrintStream printStream) {
        SINGLETON.setPrintStream(printStream);
    }

    /**
     * Print the contents of the context statuses, but only if they contain warnings
     * or errors.
     *
     * @param context a context to print
     */
    public static void printInCaseOfErrorsOrWarnings(Context context) {
        SINGLETON.printInCaseOfErrorsOrWarnings(context, 0);
    }

    /**
     * Print the contents of the context status, but only if they contain warnings
     * or errors occurring later than the threshold.
     *
     * @param context a context to print
     * @param threshold filter events later than the threshold
     */
    public static void printInCaseOfErrorsOrWarnings(Context context, long threshold) {
        SINGLETON.printInCaseOfErrorsOrWarnings(context, threshold);
    }

    /**
     * Print the contents of the context statuses, but only if they contain errors.
     *
     * @param context a context to print
     */
    public static void printIfErrorsOccured(Context context) {
        SINGLETON.printIfErrorsOccured(context);
    }

    /**
     * Print the contents of the context's status data.
     *
     * @param context a context to print
     */
    public static void print(Context context) {
        SINGLETON.print(context, 0);
    }

    /**
     * Print context's status data with a timestamp higher than the threshold.
     * 
     * @param context a context to print
     * @param threshold filter events later than the threshold
     */
    public static void print(Context context, long threshold) {
        SINGLETON.print(context, threshold);
    }

    public static void print(StatusManager sm) {
        SINGLETON.print(sm, 0);
    }

    public static void print(StatusManager sm, long threshold) {
        SINGLETON.print(sm, threshold);
    }

    public static void print(List<Status> statusList) {
        SINGLETON.print(statusList);
    }

    public static void buildStr(StringBuilder sb, String indentation, Status s) {
        SINGLETON.buildStr(sb, indentation, s);
    }
}
