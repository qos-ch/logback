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

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.turbo.NOPTurboFilter;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.helpers.NOPAppender;
import ch.qos.logback.core.testUtil.EnvUtilForTests;

@Ignore
public class LoggerPerfTest {

    static final long NANOS_IN_ONE_SEC = 1000 * 1000 * 1000L;
    static long NORMAL_RUN_LENGTH = 1 * 1000 * 1000;
    static long SHORTENED_RUN_LENGTH = 500 * 1000;

    LoggerContext lc = new LoggerContext();
    Logger lbLogger = lc.getLogger(this.getClass());
    org.slf4j.Logger logger = lbLogger;

    @Before
    public void setUp() throws Exception {
    }

    // ===========================================================================
    @Test
    public void durationOfDisabledLogsWith_1_NOPFilter() {
        final double avg = computeDurationOfDisabledLogsWith_1_NOPFilter(1, NORMAL_RUN_LENGTH);
        System.out.println("durationOfDisabledLogsWith_1_NOPFilter=" + avg);
        @SuppressWarnings("unused")
        final long referencePerf = 60;

        // BogoPerf.assertDuration(avg, referencePerf, CoreConstants.REFERENCE_BIPS);
    }

    double computeDurationOfDisabledLogsWith_1_NOPFilter(final int numOfFilters, final long len) {
        for (int i = 0; i < numOfFilters; i++) {
            lc.addTurboFilter(new NOPTurboFilter());
        }
        lbLogger.setLevel(Level.OFF);
        for (long i = 0; i < len; i++) {
            logger.debug("Toto");
        }

        final long start = System.nanoTime();
        for (long i = 0; i < len; i++) {
            logger.debug("Toto");
        }

        return (System.nanoTime() - start) / len;
    }

    // ===========================================================================
    @Test
    public void durationOfIsDebugEnabled() {
        final double avg = computedurationOfIsDebugEnabled(10 * NORMAL_RUN_LENGTH);
        System.out.println("durationOfIsDebugEnabled=" + avg);

        @SuppressWarnings("unused")
        final long referencePerf = 15;
        // BogoPerf.assertDuration(avg, referencePerf, CoreConstants.REFERENCE_BIPS);
    }

    double computedurationOfIsDebugEnabled(final long len) {
        lbLogger.setLevel(Level.OFF);
        for (long i = 0; i < len; i++) {
            logger.isDebugEnabled();
        }
        Thread.yield();
        for (long i = 0; i < len; i++) {
            logger.isDebugEnabled();
        }

        final long start = System.nanoTime();
        for (long i = 0; i < len; i++) {
            logger.isDebugEnabled();
        }
        return (System.nanoTime() - start) / len;
    }

    // ===========================================================================
    @Test
    public void durationOfDisabledLog_NoParameters() {
        final double avg = computeDurationOfDisabledLog_NoParameters(10 * NORMAL_RUN_LENGTH);
        System.out.println("durationOfDisabledLog_NoParameters=" + avg);

        @SuppressWarnings("unused")
        final long referencePerf = 18;
        // BogoPerf.assertDuration(avg, referencePerf, CoreConstants.REFERENCE_BIPS);
    }

    double computeDurationOfDisabledLog_NoParameters(final long len) {
        lbLogger.setLevel(Level.OFF);
        for (long i = 0; i < len; i++) {
            logger.debug("Toto");
        }
        Thread.yield();
        for (long i = 0; i < len; i++) {
            logger.debug("Toto");
        }
        Thread.yield();

        final long start = System.nanoTime();
        for (long i = 0; i < len; i++) {
            logger.debug("Toto");
        }
        return (System.nanoTime() - start) / len;
    }

    // ===========================================================================

    @Test
    public void durationOfDisabledLog_1_Parameter() {
        final double avgDuration = computeDurationOfDisabledLog_1_Parameter(NORMAL_RUN_LENGTH);
        System.out.println("durationOfDisabledLog_1_Parameter=" + avgDuration);

        @SuppressWarnings("unused")
        final long referencePerf = 30;
        // BogoPerf.assertDuration(avgDuration, referencePerf, CoreConstants.REFERENCE_BIPS);
    }

    double computeDurationOfDisabledLog_1_Parameter(final long len) {
        lbLogger.setLevel(Level.OFF);
        final Object o = new Object();
        for (long i = 0; i < len; i++) {
            logger.debug("Toto {}", o);
        }

        final long start = System.nanoTime();
        for (long i = 0; i < len; i++) {
            logger.debug("Toto {}", o);
        }

        final long end = System.nanoTime();
        return (end - start) / len;
    }

    // ===========================================================================

    @Test
    public void durationOfEnabledLog() {
        if (EnvUtilForTests.isLinux()) {
            // the JIT on Linux behaves very differently
            return;
        }
        final double avgDuration = computeDurationOfEnabledLog(SHORTENED_RUN_LENGTH);
        System.out.println("durationOfEnabledLog=" + avgDuration);

        @SuppressWarnings("unused")
        final long referencePerf = 800;
        // BogoPerf.assertDuration(avgDuration, referencePerf, CoreConstants.REFERENCE_BIPS);
    }

    double computeDurationOfEnabledLog(final long len) {
        lbLogger.setLevel(Level.ALL);

        final NOPAppender<ILoggingEvent> nopAppender = new NOPAppender<>();
        nopAppender.start();
        ((ch.qos.logback.classic.Logger) logger).addAppender(nopAppender);
        for (long i = 0; i < len; i++) {
            logger.debug("Toto");
        }
        final long start = System.nanoTime();
        for (long i = 0; i < len; i++) {
            logger.debug("Toto");
        }
        final long end = System.nanoTime();
        return (end - start) / len;
    }

    // ===========================================================================

    @Test
    public void testThreadedLogging() throws InterruptedException {
        final SleepAppender<ILoggingEvent> appender = new SleepAppender<>();

        final int MILLIS_PER_CALL = 250;
        final int NANOS_PER_CALL = 250 * 1000 * 1000;

        appender.setDuration(MILLIS_PER_CALL);
        appender.start();

        lbLogger.addAppender(appender);
        lbLogger.setLevel(Level.DEBUG);
        long start;
        long end;
        final int threadCount = 10;
        final int iterCount = 5;
        final TestRunner[] threads = new TestRunner[threadCount];
        for (int i = 0; i < threads.length; ++i) {
            threads[i] = new TestRunner(logger, iterCount);
        }
        start = System.nanoTime();
        for (final Thread thread : threads) {
            thread.start();
        }
        for (final TestRunner thread : threads) {
            thread.join();
        }
        end = System.nanoTime();
        final double tolerance = threadCount * .125; // Very little thread contention
        // should occur in this test.
        final double max = (double) NANOS_PER_CALL / NANOS_IN_ONE_SEC * iterCount * tolerance;
        final double serialized = (double) NANOS_PER_CALL / NANOS_IN_ONE_SEC * iterCount * threadCount;
        final double actual = (double) (end - start) / NANOS_IN_ONE_SEC;
        System.out.printf("Sleep duration: %,.4f seconds. Max expected: %,.4f seconds, Serialized: %,.4f\n", actual, max, serialized);
        assertTrue("Exceeded maximum expected time.", actual < max);
    }

    // ============================================================
    private static class TestRunner extends Thread {
        private final org.slf4j.Logger logger;
        private final long len;

        public TestRunner(final org.slf4j.Logger logger, final long len) {
            this.logger = logger;
            this.len = len;
        }

        @Override
        public void run() {
            Thread.yield();
            for (long i = 0; i < len; i++) {
                logger.debug("Toto");
            }
        }
    }

    // ============================================================
    public static class SleepAppender<E> extends UnsynchronizedAppenderBase<E> {
        private static long duration = 500;

        public void setDuration(final long millis) {
            duration = millis;
        }

        @Override
        protected void append(final E eventObject) {
            try {
                Thread.sleep(duration);
            } catch (final InterruptedException ie) {
                // Ignore
            }
        }
    }
    // ============================================================
}
