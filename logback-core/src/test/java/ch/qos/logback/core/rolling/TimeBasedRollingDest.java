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
package ch.qos.logback.core.rolling;

import static org.junit.Assert.assertTrue;

import java.io.File;

import ch.qos.logback.core.testUtil.RandomUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.util.Compare;
import ch.qos.logback.core.util.CoreTestConstants;

/**
 * A rather exhaustive set of tests. Tests include leaving the file option
 * blank, or setting it, with and without compression, and tests with or without
 * stopping/restarting the RollingFileAppender.
 * <p/>
 * The regression tests log a few times using a RollingFileAppender. Then, they
 * predict the names of the files which should be generated and compare them
 * with witness files.
 * <p/>
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
 * @deprecated replaced by  TimeBasedRolling_STest
 */
public class TimeBasedRollingDest extends ScaffoldingForRollingTests {

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
    rfa.setEncoder(encoder);
    if (filename != null) {
      rfa.setFile(filename);
    }
  }

  void initTRBP(RollingFileAppender<Object> rfa,
                TimeBasedRollingPolicy<Object> tbrp, String filenamePattern,
                long givenTime) {
    tbrp.setContext(context);
    tbrp.setFileNamePattern(filenamePattern);
    tbrp.setParent(rfa);
    tbrp.timeBasedFileNamingAndTriggeringPolicy = new DefaultTimeBasedFileNamingAndTriggeringPolicy<Object>();
    tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(givenTime);
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
            + DATE_PATTERN_WITH_SECONDS + "}", currentTime);

    // compute the current filename
    addExpectedFileName_ByDate(randomOutputDir, testId, getDateOfCurrentPeriodsStart(), false);

    incCurrentTime(1100);
    tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);

    for (int i = 0; i < 3; i++) {
      rfa1.doAppend("Hello---" + i);
      addExpectedFileNamedIfItsTime_ByDate(randomOutputDir, testId, false);
      incCurrentTime(500);
      tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
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
            + DATE_PATTERN_WITH_SECONDS + "}.gz", currentTime);

    addExpectedFileName_ByDate(randomOutputDir, testId, getDateOfCurrentPeriodsStart(), true);
    incCurrentTime(1100);
    tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);

    for (int i = 0; i < 3; i++) {
      // when i == 2, file name should not have .gz extension
      addExpectedFileNamedIfItsTime_ByDate(randomOutputDir, testId, i != 2);
      rfa1.doAppend("Hello---" + i);
      incCurrentTime(500);
      tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
      waitForCompression(tbrp1);
    }

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
            + DATE_PATTERN_WITH_SECONDS + "}", currentTime);

    // a new file is created by virtue of rfa.start();
    addExpectedFileName_ByDate(randomOutputDir, testId, getDateOfCurrentPeriodsStart(), false);

    incCurrentTime(1100);
    tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);

    for (int i = 0; i <= 2; i++) {
      rfa1.doAppend("Hello---" + i);
      addExpectedFileNamedIfItsTime_ByDate(randomOutputDir, testId, false);
      incCurrentTime(500);
      tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
    }

    rfa1.stop();

    initRFA(rfa2, null);
    initTRBP(rfa2, tbrp2, randomOutputDir + testId + "-%d{"
            + DATE_PATTERN_WITH_SECONDS + "}", tbrp1.timeBasedFileNamingAndTriggeringPolicy
            .getCurrentTime());

    for (int i = 0; i <= 2; i++) {
      addExpectedFileNamedIfItsTime_ByDate(randomOutputDir, testId, false);
      rfa2.doAppend("World---" + i);
      incCurrentTime(100);
      tbrp2.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
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
            + DATE_PATTERN_WITH_SECONDS + "}", currentTime);

    addExpectedFileName_ByDate(randomOutputDir, testId, getDateOfCurrentPeriodsStart(), false);

    incCurrentTime(1100);
    tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);

    for (int i = 0; i <= 2; i++) {
      rfa1.doAppend("Hello---" + i);
      addExpectedFileNamedIfItsTime_ByDate(randomOutputDir, testId, false);
      incCurrentTime(500);
      tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
    }

    rfa1.stop();

    // change the timestamp of the currently actively file
    File activeFile = new File(rfa1.getFile());
    activeFile.setLastModified(currentTime);

    initRFA(rfa2, testId2FileName(testId));
    initTRBP(rfa2, tbrp2, randomOutputDir + testId + "-%d{"
            + DATE_PATTERN_WITH_SECONDS + "}", currentTime);

    for (int i = 0; i <= 2; i++) {
      rfa2.doAppend("World---" + i);
      addExpectedFileNamedIfItsTime_ByDate(randomOutputDir, testId, false);
      incCurrentTime(100);
      tbrp2.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
    }

    System.out.println("Before "+expectedFilenameList);
    massageExpectedFilesToCorresponToCurrentTarget("test4.log");
    System.out.println("After "+expectedFilenameList);


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
            + DATE_PATTERN_WITH_SECONDS + "}", currentTime);

    addExpectedFileName_ByDate(randomOutputDir, testId, getDateOfCurrentPeriodsStart(), false);

    incCurrentTime(1100);
    tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);

    for (int i = 0; i <= 2; i++) {
      rfa1.doAppend("Hello---" + i);
      addExpectedFileNamedIfItsTime_ByDate(randomOutputDir, testId, false);
      incCurrentTime(500);
      tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
    }

    rfa1.stop();

    // change the timestamp of the currently actively file
    File activeFile = new File(rfa1.getFile());
    activeFile.setLastModified(currentTime);

    incCurrentTime(2000);

    initRFA(rfa2, randomOutputDir + "test4B.log");
    initTRBP(rfa2, tbrp2, randomOutputDir + testId + "-%d{"
            + DATE_PATTERN_WITH_SECONDS + "}", currentTime);

    for (int i = 0; i <= 2; i++) {
      rfa2.doAppend("World---" + i);
      addExpectedFileNamedIfItsTime_ByDate(randomOutputDir, testId, false);
      incCurrentTime(100);
      tbrp2.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
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
            + DATE_PATTERN_WITH_SECONDS + "}", currentTime);

    addExpectedFileName_ByDate(randomOutputDir, testId, getDateOfCurrentPeriodsStart(), false);

    incCurrentTime(1100);
    tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);

    for (int i = 0; i < 3; i++) {
      rfa1.doAppend("Hello---" + i);
      addExpectedFileNamedIfItsTime_ByDate(randomOutputDir, testId, false);
      incCurrentTime(500);
      tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
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
            + DATE_PATTERN_WITH_SECONDS + "}.gz", currentTime);

    addExpectedFileName_ByDate(randomOutputDir, testId, getDateOfCurrentPeriodsStart(), true);

    incCurrentTime(1100);
    tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);

    for (int i = 0; i < 3; i++) {
      rfa1.doAppend("Hello---" + i);
      addExpectedFileNamedIfItsTime_ByDate(randomOutputDir, testId, true);
      incCurrentTime(500);
      tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
      waitForCompression(tbrp1);
    }

    massageExpectedFilesToCorresponToCurrentTarget("test6.log");

    int i = 0;
    for (String fn : expectedFilenameList) {
      assertTrue(Compare.compare(fn, CoreTestConstants.TEST_DIR_PREFIX
              + "witness/rolling/tbr-" + testId + "." + i + addGZIfNotLast(i)));
      i++;
    }
  }

  @Test
  public void withMissingTargetDir() throws Exception {
    String testId = "missingTargetDir";

    initRFA(rfa1, testId2FileName(testId));
    int secondDiff = RandomUtil.getPositiveInt();
    String randomTargetDir = CoreTestConstants.OUTPUT_DIR_PREFIX + secondDiff + '/';

    System.out.println("randomOutputDir"+randomOutputDir);
    System.out.println("randomTargetDir"+randomTargetDir);

    initTRBP(rfa1, tbrp1, randomTargetDir + testId + "-%d{"
            + DATE_PATTERN_WITH_SECONDS + "}", currentTime);

    addExpectedFileName_ByDate(randomTargetDir, testId, getDateOfCurrentPeriodsStart(), false);

    incCurrentTime(1100);
    tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);

    for (int i = 0; i < 3; i++) {
      rfa1.doAppend("Hello---" + i);
      addExpectedFileNamedIfItsTime_ByDate(randomTargetDir, testId, false);
      incCurrentTime(500);
      tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
    }
    massageExpectedFilesToCorresponToCurrentTarget("missingTargetDir.log");
    int i = 0;
    for (String fn : expectedFilenameList) {
      System.out.println("expectedFile="+fn);
      assertTrue(Compare.compare(fn, CoreTestConstants.TEST_DIR_PREFIX
              + "witness/rolling/tbr-test5." + i++));
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
