/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.helpers.BogoPerf;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.turbo.NOPTurboFilter;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.appender.NOPAppender;
import ch.qos.logback.core.testUtil.Env;

public class LoggerPerfTest {

  long NANOS_IN_ONE_SEC = 1000*1000*1000L;
  static long NORMAL_RUN_LENGTH = 1000 * 1000;
  static long SHORTENED_RUN_LENGTH = 500 * 1000;
  
  
  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testDurationOfDisabledLogWithStraightStringParameter() {
    computeDurationOfDisabledLogWithStraightStringParameter(NORMAL_RUN_LENGTH);
    double avg = computeDurationOfDisabledLogWithStraightStringParameter(NORMAL_RUN_LENGTH);

    long referencePerf = 17;
    BogoPerf.assertDuration(avg, referencePerf, CoreConstants.REFERENCE_BIPS);
  }

  double computeDurationOfDisabledLogWithStraightStringParameter(long len) {
    LoggerContext lc = new LoggerContext();
    Logger logger = lc.getLogger(this.getClass());
    logger.setLevel(Level.OFF);
    for (long i = 0; i < len; i++) {
      logger.debug("Toto");
    }
    long start = System.nanoTime();
    for (long i = 0; i < len; i++) {
      logger.debug("Toto");
    }
    long end = System.nanoTime();
    return (end - start) / len;
  }

  @Test
  public void testDurationOfDisabledLogWithParameters() {
    computeDurationOfDisabledLogWithParameters(NORMAL_RUN_LENGTH);
    double avgDuration = computeDurationOfDisabledLogWithParameters(NORMAL_RUN_LENGTH);
    long referencePerf = 36;
    BogoPerf.assertDuration(avgDuration, referencePerf, CoreConstants.REFERENCE_BIPS);
  }
  
  double computeDurationOfDisabledLogWithParameters(long len) {
    LoggerContext lc = new LoggerContext();
    Logger logger = lc.getLogger(this.getClass());
    logger.setLevel(Level.OFF);
    for (long i = 0; i < len; i++) {
      logger.debug("Toto {}", i);
    }
    long start = System.nanoTime();
    for (long i = 0; i < len; i++) {
      logger.debug("Toto {}", i);
    }
    long end = System.nanoTime();
    return (end - start) / len;
  }

  @Test
  public void testDurationOfEnabledLog() {
    if(Env.isLinux()) {
      // the JIT on Linux behaves very differently
      return;
    }
    computeDurationOfEnabledLog(SHORTENED_RUN_LENGTH);
    double avgDuration = computeDurationOfEnabledLog(SHORTENED_RUN_LENGTH);
    long referencePerf = 500;
    BogoPerf.assertDuration(avgDuration, referencePerf, CoreConstants.REFERENCE_BIPS);
  }
  
  double computeDurationOfEnabledLog(long len) {
    LoggerContext lc = new LoggerContext();
    Logger logger = lc.getLogger(this.getClass());
    logger.setLevel(Level.ALL);
    NOPAppender<LoggingEvent> nopAppender = new NOPAppender<LoggingEvent>();
    nopAppender.start();
    logger.addAppender(nopAppender);
    for (long i = 0; i < len; i++) {
      logger.debug("Toto");
    }
    long start = System.nanoTime();
    for (long i = 0; i < len; i++) {
      logger.debug("Toto");
    }
    long end = System.nanoTime();
    return (end - start) / len;
  }

  @Test
  public void testComputeDurationOfDisabledLogsWithNOPFilter() {
    computeDurationOfDisabledLogsWithNOPFilter(NORMAL_RUN_LENGTH);
    double avg = computeDurationOfDisabledLogsWithNOPFilter(NORMAL_RUN_LENGTH);
    long referencePerf = 48;
    BogoPerf.assertDuration(avg, referencePerf, CoreConstants.REFERENCE_BIPS);
  }

  double computeDurationOfDisabledLogsWithNOPFilter(long len) {
    LoggerContext lc = new LoggerContext();
    NOPAppender<LoggingEvent> mopAppender = new NOPAppender<LoggingEvent>();
    NOPTurboFilter nopFilter = new NOPTurboFilter();
    nopFilter.setName("nop");
    mopAppender.start();
    lc.addTurboFilter(nopFilter);
    Logger logger = lc.getLogger(this.getClass());
    logger.setLevel(Level.OFF);
    for (long i = 0; i < len; i++) {
      logger.debug("Toto");
    }
    long start = System.nanoTime();
    for (long i = 0; i < len; i++) {
      logger.debug("Toto");
    }
    long end = System.nanoTime();
    return (end - start) / len;
  }

  @Test
  public void testThreadedLogging() throws InterruptedException {
    LoggerContext lc = new LoggerContext();
    SleepAppender<LoggingEvent> appender = new SleepAppender<LoggingEvent>();
    
    int MILLIS_PER_CALL = 250;
    int NANOS_PER_CALL = 250*1000*1000;;
    appender.setDuration(MILLIS_PER_CALL);
    appender.start();
    Logger logger = lc.getLogger(this.getClass());
    logger.addAppender(appender);
    logger.setLevel(Level.DEBUG);
    long start;
    long end;
    int threadCount = 10;
    int iterCount = 5;
    TestRunner[] threads = new TestRunner[threadCount];
    for (int i = 0; i < threads.length; ++i) {
      threads[i] = new TestRunner(logger, iterCount);
    }
    start = System.nanoTime();
    for (Thread thread : threads) {
      thread.start();
    }
    for (TestRunner thread : threads) {
      thread.join();
    }
    end = System.nanoTime();
    double tolerance = threadCount * .125; // Very little thread contention
    // should occur in this test.
    double max = ((((double) NANOS_PER_CALL) / NANOS_IN_ONE_SEC) * iterCount) * tolerance;
    double serialized = (((double) NANOS_PER_CALL) / NANOS_IN_ONE_SEC) * iterCount
        * threadCount;
    double actual = ((double) (end - start)) / NANOS_IN_ONE_SEC;
    System.out
        .printf(
            "Sleep duration: %,.4f seconds. Max expected: %,.4f seconds, Serialized: %,.4f\n",
            actual, max, serialized);
    assertTrue("Exceeded maximum expected time.", actual < max);
  }

  // ============================================================
  private static class TestRunner extends Thread {
    private Logger logger;
    private long len;

    public TestRunner(Logger logger, long len) {
      this.logger = logger;
      this.len = len;
    }

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

    public void setDuration(long millis) {
      duration = millis;
    }

    @Override
    protected void append(E eventObject) {
      try {
        Thread.sleep(duration);
      } catch (InterruptedException ie) {
        // Ignore
      }
    }
  }
  // ============================================================
}
