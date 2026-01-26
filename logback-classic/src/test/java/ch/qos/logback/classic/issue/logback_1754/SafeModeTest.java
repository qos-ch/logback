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

package ch.qos.logback.classic.issue.logback_1754;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.core.testUtil.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafeModeTest {

    private static final int THREADS = 3;

    private void runTest() {

        CountDownLatch latch = new CountDownLatch(THREADS);
        List<Thread> threads = new ArrayList<>(THREADS);
        for (int i = 0; i < THREADS; i++) {
            LoggerThread thread = new LoggerThread(latch, "message from thread " + i);
            thread.start();
            threads.add(thread);
        }
        int i = 0;
        for (Thread thread : threads) {
            try {
                thread.join();
                System.out.println("joined thread "+thread.getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
                //Thread.currentThread().interrupt();
                //throw new RuntimeException(e);
            }
        }
    }

    public static void main(String... args) {
        int diff = RandomUtil.getPositiveInt();
        //System.setProperty("logback.statusListenerClass", "sysout");
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, ClassicTestConstants.INPUT_PREFIX+"issue/logback-1754.xml");
        System.setProperty("logback_1754_targetDirectory", ClassicTestConstants.OUTPUT_DIR_PREFIX+"safeWrite_"+diff);


        new SafeModeTest().runTest();
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
            for(int i = 0; i < 100; i++) {
                if(i % 10 == 0) {
                    delay(1);
                }
                LOG.info(message + " i=" + i);
            }
        }

        static void delay(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
