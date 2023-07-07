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

package ch.qos.logback.core.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.EchoEncoder;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.RandomUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class PrudentModeTest {

    FileAppender<Object> fa = new FileAppender<Object>();
    Context context = new ContextBase();

    StatusChecker statusChecker = new StatusChecker(context);
    int diff = RandomUtil.getPositiveInt();

    String outputDirStr = CoreTestConstants.OUTPUT_DIR_PREFIX + "prudentLockTest-" + diff + "/";
    String logfileStr = outputDirStr + "output.log";

    private static final int THREAD_COUNT = 8;
    private static final int LOOP_COUNT = 100/ THREAD_COUNT;

    @BeforeEach
    public void beforeEach() {
        File outputDir = new File(outputDirStr);
        if (!outputDir.mkdirs()) {
            fail("failed to create folder " + outputDir);
        }

        fa.setContext(context);
        fa.setName("FILE");
        fa.setPrudent(true);
        fa.setEncoder(new EchoEncoder<Object>());
        fa.setFile(logfileStr);
        fa.start();
    }

    // see https://jira.qos.ch/browse/LOGBACK-1754
    @Test
    public void assertNoOverlappingFileLockException () throws IOException {
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        List<Thread> threads = new ArrayList<>(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            LoggerThread thread = new LoggerThread(latch, "message from thread " + i);
            thread.start();
            threads.add(thread);
        }
        int i = 0;
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        StatusPrinter.print(context);
        statusChecker.assertIsWarningOrErrorFree();

        fa.stop();

        File file = new File(logfileStr);
        List<String> allLines = Files.readAllLines(file.toPath());
        int actualLineCount = allLines.size();
        assertEquals(LOOP_COUNT*THREAD_COUNT, actualLineCount, "unexpected line count "+actualLineCount);

    }

    class LoggerThread extends Thread {
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
            for (int i = 0; i < LOOP_COUNT; i++) {
                if ((i & 0x08) == 0) {
                    // yield to spice it up
                    Thread.yield();
                }
                PrudentModeTest.this.fa.doAppend(message + " i=" + i);
            }
        }

        void delay(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

