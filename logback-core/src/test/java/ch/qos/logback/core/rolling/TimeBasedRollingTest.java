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

import static org.junit.Assert.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
  long currentTime;

  @Before
  public void setUp() {
    context.setName("test");
    cal.set(Calendar.MILLISECOND, 0);
    currentTime = cal.getTimeInMillis();

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
      String filenamePattern, long currentTime) {
    tbrp.setContext(context);
    tbrp.setFileNamePattern(filenamePattern);
    tbrp.setParent(rfa);
    tbrp.setCurrentTime(currentTime);
    tbrp.start();
    rfa.setRollingPolicy(tbrp);
    rfa.start();
  }

  String[] computeFilenames(String testStr,
      boolean compression, String lastFile) {
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
  public void test1() throws Exception {
    initRFA(rfa1, null);
    initTRBP(rfa1, tbrp1, Constants.OUTPUT_DIR_PREFIX + "test1-%d{"
        + DATE_PATTERN + "}", currentTime);

    String[] filenames = computeFilenames("test1-", false, null);

    // System.out.println("Waiting until next second and 100 millis.");
    tbrp1.setCurrentTime(addTime(currentTime, 1100));
    // System.out.println("Done waiting.");

    for (int i = 0; i < 3; i++) {
      rfa1.doAppend("Hello---" + i);
      tbrp1.setCurrentTime(addTime(tbrp1.getCurrentTime(), 500));
      // Thread.sleep(500);
    }

    // for (int i = 0; i < 3; i++) {
    // System.out.println(i + " expected filename [" + filenames[i] + "].");
    // }

    for (int i = 0; i < 3; i++) {
      assertTrue(Compare.compare(filenames[i], Constants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test1." + i));
    }
  }

  /**
   * No compression, with stop/restart, file option left blank
   */
  @Test
  public void test2() throws Exception {

    initRFA(rfa1, null);
    initTRBP(rfa1, tbrp1, Constants.OUTPUT_DIR_PREFIX + "test2-%d{"
        + DATE_PATTERN + "}", currentTime);

    String[] filenames = computeFilenames("test2-", false, null);

    // System.out.println("Waiting until next second and 100 millis.");
    tbrp1.setCurrentTime(addTime(currentTime, 1100));
    // System.out.println("Done waiting.");

    for (int i = 0; i <= 2; i++) {
      rfa1.doAppend("Hello---" + i);
      tbrp1.setCurrentTime(addTime(tbrp1.getCurrentTime(), 500));
      // Thread.sleep(500);
    }

    rfa1.stop();

    initRFA(rfa2, null);
    initTRBP(rfa2, tbrp2, Constants.OUTPUT_DIR_PREFIX + "test2-%d{"
        + DATE_PATTERN + "}", tbrp1.getCurrentTime());

    for (int i = 0; i <= 2; i++) {
      rfa2.doAppend("Hello---" + i);
      tbrp2.setCurrentTime(addTime(tbrp2.getCurrentTime(), 100));
      // Thread.sleep(100);
    }

    for (int i = 0; i < 3; i++) {
      assertTrue(Compare.compare(filenames[i], Constants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test2." + i));
    }
  }

  /**
   * With compression, file option left blank, no stop/restart
   */
  @Test
  public void test3() throws Exception {
    initRFA(rfa1, null);
    initTRBP(rfa1, tbrp1, Constants.OUTPUT_DIR_PREFIX + "test3-%d{"
        + DATE_PATTERN + "}.gz", currentTime);

    String[] filenames = computeFilenames("test3-", true, null);

    tbrp1.setCurrentTime(addTime(currentTime, 1100));

    for (int i = 0; i < 3; i++) {
      rfa1.doAppend("Hello---" + i);
      tbrp1.setCurrentTime(addTime(tbrp1.getCurrentTime(), 500));
    }

    tbrp1.future.get(2000, TimeUnit.MILLISECONDS);

    for (int i = 0; i < 2; i++) {
      assertTrue(Compare.gzCompare(filenames[i], Constants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test3." + i + ".gz"));
    }

    assertTrue(Compare.compare(filenames[2], Constants.TEST_DIR_PREFIX
        + "witness/rolling/tbr-test3.2"));
  }

  /**
   * Without compression, file option set, with stop/restart
   */
  @Test
  public void test4() throws Exception {
    initRFA(rfa1, Constants.OUTPUT_DIR_PREFIX + "test4.log");
    initTRBP(rfa1, tbrp1, Constants.OUTPUT_DIR_PREFIX + "test4-%d{"
        + DATE_PATTERN + "}", currentTime);

    String[] filenames = computeFilenames("test4-", false, "test4.log");

    // System.out.println("Waiting until next second and 100 millis.");
    tbrp1.setCurrentTime(addTime(currentTime, 1100));
    // System.out.println("Done waiting.");

    for (int i = 0; i <= 2; i++) {
      rfa1.doAppend("Hello---" + i);
      tbrp1.setCurrentTime(addTime(tbrp1.getCurrentTime(), 500));
    }

    rfa1.stop();

    initRFA(rfa2, Constants.OUTPUT_DIR_PREFIX + "test4.log");
    initTRBP(rfa2, tbrp2, Constants.OUTPUT_DIR_PREFIX + "test4-%d{"
        + DATE_PATTERN + "}", tbrp1.getCurrentTime());

    for (int i = 0; i <= 2; i++) {
      rfa2.doAppend("Hello---" + i);
      tbrp2.setCurrentTime(addTime(tbrp2.getCurrentTime(), 100));
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
  public void test5() throws Exception {
    initRFA(rfa1, Constants.OUTPUT_DIR_PREFIX + "test5.log");
    initTRBP(rfa1, tbrp1, Constants.OUTPUT_DIR_PREFIX + "test5-%d{"
        + DATE_PATTERN + "}", currentTime);

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
  public void test6() throws Exception {
    initRFA(rfa1, Constants.OUTPUT_DIR_PREFIX + "test6.log");
    initTRBP(rfa1, tbrp1, Constants.OUTPUT_DIR_PREFIX + "test6-%d{"
        + DATE_PATTERN + "}.gz", currentTime);

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

  static long addTime(long currentTime, long timeToWait) {
    return currentTime + timeToWait;
  }

}
