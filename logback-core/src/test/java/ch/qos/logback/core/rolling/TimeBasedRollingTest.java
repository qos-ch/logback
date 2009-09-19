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
package ch.qos.logback.core.rolling;

import static org.junit.Assert.assertTrue;

import java.sql.Date;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.util.Compare;
import ch.qos.logback.core.util.CoreTestConstants;

/**
 * A rather exhaustive set of tests. Tests include leaving the file option
 * blank, or setting it, with and without compression, and tests with or without
 * stopping/restarting the RollingFileAppender.
 * 
 * The regression tests log a few times using a RollingFileAppender. Then, they
 * predict the names of the files which should be generated and compare them
 * with witness files.
 * 
 * <pre>
 *                Compression     file option    Stop/Restart 
 *     Test1      NO              BLANK           NO
 *     Test2      YES             BLANK           NO
 *     Test3      NO              BLANK           YES
 *     Test4      NO              SET             YES 
 *     Test5      NO              SET             NO
 *     Test6      YES             SET             NO
 * </pre>
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class TimeBasedRollingTest extends ScaffoldingForRollingTests {

  RollingFileAppender<Object> rfa1 = new RollingFileAppender<Object>();
  TimeBasedRollingPolicy<Object> tbrp1 = new TimeBasedRollingPolicy<Object>();

  RollingFileAppender<Object> rfa2 = new RollingFileAppender<Object>();
  TimeBasedRollingPolicy<Object> tbrp2 = new TimeBasedRollingPolicy<Object>();


  @Before
  @Override
  public void setUp() {
    super.setUp();
  }


  @After
  public void tearDown() {
  }

  void initRFA(RollingFileAppender<Object> rfa, String filename) {
    rfa.setContext(context);
    rfa.setLayout(layout);
    if (filename != null) {
      rfa.setFile(filename);
    }
  }

  void initTRBP(RollingFileAppender<Object> rfa, TimeBasedRollingPolicy<Object> tbrp,
      String filenamePattern, long givenTime, long lastCheck) {
    tbrp.setContext(context);
    tbrp.setFileNamePattern(filenamePattern);
    tbrp.setParent(rfa);
    tbrp.timeBasedTriggering = new DefaultTimeBasedFileNamingAndTriggeringPolicy<Object>();
    tbrp.timeBasedTriggering.setCurrentTime(givenTime);
    if (lastCheck != 0) {
      tbrp.timeBasedTriggering.setDateInCurrentPeriod(new Date(lastCheck));
    }
    rfa.setRollingPolicy(tbrp);
    tbrp.start();
    rfa.start();
  }

  /**
   * Test rolling without compression, file option left blank, no stop/start
   */
  @Test
  public void noCompression_FileBlank_NoRestart_1() throws Exception {
    String testId = "test1";
    initRFA(rfa1, null);
    initTRBP(rfa1, tbrp1, randomOutputDir + testId + "-%d{"
        + DATE_PATTERN_WITH_SECONDS + "}", currentTime, 0);

    // compute the current filename
    addExpectedFileName_ByDate(testId, getDateOfCurrentPeriodsStart(), false);

    incCurrentTime(1100);
    tbrp1.timeBasedTriggering.setCurrentTime(currentTime);

    for (int i = 0; i < 3; i++) {
      rfa1.doAppend("Hello---" + i);
      addExpectedFileNamedIfItsTime_ByDate(testId, false);
      incCurrentTime(500);
      tbrp1.timeBasedTriggering.setCurrentTime(currentTime);
    }

    int i = 0;
    for (String fn : expectedFilenameList) {
      assertTrue(Compare.compare(fn, CoreTestConstants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-" + testId + "." + i++));
    }
  }

  /**
   * With compression, file option left blank, no stop/restart
   */
  @Test
  public void withCompression_FileBlank_NoRestart_2() throws Exception {
    String testId = "test2";
    initRFA(rfa1, null);
    initTRBP(rfa1, tbrp1, randomOutputDir + testId + "-%d{"
        + DATE_PATTERN_WITH_SECONDS + "}.gz", currentTime, 0);

    addExpectedFileName_ByDate(testId, getDateOfCurrentPeriodsStart(), true);
    incCurrentTime(1100);
    tbrp1.timeBasedTriggering.setCurrentTime(currentTime);

    for (int i = 0; i < 3; i++) {
      // when i == 2, file name should not have .gz extension
      addExpectedFileNamedIfItsTime_ByDate(testId, i != 2);
      rfa1.doAppend("Hello---" + i);
      incCurrentTime(500);
      tbrp1.timeBasedTriggering.setCurrentTime(currentTime);
    }

    tbrp1.future.get(2000, TimeUnit.MILLISECONDS);

    int i = 0;
    for (String fn : expectedFilenameList) {
      assertTrue(Compare.compare(fn, CoreTestConstants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-" + testId + "." + i + addGZIfNotLast(i)));
      i++;
    }
  }

  /**
   * No compression, file option left blank, with stop/restart,
   */
  @Test
  public void noCompression_FileBlank_StopRestart_3() throws Exception {
    String testId = "test3";

    initRFA(rfa1, null);
    initTRBP(rfa1, tbrp1, randomOutputDir + testId + "-%d{"
        + DATE_PATTERN_WITH_SECONDS + "}", currentTime, 0);

    // a new file is created by virtue of rfa.start();
    addExpectedFileName_ByDate(testId, getDateOfCurrentPeriodsStart(), false);

    incCurrentTime(1100);
    tbrp1.timeBasedTriggering.setCurrentTime(currentTime);

    for (int i = 0; i <= 2; i++) {
      rfa1.doAppend("Hello---" + i);
      addExpectedFileNamedIfItsTime_ByDate(testId, false);
      incCurrentTime(500);
      tbrp1.timeBasedTriggering.setCurrentTime(currentTime);
    }

    rfa1.stop();

    initRFA(rfa2, null);
    initTRBP(rfa2, tbrp2, randomOutputDir + testId + "-%d{"
        + DATE_PATTERN_WITH_SECONDS + "}", tbrp1.timeBasedTriggering.getCurrentTime(), 0);

    for (int i = 0; i <= 2; i++) {
      addExpectedFileNamedIfItsTime_ByDate(testId, false);
      rfa2.doAppend("World---" + i);
      incCurrentTime(100);
      tbrp2.timeBasedTriggering.setCurrentTime(currentTime);
    }

    int i = 0;
    for (String fn : expectedFilenameList) {
      assertTrue(Compare.compare(fn, CoreTestConstants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-" + testId + "." + i++));
    }
  }

  /**
   * Without compression, file option set, with stop/restart
   */
  @Test
  public void noCompression_FileSet_StopRestart_4() throws Exception {
    String testId = "test4";
    initRFA(rfa1, testId2FileName(testId));
    initTRBP(rfa1, tbrp1, randomOutputDir + testId + "-%d{"
        + DATE_PATTERN_WITH_SECONDS + "}", currentTime, 0);

    addExpectedFileName_ByDate(testId, getDateOfCurrentPeriodsStart(), false);

    incCurrentTime(1100);
    tbrp1.timeBasedTriggering.setCurrentTime(currentTime);

    for (int i = 0; i <= 2; i++) {
      rfa1.doAppend("Hello---" + i);
      addExpectedFileNamedIfItsTime_ByDate(testId, false);
      incCurrentTime(500);
      tbrp1.timeBasedTriggering.setCurrentTime(currentTime);
    }

    rfa1.stop();

    initRFA(rfa2, testId2FileName(testId));
    initTRBP(rfa2, tbrp2, randomOutputDir + testId + "-%d{"
        + DATE_PATTERN_WITH_SECONDS + "}", currentTime, currentTime);

    for (int i = 0; i <= 2; i++) {
      rfa2.doAppend("World---" + i);
      addExpectedFileNamedIfItsTime_ByDate(testId, false);
      incCurrentTime(100);
      tbrp2.timeBasedTriggering.setCurrentTime(currentTime);
    }

    massageExpectedFilesToCorresponToCurrentTarget("test4.log");

    int i = 0;
    for (String fn : expectedFilenameList) {
      assertTrue(Compare.compare(fn, CoreTestConstants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-" + testId + "." + i++));
    }
  }

  @Test
  public void noCompression_FileSet_StopRestart_WithLongWait_4B()
      throws Exception {
    String testId = "test4B";
    initRFA(rfa1, testId2FileName(testId));
    initTRBP(rfa1, tbrp1, randomOutputDir + testId + "-%d{"
        + DATE_PATTERN_WITH_SECONDS + "}", currentTime, 0);

    addExpectedFileName_ByDate(testId, getDateOfCurrentPeriodsStart(), false);

    incCurrentTime(1100);
    tbrp1.timeBasedTriggering.setCurrentTime(currentTime);

    for (int i = 0; i <= 2; i++) {
      rfa1.doAppend("Hello---" + i);
      addExpectedFileNamedIfItsTime_ByDate(testId, false);
      incCurrentTime(500);
      tbrp1.timeBasedTriggering.setCurrentTime(currentTime);
    }

    rfa1.stop();

    long fileTimestamp = currentTime;
    incCurrentTime(2000);

    initRFA(rfa2, randomOutputDir + "test4B.log");
    initTRBP(rfa2, tbrp2, randomOutputDir + testId + "-%d{"
        + DATE_PATTERN_WITH_SECONDS + "}", currentTime, fileTimestamp);

    for (int i = 0; i <= 2; i++) {
      rfa2.doAppend("World---" + i);
      addExpectedFileNamedIfItsTime_ByDate(testId, false);
      incCurrentTime(100);
      tbrp2.timeBasedTriggering.setCurrentTime(currentTime);
    }

    massageExpectedFilesToCorresponToCurrentTarget("test4B.log");

    int i = 0;
    for (String fn : expectedFilenameList) {
      assertTrue(Compare.compare(fn, CoreTestConstants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test4B." + i++));
    }

  }

  /**
   * No compression, file option set, without stop/restart
   */
  @Test
  public void noCompression_FileSet_NoRestart_5() throws Exception {
    String testId = "test5";

    initRFA(rfa1, testId2FileName(testId));
    initTRBP(rfa1, tbrp1, randomOutputDir + testId + "-%d{"
        + DATE_PATTERN_WITH_SECONDS + "}", currentTime, 0);

    addExpectedFileName_ByDate(testId, getDateOfCurrentPeriodsStart(), false);

    incCurrentTime(1100);
    tbrp1.timeBasedTriggering.setCurrentTime(currentTime);

    for (int i = 0; i < 3; i++) {
      rfa1.doAppend("Hello---" + i);
      addExpectedFileNamedIfItsTime_ByDate(testId, false);
      incCurrentTime(500);
      tbrp1.timeBasedTriggering.setCurrentTime(currentTime);
    }

    massageExpectedFilesToCorresponToCurrentTarget("test5.log");

    int i = 0;
    for (String fn : expectedFilenameList) {
      assertTrue(Compare.compare(fn, CoreTestConstants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test5." + i++));
    }
  }

  /**
   * With compression, file option set, no stop/restart,
   */
  @Test
  public void withCompression_FileSet_NoRestart_6() throws Exception {

    String testId = "test6";

    initRFA(rfa1, testId2FileName(testId));
    initTRBP(rfa1, tbrp1, randomOutputDir + testId + "-%d{"
        + DATE_PATTERN_WITH_SECONDS + "}.gz", currentTime, 0);

    addExpectedFileName_ByDate(testId, getDateOfCurrentPeriodsStart(), true);

    incCurrentTime(1100);
    tbrp1.timeBasedTriggering.setCurrentTime(currentTime);

    for (int i = 0; i < 3; i++) {
      rfa1.doAppend("Hello---" + i);
      addExpectedFileNamedIfItsTime_ByDate(testId, true);
      incCurrentTime(500);
      tbrp1.timeBasedTriggering.setCurrentTime(currentTime);
    }

    // wait for the compression task to finish
    tbrp1.future.get(1000, TimeUnit.MILLISECONDS);

    massageExpectedFilesToCorresponToCurrentTarget("test6.log");

    int i = 0;
    for (String fn : expectedFilenameList) {
      assertTrue(Compare.compare(fn, CoreTestConstants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-" + testId + "." + i + addGZIfNotLast(i)));
      i++;
    }
  }

  // =========================================================================
  // utility methods
  // =========================================================================


  void massageExpectedFilesToCorresponToCurrentTarget(String file) {
    // we added one too many files by date
    expectedFilenameList.remove(expectedFilenameList.size() - 1);
    // since file is set, we have to add it
    addExpectedFileName_ByFile(file);
  }

  void addExpectedFileName_ByFile(String filenameSuffix) {
    String fn = randomOutputDir + filenameSuffix;
    expectedFilenameList.add(fn);
  }
}
