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
package ch.qos.logback.classic.turbo;

import java.io.File;
import java.io.IOException;

import ch.qos.logback.core.testUtil.EnvUtilForTests;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.issue.lbclassic135.LoggingRunnable;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.contention.MultiThreadedHarness;
import ch.qos.logback.core.contention.RunnableWithCounterAndDone;
import ch.qos.logback.core.joran.spi.JoranException;

@Ignore
public class ReconfigurePerf {
    final static int THREAD_COUNT = 500;
    // final static int LOOP_LEN = 1000 * 1000;

    // the space in the file name mandated by
    // http://jira.qos.ch/browse/LBCORE-119
    final static String CONF_FILE_AS_STR = ClassicTestConstants.INPUT_PREFIX + "turbo/scan_perf.xml";

    // it actually takes time for Windows to propagate file modification changes
    // values below 100 milliseconds can be problematic the same propagation
    // latency occurs in Linux but is even larger (>600 ms)
    final static int DEFAULT_SLEEP_BETWEEN_UPDATES = 110;

    int sleepBetweenUpdates = DEFAULT_SLEEP_BETWEEN_UPDATES;

    static int numberOfCycles = 100;
    static int totalTestDuration;

    LoggerContext loggerContext = new LoggerContext();
    Logger logger = loggerContext.getLogger(this.getClass());
    MultiThreadedHarness harness;

    @Before
    public void setUp() {
        // take into account propagation latency occurs on Linux
        if (EnvUtilForTests.isLinux()) {
            sleepBetweenUpdates = 850;
            totalTestDuration = sleepBetweenUpdates * numberOfCycles;
        } else {
            totalTestDuration = sleepBetweenUpdates * numberOfCycles * 2;
        }
        harness = new MultiThreadedHarness(totalTestDuration);
    }

    void configure(File file) throws JoranException {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        jc.doConfigure(file);
    }

    RunnableWithCounterAndDone[] buildRunnableArray() {
        RunnableWithCounterAndDone[] rArray = new RunnableWithCounterAndDone[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            rArray[i] = new LoggingRunnable(logger);
        }
        return rArray;
    }

    // Tests whether ConfigurationAction is installing ReconfigureOnChangeFilter
    @Test
    public void scan1() throws JoranException, IOException, InterruptedException {
        File file = new File(CONF_FILE_AS_STR);
        configure(file);
        System.out.println("Running scan1()");
        doRun();
    }

    void doRun() throws InterruptedException {
        RunnableWithCounterAndDone[] runnableArray = buildRunnableArray();
        harness.execute(runnableArray);
    }
}
