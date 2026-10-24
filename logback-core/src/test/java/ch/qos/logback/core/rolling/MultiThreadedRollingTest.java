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
package ch.qos.logback.core.rolling;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import ch.qos.logback.core.testUtil.CoreTestConstants;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.contention.MultiThreadedHarness;
import ch.qos.logback.core.testUtil.RunnableWithCounterAndDone;
import ch.qos.logback.core.encoder.EchoEncoder;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.StatusPrinter;

public class MultiThreadedRollingTest {

    final static int NUM_THREADS = 10;
    final static int TOTAL_DURATION = 600;
    RunnableWithCounterAndDone[] runnableArray;

    Encoder<Object> encoder;
    Context context = new ContextBase();

    int diff = RandomUtil.getPositiveInt();
    String outputDirStr = CoreTestConstants.OUTPUT_DIR_PREFIX + "multi-" + diff + "/";

    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();

    @BeforeEach
    public void setUp() throws Exception {
        encoder = new EchoEncoder<Object>();
        File outputDir = new File(outputDirStr);
        boolean result = outputDir.mkdirs();
        if(!result) {
            System.out.println("Failed to create folder "+outputDirStr);
        }
        System.out.println("Output dir [" + outputDirStr + "]");

        rfa.setName("rolling");
        rfa.setEncoder(encoder);
        rfa.setContext(context);
        rfa.setFile(outputDirStr + "output.log");

    }

    @AfterEach
    public void tearDown() throws Exception {
        rfa.stop();
    }

    public void setUpTimeBasedTriggeringPolicy(RollingFileAppender<Object> rfa) {
        String datePattern = "yyyy-MM-dd'T'HH_mm_ss_SSS";
        TimeBasedRollingPolicy<Object> tbrp = new TimeBasedRollingPolicy<>();
        tbrp.setFileNamePattern(outputDirStr + "test-%d{" + datePattern + "}");
        tbrp.setContext(context);
        tbrp.setParent(rfa);
        tbrp.start();

        rfa.setRollingPolicy(tbrp);
        rfa.start();
    }

    public void setUpSizeBasedTriggeringPolicy(RollingFileAppender<Object> rfa) {
        SizeBasedTriggeringPolicy<Object> zbtp = new SizeBasedTriggeringPolicy<>();
        zbtp.setContext(context);
        zbtp.setMaxFileSize(FileSize.valueOf("100KB"));
        // the default check increment of 60 seconds would prevent any rollover during the test
        zbtp.setCheckIncrement(Duration.buildByMilliseconds(10));

        zbtp.start();
        rfa.setTriggeringPolicy(zbtp);

        FixedWindowRollingPolicy fwrp = new FixedWindowRollingPolicy();
        fwrp.setContext(context);
        fwrp.setFileNamePattern(outputDirStr + "test-%i.log");
        fwrp.setMaxIndex(20);
        fwrp.setMinIndex(0);
        fwrp.setParent(rfa);
        fwrp.start();
        rfa.setRollingPolicy(fwrp);
        rfa.start();
    }

    RunnableWithCounterAndDone[] buildRunnableArray(boolean withDelay) {
        RunnableWithCounterAndDone[] runnableArray = new RunnableWithCounterAndDone[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            runnableArray[i] = new RFARunnable(i, rfa, withDelay);
        }
        return runnableArray;
    }

    @Test
    public void multiThreadedTimedBased() throws InterruptedException, IOException {
        setUpTimeBasedTriggeringPolicy(rfa);
        executeHarness(TOTAL_DURATION, false);
        verify(timeBasedFilesInOrder());
    }

    int testFileCount() {
        File outputDir = new File(outputDirStr);
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.matches("test-\\d{1,2}.log")) {
                    return true;
                }
                return false;
            }
        };
        File[] files = outputDir.listFiles(filter);
        return files.length;
    }

    // archives are named after their date, so sorting by name gives the order of writing
    List<File> timeBasedFilesInOrder() {
        File[] archives = new File(outputDirStr).listFiles((dir, name) -> name.startsWith("test-"));
        Arrays.sort(archives, Comparator.comparing(File::getName));
        List<File> result = new ArrayList<>(Arrays.asList(archives));
        result.add(new File(outputDirStr + "output.log"));
        return result;
    }

    // the highest index is the oldest archive
    List<File> sizeBasedFilesInOrder(int numFiles) {
        List<File> result = new ArrayList<>();
        for (int i = numFiles - 1; i >= 0; i--) {
            result.add(new File(outputDirStr + "test-" + i + ".log"));
        }
        result.add(new File(outputDirStr + "output.log"));
        return result;
    }

    // each line is "<threadId> <counter>"; the counters of each thread must appear
    // in order, without gaps, up to the final value of the thread's counter
    void verify(List<File> filesInOrder) throws IOException {
        long[] lastCounter = new long[NUM_THREADS];
        for (File file : filesInOrder) {
            for (String line : Files.readAllLines(file.toPath())) {
                int space = line.indexOf(' ');
                int id = Integer.parseInt(line.substring(0, space));
                long counter = Long.parseLong(line.substring(space + 1));
                Assertions.assertEquals(lastCounter[id] + 1, counter, "thread " + id + " in " + file.getName());
                lastCounter[id] = counter;
            }
        }
        for (int i = 0; i < NUM_THREADS; i++) {
            Assertions.assertEquals(runnableArray[i].getCounter(), lastCounter[i], "final counter of thread " + i);
        }
    }

    @Test
    public void multiThreadedSizeBased() throws InterruptedException, IOException {
        setUpSizeBasedTriggeringPolicy(rfa);
        // on a fast machine with a fast hard disk, if the tests runs for too
        // long the MAX_WINDOW_SIZE is reached, resulting in data loss which
        // we cannot test for.
        executeHarness(TOTAL_DURATION, true);
        verify(sizeBasedFilesInOrder(testFileCount()));
    }

    private void executeHarness(int duration, boolean withDelay) throws InterruptedException {
        MultiThreadedHarness multiThreadedHarness = new MultiThreadedHarness(duration);
        this.runnableArray = buildRunnableArray(withDelay);
        multiThreadedHarness.execute(runnableArray);

        StatusChecker checker = new StatusChecker(context.getStatusManager());
        if (!checker.isErrorFree(0)) {
            StatusPrinter.print(context);
            Assertions.fail("errors reported");
        }
    }

    long diff(long start) {
        return System.currentTimeMillis() - start;
    }

    static class RFARunnable extends RunnableWithCounterAndDone {
        RollingFileAppender<Object> rfa;
        int id;
        boolean withInducedDelay;

        RFARunnable(int id, RollingFileAppender<Object> rfa, boolean withInducedDelay) {
            this.id = id;
            this.rfa = rfa;
            this.withInducedDelay = withInducedDelay;
        }

        public void run() {
            while (!isDone()) {
                counter++;
                rfa.doAppend(id + " " + counter);
                if ((counter % 64 == 0) && withInducedDelay) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }
}
