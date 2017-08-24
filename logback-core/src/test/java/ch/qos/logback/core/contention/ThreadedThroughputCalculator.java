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
package ch.qos.logback.core.contention;

/**
 * Useful scaffolding to measure the throughput of certain operations when
 * invoked by multiple threads.
 * 
 * @author Joern Huxhorn
 * @author Ralph Goers
 * @author Ceki Gulcu
 */
public class ThreadedThroughputCalculator extends MultiThreadedHarness {

    public ThreadedThroughputCalculator(long overallDurationInMillis) {
        super(overallDurationInMillis);
    }

    public void printThroughput(String msg) throws InterruptedException {
        printThroughput(msg, false);
    }

    public void printThroughput(String msg, boolean detailed) throws InterruptedException {
        long sum = 0;
        for (RunnableWithCounterAndDone r : runnableArray) {
            if (detailed) {
                System.out.println(r + " count=" + r.getCounter());
            }
            sum += r.getCounter();
        }

        System.out.println(msg + "total of " + sum + " operations, or " + ((sum) / overallDurationInMillis) + " operations per millisecond");
    }
}
