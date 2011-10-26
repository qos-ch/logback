/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.net.MalformedURLException;
import java.util.concurrent.ThreadPoolExecutor;

import ch.qos.logback.classic.gaffer.GafferConfigurator;
import ch.qos.logback.core.contention.AbstractMultiThreadedHarness;
import ch.qos.logback.core.contention.MultiThreadedHarness;
import ch.qos.logback.core.contention.WaitOnExecutionMultiThreadedHarness;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.helpers.BogoPerf;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.issue.lbclassic135.LoggingRunnable;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.contention.RunnableWithCounterAndDone;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.testUtil.Env;
import ch.qos.logback.core.util.StatusPrinter;

public class ReconfigureOnChangeTest {
  final static int THREAD_COUNT = 5;
  final static int LOOP_LEN = 1000 * 1000;

  static final boolean MUST_BE_ERROR_FREE = true;
  static final boolean ERRORS_EXPECTED = false;

  static final boolean REGULAR_RECONFIGURATION = false;
  static final boolean FORCED_RECONFIGURATION_SKIP = true;


  int diff = RandomUtil.getPositiveInt();

  // the space in the file name mandated by
  // http://jira.qos.ch/browse/LBCORE-119
  final static String SCAN1_FILE_AS_STR = ClassicTestConstants.INPUT_PREFIX
          + "turbo/scan 1.xml";

  final static String G_SCAN1_FILE_AS_STR = ClassicTestConstants.INPUT_PREFIX
          + "turbo/scan 1.groovy";

  final static String SCAN_LBCLASSIC_154_FILE_AS_STR = ClassicTestConstants.INPUT_PREFIX
          + "turbo/scan_lbclassic154.xml";

  final static String INCLUSION_SCAN_TOPLEVEL0_AS_STR = ClassicTestConstants.INPUT_PREFIX
          + "turbo/inclusion/topLevel0.xml";

  final static String INCLUSION_SCAN_TOPLEVEL2_AS_STR = ClassicTestConstants.INPUT_PREFIX
          + "turbo/inclusion/topLevel2.xml";

  final static String INCLUSION_SCAN_TOP_BY_RESOURCE_AS_STR = ClassicTestConstants.INPUT_PREFIX
          + "turbo/inclusion/topByResource.xml";


  final static String INCLUSION_SCAN_INNER0_AS_STR = ClassicTestConstants.INPUT_PREFIX
          + "turbo/inclusion/inner0.xml";
  final static String INCLUSION_SCAN_INNER2_AS_STR = ClassicTestConstants.INPUT_PREFIX
          + "turbo/inclusion/inner2.xml";


  final static String INCLUSION_SCAN_INNER1_AS_STR = "target/test-classes/asResource/inner1.xml";

  // it actually takes time for Windows to propagate file modification changes
  // values below 100 milliseconds can be problematic the same propagation
  // latency occurs in Linux but is even larger (>600 ms)
  final static int DEFAULT_SLEEP_BETWEEN_UPDATES = 60;

  int sleepBetweenUpdates = 100;//DEFAULT_SLEEP_BETWEEN_UPDATES;

//  static int totalTestDuration;

  LoggerContext loggerContext = new LoggerContext();
  Logger logger = loggerContext.getLogger(this.getClass());
  AbstractMultiThreadedHarness harness;

  ThreadPoolExecutor executor = (ThreadPoolExecutor) loggerContext.getExecutorService();

  int expectedResets = 2;

  @Before
  public void setUp() {
    harness = new WaitOnExecutionMultiThreadedHarness(executor, expectedResets);
  }

  @After
  public void tearDown() {
  }

  void configure(File file) throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(loggerContext);
    jc.doConfigure(file);
  }

  void configure(InputStream is) throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(loggerContext);
    jc.doConfigure(is);
  }

  void gConfigure(File file) throws JoranException {
    GafferConfigurator gc = new GafferConfigurator(loggerContext);
    gc.run(file);
  }

  RunnableWithCounterAndDone[] buildRunnableArray(File configFile, UpdateType updateType) {
    RunnableWithCounterAndDone[] rArray = new RunnableWithCounterAndDone[THREAD_COUNT];
    rArray[0] = new Updater(configFile, updateType);
    for (int i = 1; i < THREAD_COUNT; i++) {
      rArray[i] = new LoggingRunnable(logger);
    }
    return rArray;
  }


  void doScanTest(File fileToTouch) throws JoranException, IOException, InterruptedException {
    doScanTest(fileToTouch, UpdateType.TOUCH, REGULAR_RECONFIGURATION, MUST_BE_ERROR_FREE);
  }

  void doScanTest(File fileToTouch, UpdateType updateType, boolean forcedReconfigurationSkip, boolean mustBeErrorFree) throws JoranException, IOException, InterruptedException {
    RunnableWithCounterAndDone[] runnableArray = buildRunnableArray(fileToTouch, updateType);
    harness.execute(runnableArray);

    loggerContext.getStatusManager().add(
            new InfoStatus("end of execution ", this));

    int expected = expectedResets;
    if (forcedReconfigurationSkip)
      expected = 0;

    verify(expected, mustBeErrorFree);
  }

  // Tests whether ConfigurationAction is installing ReconfigureOnChangeFilter
  @Test(timeout = 4000L)
  public void scan1() throws JoranException, IOException, InterruptedException {
    File file = new File(SCAN1_FILE_AS_STR);
    configure(file);
    doScanTest(file);
  }

  @Test(timeout = 4000L)
  public void scanWithFileInclusion() throws JoranException, IOException, InterruptedException {
    File topLevelFile = new File(INCLUSION_SCAN_TOPLEVEL0_AS_STR);
    File innerFile = new File(INCLUSION_SCAN_INNER0_AS_STR);
    configure(topLevelFile);
    doScanTest(innerFile);
  }

  @Test(timeout = 4000L)
  public void scanWithResourceInclusion() throws JoranException, IOException, InterruptedException {
    File topLevelFile = new File(INCLUSION_SCAN_TOP_BY_RESOURCE_AS_STR);
    File innerFile = new File(INCLUSION_SCAN_INNER1_AS_STR);
    configure(topLevelFile);
    doScanTest(innerFile);
  }

  // See also http://jira.qos.ch/browse/LBCLASSIC-247
  @Test(timeout = 4000L)
  public void includeScanViaInputStreamSuppliedConfigFile() throws IOException, JoranException, InterruptedException {
    harness = new MultiThreadedHarness(1000);
    String configurationStr = "<configuration scan=\"true\" scanPeriod=\"50 millisecond\"><include resource=\"asResource/inner1.xml\"/></configuration>";
    configure(new ByteArrayInputStream(configurationStr.getBytes("UTF-8")));
    File innerFile = new File(INCLUSION_SCAN_INNER1_AS_STR);
    doScanTest(innerFile, UpdateType.TOUCH, FORCED_RECONFIGURATION_SKIP, MUST_BE_ERROR_FREE);
  }

  @Test(timeout = 4000L)
  public void fallbackToSafe() throws IOException, JoranException, InterruptedException {
    String path = CoreTestConstants.OUTPUT_DIR_PREFIX + "reconfigureOnChangeConfig_fallbackToSafe-" + diff + ".xml";
    File file = new File(path);
    writeToFile(file, "<configuration scan=\"true\" scanPeriod=\"50 millisecond\"><root level=\"ERROR\"/></configuration> ");
    configure(file);
    doScanTest(file, UpdateType.MALFORMED, REGULAR_RECONFIGURATION, ERRORS_EXPECTED);
  }

  @Test(timeout = 4000L)
  public void fallbackToSafeWithIncludedFile() throws IOException, JoranException, InterruptedException {
    String topLevelFileAsStr = CoreTestConstants.OUTPUT_DIR_PREFIX + "reconfigureOnChangeConfig_top-" + diff + ".xml";
    String innerFileAsStr = CoreTestConstants.OUTPUT_DIR_PREFIX + "reconfigureOnChangeConfig_inner-" + diff + ".xml";
    File topLevelFile = new File(topLevelFileAsStr);
    writeToFile(topLevelFile, "<configuration scan=\"true\" scanPeriod=\"50 millisecond\"><include file=\""+innerFileAsStr+"\"/></configuration> ");

    File innerFile = new File(innerFileAsStr);
    writeToFile(innerFile, "<included><root level=\"ERROR\"/></included> ");
    configure(topLevelFile);
    doScanTest(innerFile, UpdateType.MALFORMED_INNER, REGULAR_RECONFIGURATION, ERRORS_EXPECTED);
  }


  @Test(timeout = 4000L)
  public void gscan1() throws JoranException, IOException, InterruptedException {
    File file = new File(G_SCAN1_FILE_AS_STR);
    gConfigure(file);
    RunnableWithCounterAndDone[] runnableArray = buildRunnableArray(file, UpdateType.TOUCH);
    harness.execute(runnableArray);

    loggerContext.getStatusManager().add(
            new InfoStatus("end of execution ", this));

    verify(expectedResets, MUST_BE_ERROR_FREE);
  }

  // check for deadlocks
  @Test(timeout = 4000L)
  public void scan_lbclassic154() throws JoranException, IOException,
          InterruptedException {
    File file = new File(SCAN_LBCLASSIC_154_FILE_AS_STR);
    configure(file);
    RunnableWithCounterAndDone[] runnableArray = buildRunnableArray(file, UpdateType.TOUCH);
    harness.execute(runnableArray);

    loggerContext.getStatusManager().add(
            new InfoStatus("end of execution ", this));

    verify(expectedResets, MUST_BE_ERROR_FREE);
  }


  void verify(int expected, boolean errorFreeness) {
    StatusChecker checker = new StatusChecker(loggerContext);
    StatusPrinter.print(loggerContext);
    if (errorFreeness == MUST_BE_ERROR_FREE) {
      assertTrue(checker.isErrorFree(0));
    } else {
      assertFalse(checker.isErrorFree(0));
    }

    int effectiveResets = checker
            .matchCount("Will reset and reconfigure context");

    String failMsg = "effective=" + effectiveResets + ", expected="
            + expected;

    // there might be more effective resets than the expected amount
    // since the harness may be sleeping while a reset occurs
    assertTrue(failMsg, expected <= effectiveResets && (expected +2) >= effectiveResets);

  }

  ReconfigureOnChangeFilter initROCF() throws MalformedURLException {
    ReconfigureOnChangeFilter rocf = new ReconfigureOnChangeFilter();
    rocf.setContext(loggerContext);
    File file = new File(SCAN1_FILE_AS_STR);
    ConfigurationWatchListUtil.setMainWatchURL(loggerContext, file.toURI().toURL());
    rocf.start();
    return rocf;
  }

  @Test
  public void directPerfTest() throws MalformedURLException {
    if (Env.isLinux()) {
      // for some reason this test does not pass on Linux (AMD 64 bit,
      // Dual Core Opteron 170)
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
      // for some reason this test does not pass on Linux (AMD 64 bit,
      // Dual Core
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

  enum UpdateType {TOUCH, MALFORMED, MALFORMED_INNER}

  void writeToFile(File file, String contents) {
    try {
      FileWriter fw = new FileWriter(file);
      fw.write(contents);
      fw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  class Updater extends RunnableWithCounterAndDone {
    File configFile;
    UpdateType updateType;

    Updater(File configFile, UpdateType updateType) {
      this.configFile = configFile;
      this.updateType = updateType;
    }

    Updater(File configFile) {
      this(configFile, UpdateType.TOUCH);
    }

    public void run() {
      while (!isDone()) {
        try {
          Thread.sleep(sleepBetweenUpdates);
        } catch (InterruptedException e) {
        }
        if (isDone()) {
          return;
        }
        counter++;
        ReconfigureOnChangeTest.this.addInfo("***settting last modified", this);
        switch (updateType) {
          case TOUCH:
            touchFile();
            break;
          case MALFORMED:
            malformedUpdate(counter);
            break;
          case MALFORMED_INNER:
            malformedInnerUpdate(counter);
        }
      }
    }

    private void malformedUpdate(long counter) {
      writeToFile(configFile, "<configuration scan=\"true\" scanPeriod=\"50 millisecond\">\n" +
              "  <root level=\"ERROR\">\n" +
              "</configuration>");
    }

    private void malformedInnerUpdate(long counter) {
      writeToFile(configFile, "<included>\n" +
              "  <root>\n" +
              "</included>");
    }

    void touchFile() {
      configFile.setLastModified(System.currentTimeMillis());
    }
  }


}
