/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.rolling;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.layout.EchoLayout;
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
 *               Compression     file option    Stop/Restart 
 *    Test1      NO              BLANK           NO
 *    Test2      YES             BLANK           NO
 *    Test3      NO              BLANK           YES
 *    Test4      NO              SET             YES 
 *    Test5      NO              SET             NO
 *    Test6      YES             SET             NO
 * </pre>
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class TimeBasedRollingTest {

  static final String DATE_PATTERN = "yyyy-MM-dd_HH_mm_ss";
  SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

  EchoLayout<Object> layout = new EchoLayout<Object>();
  Context context = new ContextBase();

  RollingFileAppender<Object> rfa1 = new RollingFileAppender<Object>();
  TimeBasedRollingPolicy tbrp1 = new TimeBasedRollingPolicy();

  RollingFileAppender<Object> rfa2 = new RollingFileAppender<Object>();
  TimeBasedRollingPolicy tbrp2 = new TimeBasedRollingPolicy();

  Calendar cal = Calendar.getInstance();
  long currentTime; // initialized in setUp()
  long nextRolloverThreshold; // initialized in setUp()
  List<String> expectedFilenameList = new ArrayList<String>();

  @Before
  public void setUp() {
    context.setName("test");
    cal.set(Calendar.MILLISECOND, 333);
    currentTime = cal.getTimeInMillis();
    recomputeRolloverThreshold(currentTime);
    System.out.println("at setUp() currentTime=" + sdf.format(new Date(currentTime)));

    // Delete .log files
    deleteStaleLogFile("test4.log");
    deleteStaleLogFile("test4B.log");
    deleteStaleLogFile("test5.log");
    deleteStaleLogFile("test6.log");
  }

  void deleteStaleLogFile(String filename) {
    File target = new File(CoreTestConstants.OUTPUT_DIR_PREFIX + filename);
    target.mkdirs();
    target.delete();
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

  void initTRBP(RollingFileAppender<Object> rfa, TimeBasedRollingPolicy tbrp,
      String filenamePattern, long givenTime, long lastCheck) {
    tbrp.setContext(context);
    tbrp.setFileNamePattern(filenamePattern);
    tbrp.setParent(rfa);
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
    initTRBP(rfa1, tbrp1, CoreTestConstants.OUTPUT_DIR_PREFIX + testId + "-%d{"
        + DATE_PATTERN + "}", currentTime, 0);

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
    initTRBP(rfa1, tbrp1, CoreTestConstants.OUTPUT_DIR_PREFIX + testId + "-%d{"
        + DATE_PATTERN + "}.gz", currentTime, 0);

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
    initTRBP(rfa1, tbrp1, CoreTestConstants.OUTPUT_DIR_PREFIX + testId + "-%d{"
        + DATE_PATTERN + "}", currentTime, 0);

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
    initTRBP(rfa2, tbrp2, CoreTestConstants.OUTPUT_DIR_PREFIX + testId + "-%d{"
        + DATE_PATTERN + "}", tbrp1.timeBasedTriggering.getCurrentTime(), 0);

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
    initTRBP(rfa1, tbrp1, CoreTestConstants.OUTPUT_DIR_PREFIX + testId + "-%d{"
        + DATE_PATTERN + "}", currentTime, 0);

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
    initTRBP(rfa2, tbrp2, CoreTestConstants.OUTPUT_DIR_PREFIX + testId + "-%d{"
        + DATE_PATTERN + "}", currentTime, currentTime);

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
    initTRBP(rfa1, tbrp1, CoreTestConstants.OUTPUT_DIR_PREFIX + testId + "-%d{"
        + DATE_PATTERN + "}", currentTime, 0);

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

    initRFA(rfa2, CoreTestConstants.OUTPUT_DIR_PREFIX + "test4B.log");
    initTRBP(rfa2, tbrp2, CoreTestConstants.OUTPUT_DIR_PREFIX + testId +"-%d{"
        + DATE_PATTERN + "}", currentTime, fileTimestamp);

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
    initTRBP(rfa1, tbrp1, CoreTestConstants.OUTPUT_DIR_PREFIX + testId + "-%d{"
        + DATE_PATTERN + "}", currentTime, 0);

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
    initTRBP(rfa1, tbrp1, CoreTestConstants.OUTPUT_DIR_PREFIX + testId + "-%d{"
        + DATE_PATTERN + "}.gz", currentTime, 0);

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

  String testId2FileName(String testId) {
    return CoreTestConstants.OUTPUT_DIR_PREFIX + testId + ".log";
  }

  void massageExpectedFilesToCorresponToCurrentTarget(String file) {
    // we added one too many files by date
    expectedFilenameList.remove(expectedFilenameList.size() - 1);
    // since file is set, we have to add it
    addExpectedFileName_ByFile(file);
  }

  String addGZIfNotLast(int i) {
    int lastIndex = expectedFilenameList.size() - 1;
    if (i != lastIndex) {
      return ".gz";
    } else {
      return "";
    }
  }

  void addExpectedFileName_ByDate(String testId, Date date, boolean gzExtension) {
    String fn = CoreTestConstants.OUTPUT_DIR_PREFIX + testId + "-" + sdf.format(date);
    if (gzExtension) {
      fn += ".gz";
    }
    expectedFilenameList.add(fn);
  }

  void addExpectedFileNamedIfItsTime_ByDate(String testId, boolean gzExtension) {
    if (passThresholdTime(nextRolloverThreshold)) {
      addExpectedFileName_ByDate(testId, getDateOfCurrentPeriodsStart(),
          gzExtension);
      recomputeRolloverThreshold(currentTime);
    }
  }

  void addExpectedFileName_ByFile(String filenameSuffix) {
    String fn = CoreTestConstants.OUTPUT_DIR_PREFIX + filenameSuffix;
    expectedFilenameList.add(fn);
  }

  Date getDateOfCurrentPeriodsStart() {
    long delta = currentTime % 1000;
    return new Date(currentTime - delta);
  }

  Date getDateOfPastPeriodsStart() {
    long delta = currentTime % 1000;
    return new Date(currentTime - delta - 1000);
  }

  static long addTime(long currentTime, long timeToWait) {
    return currentTime + timeToWait;
  }

  boolean passThresholdTime(long nextRolloverThreshold) {
    return currentTime >= nextRolloverThreshold;
  }

  // assuming rollover every second
  void recomputeRolloverThreshold(long ct) {
    long delta = ct % 1000;
    nextRolloverThreshold = (ct - delta) + 1000;
  }

  void incCurrentTime(long increment) {
    currentTime += increment;
  }

  void printLongAsDate(String msg, long time) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
    System.out.println(msg + sdf.format(new Date(time)));
  }
}
