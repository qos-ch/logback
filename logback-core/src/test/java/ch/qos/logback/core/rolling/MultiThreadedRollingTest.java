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
package ch.qos.logback.core.rolling;

import static ch.qos.logback.core.util.CoreTestConstants.FAILURE_EXIT_CODE;
import static ch.qos.logback.core.util.CoreTestConstants.SUCCESSFUL_EXIT_CODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;

import ch.qos.logback.core.testUtil.EnvUtilForTests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.contention.MultiThreadedHarness;
import ch.qos.logback.core.contention.RunnableWithCounterAndDone;
import ch.qos.logback.core.encoder.EchoEncoder;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.StatusPrinter;

public class MultiThreadedRollingTest {

    final static int NUM_THREADS = 10;
    final static int TOTAL_DURATION = 600;
    RunnableWithCounterAndDone[] runnableArray;

    Encoder<Object> encoder;
    Context context = new ContextBase();

    static String VERIFY_SH = "verify.sh";

    int diff = RandomUtil.getPositiveInt();
    String outputDirStr = CoreTestConstants.OUTPUT_DIR_PREFIX + "multi-" + diff + "/";

    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();

    String pathToBash = EnvUtilForTests.getPathToBash();
    OutputStream scriptOS;

    @Before
    public void setUp() throws Exception {
        encoder = new EchoEncoder<Object>();
        File outputDir = new File(outputDirStr);
        outputDir.mkdirs();

        System.out.println("Output dir [" + outputDirStr + "]");

        scriptOS = openScript();

        rfa.setName("rolling");
        rfa.setEncoder(encoder);
        rfa.setContext(context);
        rfa.setFile(outputDirStr + "output.log");

    }

    void close(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        rfa.stop();
    }

    public void setUpTimeBasedTriggeringPolicy(RollingFileAppender<Object> rfa) {
        String datePattern = "yyyy-MM-dd'T'HH_mm_ss_SSS";
        TimeBasedRollingPolicy tbrp = new TimeBasedRollingPolicy();
        tbrp.setFileNamePattern(outputDirStr + "test-%d{" + datePattern + "}");
        tbrp.setContext(context);
        tbrp.setParent(rfa);
        tbrp.start();

        rfa.setRollingPolicy(tbrp);
        rfa.start();
    }

    public void setUpSizeBasedTriggeringPolicy(RollingFileAppender<Object> rfa) {
        SizeBasedTriggeringPolicy<Object> zbtp = new SizeBasedTriggeringPolicy<Object>();
        zbtp.setContext(context);
        zbtp.setMaxFileSize(FileSize.valueOf("100KB"));

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

    OutputStream openScript() throws IOException {
        return new FileOutputStream(outputDirStr + VERIFY_SH);
    }

    @Test
    public void multiThreadedTimedBased() throws InterruptedException, IOException {
        setUpTimeBasedTriggeringPolicy(rfa);
        executeHarness(TOTAL_DURATION, false);
        printScriptForTimeBased();
        verify();
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

    void verify() throws IOException, InterruptedException {
        close(scriptOS);
        // no point in this test if we don't have bash
        if (pathToBash == null) {
            return;
        }
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(pathToBash, VERIFY_SH);
        pb.directory(new File(outputDirStr));
        Process process = pb.start();
        process.waitFor();
        int exitCode = process.exitValue();

        assertEquals(SUCCESSFUL_EXIT_CODE, exitCode);
        System.out.println("External script based verification returned with exit code " + exitCode);
    }

    @Test
    public void multiThreadedSizeBased() throws InterruptedException, IOException {
        setUpSizeBasedTriggeringPolicy(rfa);
        // on a fast machine with a fast hard disk, if the tests runs for too
        // long the MAX_WINDOW_SIZE is reached, resulting in data loss which
        // we cannot test for.
        executeHarness(TOTAL_DURATION, true);
        int numFiles = testFileCount();
        printScriptForSizeBased(numFiles);
        verify();
    }

    private void printScriptHeader(String type) throws IOException {
        out("# ====================================================");
        out("# A script to check the exactness of the output ");
        out("# produced by " + type + " test");
        out("# ====================================================");
        out("# ");
    }

    private void printCommonScriptCore() throws IOException {
        out("");
        out("for t in $(seq 0 1 " + (NUM_THREADS - 1) + ")");
        out("do");
        out("  echo \"Testing results of thread $t\"");
        out("  grep \"$t \" aggregated | cut -d ' ' -f 2 > ${t}-sample");
        out("  for j in $(seq 1 1 ${end[$t]}); do echo $j; done > ${t}-witness");
        out("  diff -q -w ${t}-sample ${t}-witness;");
        out("  res=$?");
        out("  if [ $res != \"0\" ]; then");
        out("    echo \"FAILED for $t\"");
        out("    exit " + FAILURE_EXIT_CODE);
        out("  fi");
        out("done");
        out("");
        out("exit " + SUCCESSFUL_EXIT_CODE);
    }

    private void printScriptForTimeBased() throws IOException {
        printScriptHeader("TimeBased");
        for (int i = 0; i < NUM_THREADS; i++) {
            out("end[" + i + "]=" + this.runnableArray[i].getCounter());
        }
        out("");
        out("rm aggregated");
        out("cat test* output.log >> aggregated");
        printCommonScriptCore();

    }

    private void printScriptForSizeBased(int numfiles) throws IOException {
        printScriptHeader("SizeBased");

        for (int i = 0; i < NUM_THREADS; i++) {
            out("end[" + i + "]=" + this.runnableArray[i].getCounter());
        }
        out("");
        out("rm aggregated");
        out("for i in $(seq " + (numfiles - 1) + " -1 0); do cat test-$i.log >> aggregated; done");
        out("cat output.log >> aggregated");
        out("");
        printCommonScriptCore();
    }

    private void out(String msg) throws IOException {
        scriptOS.write(msg.getBytes());
        scriptOS.write("\n".getBytes());
    }

    private void executeHarness(int duration, boolean withDelay) throws InterruptedException {
        MultiThreadedHarness multiThreadedHarness = new MultiThreadedHarness(duration);
        this.runnableArray = buildRunnableArray(withDelay);
        multiThreadedHarness.execute(runnableArray);

        StatusChecker checker = new StatusChecker(context.getStatusManager());
        if (!checker.isErrorFree(0)) {
            StatusPrinter.print(context);
            fail("errors reported");
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
