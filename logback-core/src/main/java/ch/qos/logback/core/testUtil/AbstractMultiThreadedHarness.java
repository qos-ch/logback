/*
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
package ch.qos.logback.core.testUtil;

abstract public class AbstractMultiThreadedHarness {

    abstract public void waitUntilEndCondition() throws InterruptedException;

    public void execute(RunnableWithCounterAndDone[] runnableArray) throws InterruptedException {
        Thread[] threadArray = new Thread[runnableArray.length];

        for (int i = 0; i < runnableArray.length; i++) {
            System.out.println("Starting "+runnableArray[i]);
            threadArray[i] = new Thread(runnableArray[i], "Harness[" + i + "]");
        }
        for (Thread t : threadArray) {
            t.start();
        }

        waitUntilEndCondition();
        for (RunnableWithCounterAndDone r : runnableArray) {
            r.setDone(true);
        }
        for (Thread t : threadArray) {
            t.join();
        }
    }
}
