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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.qos.logback.core.contention.RunnableWithCounterAndDone;
import ch.qos.logback.core.encoder.EchoEncoder;
import ch.qos.logback.core.recovery.RecoveryCoordinator;
import ch.qos.logback.core.recovery.RecoveryListener;
import ch.qos.logback.core.recovery.ResilientFileOutputStream;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.ResilienceUtil;

public class FileAppenderResilienceTest implements RecoveryListener {

    FileAppender<Object> fa = new FileAppender<Object>();
    
    ResilientFileOutputStream resilientFOS;
    
    Context context = new ContextBase();
    int diff = RandomUtil.getPositiveInt();
    String outputDirStr = CoreTestConstants.OUTPUT_DIR_PREFIX + "resilience-" + diff + "/";

    // String outputDirStr = "\\\\192.168.1.3\\lbtest\\" + "resilience-"+ diff +
    // "/";;
    String logfileStr = outputDirStr + "output.log";

    boolean failedState = false;

    int recoveryCounter = 0;
    int failureCounter = 0;
    
    
    @BeforeEach
    public void setUp() throws InterruptedException {

        context.getStatusManager().add(new OnConsoleStatusListener());

        File outputDir = new File(outputDirStr);
        outputDir.mkdirs();

        fa.setContext(context);
        fa.setName("FILE");
        fa.setEncoder(new EchoEncoder<Object>());
        fa.setFile(logfileStr);
        fa.start();
        resilientFOS = (ResilientFileOutputStream) fa.getOutputStream();
        resilientFOS.addRecoveryListener(this);
        
    }

    @Test
    @Disabled
    public void manual() throws InterruptedException, IOException {
        Runner runner = new Runner(fa);
        Thread t = new Thread(runner);
        t.start();

        while (true) {
            Thread.sleep(110);
        }
    }

    @Test
    public void smoke() throws InterruptedException, IOException {
        Runner runner = new Runner(fa);
        Thread t = new Thread(runner);
        t.start();

        double delayCoefficient = 2.0;
        for (int i = 0; i < 5; i++) {
            Thread.sleep((int) (RecoveryCoordinator.BACKOFF_COEFFICIENT_MIN * delayCoefficient));
            closeLogFileOnPurpose();
        }
        runner.setDone(true);
        t.join();

        double bestCaseSuccessRatio = 1 / delayCoefficient;
        // expect to lose at most 35% of the events
        double lossinessFactor = 0.35;
        double resilianceFactor = (1 - lossinessFactor);

        Assertions.assertTrue(recoveryCounter > 0, "at least one recovery should have occured");
        Assertions.assertTrue(failureCounter > 0, "at least one failure should have occured");

        System.out.println("recoveryCounter=" + recoveryCounter);
        System.out.println("failureCounter=" + failureCounter);



        String errmsg0 = "failureCounter="+failureCounter+" must be greater or equal to recoveryCounter="+recoveryCounter;
        Assertions.assertTrue(failureCounter >= recoveryCounter, errmsg0);

        String errmsg1 = "Difference between failureCounter="+failureCounter+" and recoveryCounter="+recoveryCounter+" should not exceeed 1";
        Assertions.assertTrue(failureCounter - recoveryCounter <= 1, errmsg1);



        int actuallyWritten = ResilienceUtil.countLines(logfileStr, "^hello (\\d{1,5})$");
        long exptectedWrites = runner.getCounter()-recoveryCounter;
        Assertions.assertEquals(exptectedWrites, actuallyWritten);
    }

    private void closeLogFileOnPurpose() throws IOException {
        ResilientFileOutputStream resilientFOS = (ResilientFileOutputStream) fa.getOutputStream();
        FileChannel fileChannel = resilientFOS.getChannel();
        fileChannel.close();
    }

    @Override
    public void newFailure(IOException e) {
        failedState = true;
        failureCounter++;
        
    }

    @Override
    public void recoveryOccured() {
        failedState = false;
        recoveryCounter++;
    }
    
    class Runner extends RunnableWithCounterAndDone {
        FileAppender<Object> fa;

        Runner(FileAppender<Object> fa) {
            this.fa = fa;
        }

        public void run() {
            while (!isDone()) {
                fa.doAppend("hello " + counter);
                if(!FileAppenderResilienceTest.this.failedState) { 
                    counter++;
                }
                if (counter % 128 == 0) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

    }
}

