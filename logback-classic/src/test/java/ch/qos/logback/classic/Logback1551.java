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

package ch.qos.logback.classic;

import ch.qos.logback.core.util.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Logback1551 {
    LoggerContext lc;

    @BeforeEach
    public void setUp() throws Exception {
        lc = new LoggerContext();
        lc.setName("x");
    }
    @Test
    public void testConcurrentModificationScheduledTasks() {
        ScheduledExecutorService scheduledExecutorService = lc.getScheduledExecutorService();
        Duration duration = Duration.buildByMilliseconds(10);

        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(runnable,
                duration.getMilliseconds(), duration.getMilliseconds(), TimeUnit.MILLISECONDS);

        lc.addScheduledFuture(scheduledFuture);
        int THREAD_COUNT = 20;
        Thread[] threads = new Thread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(new CancelRunnable(lc));
            threads[i].start();
        }

        Arrays.stream(threads).forEach(t-> {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private class CancelRunnable implements Runnable {
        LoggerContext lc;
        public CancelRunnable(LoggerContext lc) {
            this.lc = lc;
        }

        @Override
        public void run() {
            lc.cancelScheduledTasks();
        }
    }
}
