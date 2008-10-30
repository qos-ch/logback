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
import java.util.Arrays;
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
import ch.qos.logback.core.util.Constants;

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
 *    Test2      NO              BLANK           YES
 *    Test3      YES             BLANK           NO
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
  List<String> filenameList = new ArrayList<String>();

  @Before
  public void setUp() {
    context.setName("test");
    cal.set(Calendar.MILLISECOND, 0);
    currentTime = cal.getTimeInMillis();
    recomputeRolloverThreshold(currentTime);

    // Delete .log files
    {
      File target = new File(Constants.OUTPUT_DIR_PREFIX + "test4.log");
      target.mkdirs();
      target.delete();
    }
    {
      File target = new File(Constants.OUTPUT_DIR_PREFIX + "test5.log");
      target.mkdirs();
      target.delete();
    }
    {
      File target = new File(Constants.OUTPUT_DIR_PREFIX + "test6.log");
      target.mkdirs();
      target.delete();
    }
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
    tbrp.setCurrentTime(givenTime);
    if (lastCheck != 0) {
      tbrp.setLastCheck(new Date(lastCheck));
    }
    tbrp.start();
    rfa.setRollingPolicy(tbrp);
    rfa.start();
  }

  void addFileName(String testId, Date date, boolean compression) {
    String fn = Constants.OUTPUT_DIR_PREFIX + testId + sdf.format(date);
    if (compression) {
      fn += ".gz";
    }
    filenameList.add(fn);
  }

  String[] computeFilenames(String testStr, boolean compression, String lastFile) {
    String[] filenames = new String[3];
    int oneBeforeLast = filenames.length - 1;
    for (int i = 0; i < oneBeforeLast; i++) {
      filenames[i] = Constants.OUTPUT_DIR_PREFIX + testStr
          + sdf.format(cal.getTime());
      if (compression) {
        filenames[i] += ".gz";
      }
      cal.add(Calendar.SECOND, 1);
    }
    if (lastFile != null) {
      filenames[oneBeforeLast] = Constants.OUTPUT_DIR_PREFIX + lastFile;
    } else {
      filenames[oneBeforeLast] = Constants.OUTPUT_DIR_PREFIX + testStr
          + sdf.format(cal.getTime());
    }
    return filenames;
  }

  /**
   * Test rolling without compression, file option left blank, no stop/start
   */
  @Test
  public void noCompression_FileBlank_NoRestart_1() throws Exception {
    String testId = "test1-";
    initRFA(rfa1, null);
    initTRBP(rfa1, tbrp1, Constants.OUTPUT_DIR_PREFIX + testId + "%d{"
        + DATE_PATTERN + "}", currentTime, 0);

    // compute the current filename
    addFileName(testId, getTimeForElapsedPeriod(), false);

    incCurrentTime(1100);
    tbrp1.setCurrentTime(currentTime);

    for (int i = 0; i < 3; i++) {
      addFileNameIfNecessary(testId, false);
      rfa1.doAppend("Hello---" + i);
      incCurrentTime(500);
      tbrp1.setCurrentTime(currentTime);
    }

    int i = 0;
    for (String fn : filenameList) {
      assertTrue(Compare.compare(fn, Constants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test1." + i++));
    }
  }

  /**
   * No compression, file option left blank, with stop/restart,
   */
  @Test
  public void noCompression_FileBlank_StopRestart_2() throws Exception {
    String testId = "test2-";

    initRFA(rfa1, null);
    initTRBP(rfa1, tbrp1, Constants.OUTPUT_DIR_PREFIX + testId + "%d{"
        + DATE_PATTERN + "}", currentTime, 0);

    addFileName(testId, getTimeForElapsedPeriod(), false);
    incCurrentTime(1100);
    tbrp1.setCurrentTime(currentTime);

    for (int i = 0; i <= 2; i++) {
      addFileNameIfNecessary(testId, false);
      rfa1.doAppend("Hello---" + i);
      incCurrentTime(500);
      tbrp1.setCurrentTime(currentTime);
    }

    rfa1.stop();

    initRFA(rfa2, null);
    initTRBP(rfa2, tbrp2, Constants.OUTPUT_DIR_PREFIX + "test2-%d{"
        + DATE_PATTERN + "}", tbrp1.getCurrentTime(), 0);

    for (int i = 0; i <= 2; i++) {
      addFileNameIfNecessary(testId, false);
      rfa2.doAppend("Hello---" + i);
      incCurrentTime(100);
      tbrp2.setCurrentTime(currentTime);
    }

    int i = 0;
    for (String fn : filenameList) {
      assertTrue(Compare.compare(fn, Constants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test2." + i++));
    }
  }

  /**
   * With compression, file option left blank, no stop/restart
   */
  @Test
  public void withCompression_FileBlank_NoRestart_3() throws Exception {
    String testId = "test3-";
    initRFA(rfa1, null);
    initTRBP(rfa1, tbrp1, Constants.OUTPUT_DIR_PREFIX + testId + "%d{"
        + DATE_PATTERN + "}.gz", currentTime, 0);

    String[] filenames = computeFilenames(testId, true, null);

    addFileName(testId, getTimeForElapsedPeriod(), true);
    incCurrentTime(1100);
    tbrp1.setCurrentTime(currentTime);

    for (int i = 0; i < 3; i++) {
      addFileNameIfNecessary(testId, true);
      rfa1.doAppend("Hello---" + i);
      tbrp1.setCurrentTime(addTime(tbrp1.getCurrentTime(), 500));
    }

    tbrp1.future.get(2000, TimeUnit.MILLISECONDS);

    System.out.println(Arrays.toString(filenames));
    System.out.println(filenameList.toString());

    for (int i = 0; i < 2; i++) {
      assertTrue(Compare.gzCompare(filenameList.get(i),
          Constants.TEST_DIR_PREFIX + "witness/rolling/tbr-test3." + i + ".gz"));
    }

    assertTrue(Compare.compare(filenameList.get(2), Constants.TEST_DIR_PREFIX
        + "witness/rolling/tbr-test3.2"));
  }

  /**
   * Without compression, file option set, with stop/restart
   */
  @Test
  public void noCompression_FileSet_StopRestart_4() throws Exception {
    initRFA(rfa1, Constants.OUTPUT_DIR_PREFIX + "test4.log");
    initTRBP(rfa1, tbrp1, Constants.OUTPUT_DIR_PREFIX + "test4-%d{"
        + DATE_PATTERN + "}", currentTime, 0);

    String[] filenames = computeFilenames("test4-", false, "test4.log");

    System.out.println("CT=" + sdf.format(new Date(currentTime)));
    System.out.println("tbrp1 CT="
        + sdf.format(new Date(tbrp1.getCurrentTime())));

    tbrp1.setCurrentTime(addTime(currentTime, 1100));

    System.out.println("tbrp1 CT="
        + sdf.format(new Date(tbrp1.getCurrentTime())));

    for (int i = 0; i <= 2; i++) {
      rfa1.doAppend("Hello---" + i);
      tbrp1.setCurrentTime(addTime(tbrp1.getCurrentTime(), 500));
    }

    rfa1.stop();

    initRFA(rfa2, Constants.OUTPUT_DIR_PREFIX + "test4.log");
    initTRBP(rfa2, tbrp2, Constants.OUTPUT_DIR_PREFIX + "test4-%d{"
        + DATE_PATTERN + "}", tbrp1.getCurrentTime(), tbrp1.getCurrentTime());

    for (int i = 0; i <= 2; i++) {
      rfa2.doAppend("World---" + i);
      tbrp2.setCurrentTime(addTime(tbrp2.getCurrentTime(), 100));
    }

    for (int i = 0; i < 3; i++) {
      assertTrue(Compare.compare(filenames[i], Constants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test4." + i));
    }
  }

  @Test
  public void noCompression_FileSet_StopRestart_WithLongWait_4B()
      throws Exception {
    initRFA(rfa1, Constants.OUTPUT_DIR_PREFIX + "test4B.log");
    initTRBP(rfa1, tbrp1, Constants.OUTPUT_DIR_PREFIX + "test4B-%d{"
        + DATE_PATTERN + "}", currentTime, 0);

    String[] filenames = computeFilenames("test4B-", false, "test4B.log");

    System.out.println("CT=" + sdf.format(new Date(currentTime)));
    System.out.println("tbrp1 CT="
        + sdf.format(new Date(tbrp1.getCurrentTime())));

    tbrp1.setCurrentTime(addTime(currentTime, 1100));

    System.out.println("tbrp1 CT="
        + sdf.format(new Date(tbrp1.getCurrentTime())));

    for (int i = 0; i <= 2; i++) {
      rfa1.doAppend("Hello---" + i);
      tbrp1.setCurrentTime(addTime(tbrp1.getCurrentTime(), 500));
    }

    rfa1.stop();
    System.out.println("post stop tbrp1 CT="
        + sdf.format(new Date(tbrp1.getCurrentTime())));

    initRFA(rfa2, Constants.OUTPUT_DIR_PREFIX + "test4B.log");
    initTRBP(rfa2, tbrp2, Constants.OUTPUT_DIR_PREFIX + "test4B-%d{"
        + DATE_PATTERN + "}", tbrp1.getCurrentTime() + 3000, tbrp1
        .getCurrentTime());

    System.out.println("tbrp2 CT="
        + sdf.format(new Date(tbrp2.getCurrentTime())));

    for (int i = 0; i <= 2; i++) {
      rfa2.doAppend("World---" + i);
      System.out.println("in loop tbrp2 CT="
          + sdf.format(new Date(tbrp2.getCurrentTime())));
      tbrp2.setCurrentTime(addTime(tbrp2.getCurrentTime(), 500));
    }

    System.out.println("tbrp2 CT="
        + sdf.format(new Date(tbrp2.getCurrentTime())));

    if (1 == 1) {
      return;
    }
    for (int i = 0; i < 3; i++) {
      assertTrue(Compare.compare(filenames[i], Constants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test4." + i));
    }
  }

  /**
   * No compression, file option set, without stop/restart
   */
  @Test
  public void noCompression_FileSet_NoRestart_5() throws Exception {
    initRFA(rfa1, Constants.OUTPUT_DIR_PREFIX + "test5.log");
    initTRBP(rfa1, tbrp1, Constants.OUTPUT_DIR_PREFIX + "test5-%d{"
        + DATE_PATTERN + "}", currentTime, 0);

    String[] filenames = computeFilenames("test5-", false, "test5.log");

    tbrp1.setCurrentTime(addTime(currentTime, 1100));

    for (int i = 0; i < 3; i++) {
      rfa1.doAppend("Hello---" + i);
      tbrp1.setCurrentTime(addTime(tbrp1.getCurrentTime(), 500));
    }

    for (int i = 0; i < 3; i++) {
      assertTrue(Compare.compare(filenames[i], Constants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test5." + i));
    }
  }

  /**
   * With compression, file option set, no stop/restart,
   */
  @Test
  public void withCompression_FileSet_NoRestart_6() throws Exception {
    initRFA(rfa1, Constants.OUTPUT_DIR_PREFIX + "test6.log");
    initTRBP(rfa1, tbrp1, Constants.OUTPUT_DIR_PREFIX + "test6-%d{"
        + DATE_PATTERN + "}.gz", currentTime, 0);

    String[] filenames = computeFilenames("test6-", true, "test6.log");

    tbrp1.setCurrentTime(addTime(currentTime, 1100));

    for (int i = 0; i < 3; i++) {
      rfa1.doAppend("Hello---" + i);
      tbrp1.setCurrentTime(addTime(tbrp1.getCurrentTime(), 500));
    }

    // wait for the compression task to finish
    tbrp1.future.get(1000, TimeUnit.MILLISECONDS);

    for (int i = 0; i < 2; i++) {
      assertTrue(Compare.gzCompare(filenames[i], Constants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test6." + i + ".gz"));
    }

    assertTrue(Compare.compare(filenames[2], Constants.TEST_DIR_PREFIX
        + "witness/rolling/tbr-test6.2"));
  }

  // =========================================================================
  // utility methods
  // =========================================================================

  void addFileNameIfNecessary(String testId, boolean compression) {
    if (passThresholdTime(nextRolloverThreshold)) {
      addFileName(testId, getTimeForElapsedPeriod(), compression);
      recomputeRolloverThreshold(currentTime);
    }
  }

  Date getTimeForElapsedPeriod() {
    long delta = currentTime % 1000;
    return new Date(currentTime - delta);
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
}
