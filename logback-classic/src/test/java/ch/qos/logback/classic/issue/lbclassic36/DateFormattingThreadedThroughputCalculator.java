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
package ch.qos.logback.classic.issue.lbclassic36;

import ch.qos.logback.classic.issue.lbclassic36.SelectiveDateFormattingRunnable.FormattingModel;
import ch.qos.logback.core.contention.ThreadedThroughputCalculator;

/**
 * Measure the threaded throughtput of date formatting operations
 * 
 * @author Joern Huxhorn
 * @author Ceki Gulcu
 */
public class DateFormattingThreadedThroughputCalculator {

    static int THREAD_COUNT = 16;
    static long OVERALL_DURATION_IN_MILLIS = 3000;

    public static void main(String args[]) throws InterruptedException {

        ThreadedThroughputCalculator tp = new ThreadedThroughputCalculator(OVERALL_DURATION_IN_MILLIS);
        tp.printEnvironmentInfo("DateFormatting");

        for (int i = 0; i < 2; i++) {
            tp.execute(buildArray(FormattingModel.SDF));
            tp.execute(buildArray(FormattingModel.JODA));
        }

        tp.execute(buildArray(FormattingModel.JODA));
        tp.printThroughput("JODA: ");

        tp.execute(buildArray(FormattingModel.SDF));
        tp.printThroughput("SDF:  ");

    }

    static SelectiveDateFormattingRunnable[] buildArray(FormattingModel model) {
        SelectiveDateFormattingRunnable[] array = new SelectiveDateFormattingRunnable[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            array[i] = new SelectiveDateFormattingRunnable(model);
        }
        return array;
    }

}
