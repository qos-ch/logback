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
package ch.qos.logback.core;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.qos.logback.core.contention.RunnableWithCounterAndDone;
import ch.qos.logback.core.encoder.EchoEncoder;
import ch.qos.logback.core.recovery.RecoveryCoordinator;
import ch.qos.logback.core.recovery.ResilientFileOutputStream;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.ResilienceUtil;

public class FileAppenderResilienceTest {

    FileAppender<Object> fa = new FileAppender<>();
    Context context = new ContextBase();
    int diff = RandomUtil.getPositiveInt();
    String outputDirStr = CoreTestConstants.OUTPUT_DIR_PREFIX + "resilience-" + diff + "/";

    // String outputDirStr = "\\\\192.168.1.3\\lbtest\\" + "resilience-"+ diff +
    // "/";;
    String logfileStr = outputDirStr + "output.log";

    @Before
    public void setUp() throws InterruptedException {

        context.getStatusManager().add(new OnConsoleStatusListener());

        final File outputDir = new File(outputDirStr);
        outputDir.mkdirs();

        fa.setContext(context);
        fa.setName("FILE");
        fa.setEncoder(new EchoEncoder<>());
        fa.setFile(logfileStr);
        fa.start();
    }

    @Test
    @Ignore
    public void manual() throws InterruptedException, IOException {
        final Runner runner = new Runner(fa);
        final Thread t = new Thread(runner);
        t.start();

        while (true) {
            Thread.sleep(110);
        }
    }

    @Test
    public void smoke() throws InterruptedException, IOException {
        final Runner runner = new Runner(fa);
        final Thread t = new Thread(runner);
        t.start();

        final double delayCoefficient = 2.0;
        for (int i = 0; i < 5; i++) {
            Thread.sleep((int) (RecoveryCoordinator.BACKOFF_COEFFICIENT_MIN * delayCoefficient));
            closeLogFileOnPurpose();
        }
        runner.setDone(true);
        t.join();

        final double bestCaseSuccessRatio = 1 / delayCoefficient;
        // expect to loose at most 35% of the events
        final double lossinessFactor = 0.35;
        final double resilianceFactor = 1 - lossinessFactor;

        ResilienceUtil.verify(logfileStr, "^hello (\\d{1,5})$", runner.getCounter(), bestCaseSuccessRatio * resilianceFactor);
    }

    private void closeLogFileOnPurpose() throws IOException {
        final ResilientFileOutputStream resilientFOS = (ResilientFileOutputStream) fa.getOutputStream();
        final FileChannel fileChannel = resilientFOS.getChannel();
        fileChannel.close();
    }
}

class Runner extends RunnableWithCounterAndDone {
    FileAppender<Object> fa;

    Runner(final FileAppender<Object> fa) {
        this.fa = fa;
    }

    @Override
    public void run() {
        while (!isDone()) {
            counter++;
            fa.doAppend("hello " + counter);
            if (counter % 128 == 0) {
                try {
                    Thread.sleep(10);
                } catch (final InterruptedException e) {
                }
            }
        }
    }

}