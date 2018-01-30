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
package ch.qos.logback.core.issue.LOGBACK_849;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.util.ExecutorServiceUtil;

public class Basic {

    ExecutorService executor = ExecutorServiceUtil.newScheduledExecutorService();
    Context context = new ContextBase();

    @Test(timeout = 100)
    public void withNoSubmittedTasksShutdownNowShouldReturnImmediately() throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
    }

    @Ignore
    @Test
    public void withOneSlowTask() throws InterruptedException {
        executor.execute(new InterruptIgnoring(1000));
        Thread.sleep(100);
        ExecutorServiceUtil.shutdown(executor);
    }

    // InterruptIgnoring ===========================================
    static class InterruptIgnoring implements Runnable {

        int delay;

        InterruptIgnoring(int delay) {
            this.delay = delay;
        }

        public void run() {
            long runUntil = System.currentTimeMillis() + delay;

            while (true) {
                try {
                    long sleep = runUntil - System.currentTimeMillis();
                    System.out.println("will sleep " + sleep);
                    if (sleep > 0) {
                        Thread.sleep(delay);
                    } else {
                        return;
                    }
                } catch (InterruptedException e) {
                    // ignore the exception
                }
            }
        }
    }

}
