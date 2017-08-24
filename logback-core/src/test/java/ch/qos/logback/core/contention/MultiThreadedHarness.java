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
 * Useful scaffolding/harness to start and processPriorToRemoval multiple threads.
 * 
 * @author Joern Huxhorn
 * @author Ralph Goers
 * @author Ceki Gulcu
 */
public class MultiThreadedHarness extends AbstractMultiThreadedHarness {

    final long overallDurationInMillis;

    public MultiThreadedHarness(long overallDurationInMillis) {
        this.overallDurationInMillis = overallDurationInMillis;
    }

    public void printEnvironmentInfo(String msg) {
        System.out.println("=== " + msg + " ===");
        System.out.println("java.runtime.version = " + System.getProperty("java.runtime.version"));
        System.out.println("java.vendor          = " + System.getProperty("java.vendor"));
        System.out.println("java.version         = " + System.getProperty("java.version"));
        System.out.println("os.name              = " + System.getProperty("os.name"));
        System.out.println("os.version           = " + System.getProperty("os.version"));
    }

    public void waitUntilEndCondition() throws InterruptedException {
        Thread.sleep(overallDurationInMillis);
    }
}
