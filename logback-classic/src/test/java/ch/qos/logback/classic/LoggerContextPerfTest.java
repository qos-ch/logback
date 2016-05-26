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
package ch.qos.logback.classic;

import ch.qos.logback.classic.corpus.CorpusModel;
import ch.qos.logback.core.contention.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.fail;

public class LoggerContextPerfTest {

    static int THREAD_COUNT = 10000;
    int totalTestDuration = 4000;

    LoggerContext loggerContext = new LoggerContext();

    ThreadedThroughputCalculator harness = new ThreadedThroughputCalculator(totalTestDuration);
    RunnableWithCounterAndDone[] runnableArray = buildRunnableArray();

    CorpusModel corpusMaker;

    @Before
    public void setUp() throws Exception {
    }

    private RunnableWithCounterAndDone[] buildRunnableArray() {
        RunnableWithCounterAndDone[] runnableArray = new RunnableWithCounterAndDone[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            runnableArray[i] = new GetLoggerRunnable();
        }
        return runnableArray;
    }

    // Results computed on a Intel i7
    // 1 thread
    // 13'107 ops per milli using Hashtable for LoggerContext.loggerCache
    // 15'258 ops per milli using ConcurrentHashMap for LoggerContext.loggerCache

    // 10 threads
    // 8'468 ops per milli using Hashtable for LoggerContext.loggerCache
    // 58'945 ops per milli using ConcurrentHashMap for LoggerContext.loggerCache

    // 100 threads
    // 8'863 ops per milli using Hashtable for LoggerContext.loggerCache
    // 34'810 ops per milli using ConcurrentHashMap for LoggerContext.loggerCache

    // 1'000 threads
    // 8'188 ops per milli using Hashtable for LoggerContext.loggerCache
    // 24'012 ops per milli using ConcurrentHashMap for LoggerContext.loggerCache

    // 10'000 threads
    // 7'595 ops per milli using Hashtable for LoggerContext.loggerCache
    // 8'989 ops per milli using ConcurrentHashMap for LoggerContext.loggerCache

    @Test
    public void computeResults() throws InterruptedException {
        harness.execute(runnableArray);
        harness.printThroughput("getLogger performance: ", true);
    }

    private class GetLoggerRunnable extends RunnableWithCounterAndDone {

        final int burstLength = 3;

        public void run() {
            while (!isDone()) {
                long i = counter % burstLength;

                loggerContext.getLogger("a" + i);
                counter++;
                if (i == 0) {
                    Thread.yield();
                }
            }
        }
    }
}
