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
package ch.qos.logback.core.issue;

import ch.qos.logback.core.contention.ThreadedThroughputCalculator;
import ch.qos.logback.core.issue.SelectiveLockRunnable.LockingModel;

/**
 * Short sample code testing the throughput of a fair lock.
 * 
 * @author Joern Huxhorn
 * @author Ceki Gulcu
 */
public class LockThroughput {

    static int THREAD_COUNT = 10;
    static long OVERALL_DURATION_IN_MILLIS = 5000;

    public static void main(String args[]) throws InterruptedException {

        ThreadedThroughputCalculator tp = new ThreadedThroughputCalculator(OVERALL_DURATION_IN_MILLIS);
        tp.printEnvironmentInfo("LockThroughput");

        for (int i = 0; i < 2; i++) {
            tp.execute(buildArray(LockingModel.SYNC));
            tp.execute(buildArray(LockingModel.UNFAIR));
            tp.execute(buildArray(LockingModel.FAIR));
        }

        tp.execute(buildArray(LockingModel.SYNC));
        tp.printThroughput("Sync:   ");

        tp.execute(buildArray(LockingModel.UNFAIR));
        tp.printThroughput("Unfair: ");

        tp.execute(buildArray(LockingModel.FAIR));
        tp.printThroughput("Fair:   ");
    }

    static SelectiveLockRunnable[] buildArray(LockingModel model) {
        SelectiveLockRunnable[] array = new SelectiveLockRunnable[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            array[i] = new SelectiveLockRunnable(model);
        }
        return array;
    }

}
