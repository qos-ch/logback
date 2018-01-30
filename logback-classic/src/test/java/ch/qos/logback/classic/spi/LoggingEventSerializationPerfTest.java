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
package ch.qos.logback.classic.spi;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.ObjectOutputStream;

import ch.qos.logback.core.testUtil.EnvUtilForTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import ch.qos.logback.classic.net.NOPOutputStream;
import ch.qos.logback.classic.net.testObjectBuilders.Builder;
import ch.qos.logback.classic.net.testObjectBuilders.LoggingEventWithParametersBuilder;
import ch.qos.logback.classic.net.testObjectBuilders.TrivialLoggingEventBuilder;
import ch.qos.logback.core.CoreConstants;

// As of logback 0.9.15, 
//   average time  per logging event: 3979 nanoseconds
//   size 545'648 bytes
// 
// Using LoggingEventDO
// 
//   average time  per logging event: 4052 nanoseconds
//   average size=45,  with params, average size=136
//
// Using LoggerEventVO, with loggerName, and loggerContextRemoteView
//   average time per logging event: 4034
//   average size 57, with params, average size=148

public class LoggingEventSerializationPerfTest {

    static int LOOP_LEN = 10 * 1000;

    NOPOutputStream noos = new NOPOutputStream();
    ObjectOutputStream oos;

    @Before
    public void setUp() throws Exception {
        MDC.clear();
        oos = new ObjectOutputStream(noos);
    }

    @After
    public void tearDown() throws Exception {
    }

    double doLoop(Builder<LoggingEvent> builder, int loopLen) {
        long start = System.nanoTime();
        int resetCounter = 0;
        for (int i = 0; i < loopLen; i++) {
            try {
                ILoggingEvent le = builder.build(i);
                oos.writeObject(LoggingEventVO.build(le));

                oos.flush();
                if (++resetCounter >= CoreConstants.OOS_RESET_FREQUENCY) {
                    oos.reset();
                    resetCounter = 0;
                }

            } catch (IOException ex) {
                fail(ex.getMessage());
            }
        }
        long end = System.nanoTime();
        return (end - start) / (1.0d * loopLen);
    }

    @Test
    public void testPerformance() {
        if (EnvUtilForTests.isLinux()) {
            return;
        }
        TrivialLoggingEventBuilder builder = new TrivialLoggingEventBuilder();

        for (int i = 0; i < 3; i++) {
            doLoop(builder, LOOP_LEN);
            noos.reset();
        }
        double rt = doLoop(builder, LOOP_LEN);
        System.out.println("average time per logging event " + rt + " nanoseconds");

        long averageSize = (long) (noos.size() / (LOOP_LEN));
        System.out.println("noos size " + noos.size() + " average size=" + averageSize);
        double averageSizeLimit = 62.1;

        assertTrue("average size " + averageSize + " should be less than " + averageSizeLimit, averageSizeLimit > averageSize);

        // the reference was computed on Orion (Ceki's computer)
        @SuppressWarnings("unused")
        long referencePerf = 5000;
        //BogoPerf.assertDuration(rt, referencePerf, CoreConstants.REFERENCE_BIPS);
    }

    @Test
    public void testPerformanceWithParameters() {
        if (EnvUtilForTests.isLinux()) {
            return;
        }
        LoggingEventWithParametersBuilder builder = new LoggingEventWithParametersBuilder();

        // warm up
        for (int i = 0; i < 3; i++) {
            doLoop(builder, LOOP_LEN);
            noos.reset();
        }
        @SuppressWarnings("unused")
        double rt = doLoop(builder, LOOP_LEN);
        long averageSize = (long) (noos.size() / (LOOP_LEN));

        System.out.println("noos size " + noos.size() + " average size=" + averageSize);

        double averageSizeLimit = 160;
        assertTrue("averageSize " + averageSize + " should be less than " + averageSizeLimit, averageSizeLimit > averageSize);

        // the reference was computed on Orion (Ceki's computer)
        @SuppressWarnings("unused")
        long referencePerf = 7000;
        //BogoPerf.assertDuration(rt, referencePerf, CoreConstants.REFERENCE_BIPS);
    }
}
