/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2023, QOS.ch. All rights reserved.
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

package ch.qos.logback.classic.issue.logback_1754;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.core.testUtil.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static ch.qos.logback.classic.util.ContextInitializer.CONFIG_FILE_PROPERTY;

public class LogbackTest {

    private static final int THREADS = 16;

    private void runTest() {

        int diff = RandomUtil.getPositiveInt();
        //System.setProperty("logback.statusListenerClass", "sysout");
        System.setProperty(CONFIG_FILE_PROPERTY, ClassicTestConstants.INPUT_PREFIX+"issue/logback-1754.xml");
        System.setProperty("logback_1754_targetDirectory", ClassicTestConstants.OUTPUT_DIR_PREFIX+"safeWrite_"+diff);

        CountDownLatch latch = new CountDownLatch(THREADS);
        List<Thread> threads = new ArrayList<Thread>(THREADS);
        for (int i = 0; i < THREADS; i++) {
            LoggerThread thread = new LoggerThread(latch, "message from thread " + i);
            thread.start();
            threads.add(thread);
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String... args) {
        new LogbackTest().runTest();
    }

    private static final class LoggerThread extends Thread {
        private static final Logger LOG = LoggerFactory.getLogger(LoggerThread.class);
        private final CountDownLatch latch;
        private final String message;

        LoggerThread(CountDownLatch latch, String message) {
            setDaemon(false);
            this.latch = latch;
            this.message = message;
        }

        @Override
        public void run() {
            latch.countDown();
            LOG.info(message);
        }
    }
}
