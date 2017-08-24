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

import java.util.concurrent.ThreadPoolExecutor;

public class WaitOnExecutionMultiThreadedHarness extends AbstractMultiThreadedHarness {
    ThreadPoolExecutor threadPoolExecutor;
    int count;

    public WaitOnExecutionMultiThreadedHarness(ThreadPoolExecutor threadPoolExecutor, int count) {
        this.threadPoolExecutor = threadPoolExecutor;
        this.count = count;

    }

    @Override
    public void waitUntilEndCondition() throws InterruptedException {
        while (threadPoolExecutor.getCompletedTaskCount() < count) {
            Thread.yield();
        }
    }
}
