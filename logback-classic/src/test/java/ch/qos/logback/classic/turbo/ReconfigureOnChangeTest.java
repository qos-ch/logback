/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.helpers.BogoPerf;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.issue.lbclassic135.LoggingRunnable;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.TeztConstants;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.contention.MultiThreadedHarness;
import ch.qos.logback.core.contention.RunnableWithCounterAndDone;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.testUtil.Env;
import ch.qos.logback.core.util.StatusPrinter;

public class ReconfigureOnChangeTest {
  final static int THREAD_COUNT = 5;
  final static int LOOP_LEN = 1000 * 1000;

  final static String SCAN1_FILE_AS_STR = TeztConstants.TEST_DIR_PREFIX
      + "input/turbo/scan1.xml";

  // it actually takes time for Windows to propagate file modification changes
  // values below 100 milliseconds can be problematic
  // the same propagation latency occurs in Linux but is even larger (>600 ms)
  static int SLEEP_BETWEEN_UPDATES = 250;

  // we won't at least 10 re-configurations
  static int TOTAL_TEST_DURATION = SLEEP_BETWEEN_UPDATES * 10;

  LoggerContext loggerContext = new LoggerContext();
  Logger logger = loggerContext.getLogger(this.getClass());
  MultiThreadedHarness harness = new MultiThreadedHarness(TOTAL_TEST_DURATION);

  @Before
  public void setUp() {
    // take into account propagation latency occurs on Linux
    if (Env.isLinux()) {
      SLEEP_BETWEEN_UPDATES = 850;
    }

  }

  void configure(String file) throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(loggerContext);
    jc.doConfigure(file);
  }

  RunnableWithCounterAndDone[] buildRunnableArray(File configFile) {

    RunnableWithCounterAndDone[] rArray = new RunnableWithCounterAndDone[THREAD_COUNT];
    rArray[0] = new Updater(configFile);
    for (int i = 1; i < THREAD_COUNT; i++) {
      rArray[i] = new LoggingRunnable(logger);
    }
    return rArray;
  }

  // Tests whether ConfigurationAction is installing ReconfigureOnChangeFilter
  @Test
  public void scan1() throws JoranException, IOException, InterruptedException {
    configure(SCAN1_FILE_AS_STR);
    File file = new File(SCAN1_FILE_AS_STR);
    RunnableWithCounterAndDone[] runnableArray = buildRunnableArray(file);
    harness.execute(runnableArray);

    loggerContext.getStatusManager().add(
        new InfoStatus("end of execution ", this));

    long expectedRreconfigurations = runnableArray[0].getCounter();

    StatusChecker checker = new StatusChecker(loggerContext);
    try {
      assertTrue(checker.isErrorFree());
      int effectiveResets = checker
          .matchCount("Resetting and reconfiguring context");
      // the number of effective resets must be equal or less than
      // expectedRreconfigurations
      assertTrue(effectiveResets <= expectedRreconfigurations);
      // however, there should be some effective resets
      String failMsg = "effective=" + effectiveResets + ", expected="
          + expectedRreconfigurations;
      assertTrue(failMsg,
          (effectiveResets * 1.3) >= (expectedRreconfigurations * 1.0));
    } catch (AssertionError ae) {
      StatusPrinter.print(loggerContext);
    }
  }

  ReconfigureOnChangeFilter initROCF() throws MalformedURLException {
    ReconfigureOnChangeFilter rocf = new ReconfigureOnChangeFilter();
    rocf.setContext(loggerContext);
    File file = new File(SCAN1_FILE_AS_STR);
    loggerContext.putObject(CoreConstants.URL_OF_LAST_CONFIGURATION_VIA_JORAN,
        file.toURI().toURL());
    rocf.start();
    return rocf;
  }

  @Test
  public void directPerfTest() throws MalformedURLException {
    if (Env.isLinux()) {
      // for some reason this test does not pass on Linux (AMD 64 bit, Dual Core
      // Opteron 170)
      return;
    }

    ReconfigureOnChangeFilter rocf = initROCF();
    assertTrue(rocf.isStarted());

    for (int i = 0; i < 30; i++) {
      directLoop(rocf);
    }
    double avg = directLoop(rocf);
    System.out.println("directPerfTest: " + avg);
    // the reference was computed on Orion (Ceki's computer)
    long referencePerf = 18;
    BogoPerf.assertDuration(avg, referencePerf, CoreConstants.REFERENCE_BIPS);
  }

  public double directLoop(ReconfigureOnChangeFilter rocf) {
    long start = System.nanoTime();
    for (int i = 0; i < LOOP_LEN; i++) {
      rocf.decide(null, logger, Level.DEBUG, " ", null, null);
    }
    long end = System.nanoTime();
    return (end - start) / (1.0d * LOOP_LEN);
  }

  @Test
  public void indirectPerfTest() throws MalformedURLException {
    if (Env.isLinux()) {
      // for some reason this test does not pass on Linux (AMD 64 bit, Dual Core
      // Opteron 170)
      return;
    }

    ReconfigureOnChangeFilter rocf = initROCF();
    assertTrue(rocf.isStarted());
    loggerContext.addTurboFilter(rocf);
    logger.setLevel(Level.ERROR);

    indirectLoop();
    double avg = indirectLoop();
    System.out.println(avg);
    // the reference was computed on Orion (Ceki's computer)
    long referencePerf = 68;
    BogoPerf.assertDuration(avg, referencePerf, CoreConstants.REFERENCE_BIPS);
  }

  void addInfo(String msg, Object o) {
    loggerContext.getStatusManager().add(new InfoStatus(msg, o));
  }

  public double indirectLoop() {
    long start = System.nanoTime();
    for (int i = 0; i < LOOP_LEN; i++) {
      logger.debug("hello");
    }
    long end = System.nanoTime();
    return (end - start) / (1.0d * LOOP_LEN);
  }

  class Updater extends RunnableWithCounterAndDone {
    File configFile;

    Updater(File configFile) {
      this.configFile = configFile;
    }

    public void run() {
      while (!isDone()) {
        try {
          Thread.sleep(SLEEP_BETWEEN_UPDATES);
        } catch (InterruptedException e) {
        }
        if (isDone()) {
          return;
        }
        counter++;
        ReconfigureOnChangeTest.this.addInfo("***settting last modified", this);
        configFile.setLastModified(System.currentTimeMillis());
      }
    }
  }

}
