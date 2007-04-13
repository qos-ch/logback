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

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.layout.EchoLayout;
import ch.qos.logback.core.util.Compare;
import ch.qos.logback.core.util.Constants;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
public class TimeBasedRollingTest extends TestCase {

  static final String DATE_PATTERN = "yyyy-MM-dd_HH_mm_ss";

  EchoLayout<Object> layout = new EchoLayout<Object>();
  Context context = new ContextBase();

  public TimeBasedRollingTest(String name) {
    super(name);
  }

  public void setUp() {
    context.setName("test");
    // Delete .log files
    {
      File target = new File(Constants.TEST_DIR_PREFIX + "output/test4.log");
      target.mkdirs();
      target.delete();
    }
    {
      File target = new File(Constants.TEST_DIR_PREFIX + "output/test5.log");
      target.mkdirs();
      target.delete();
    }
    {
      File target = new File(Constants.TEST_DIR_PREFIX + "output/test6.log");
      target.mkdirs();
      target.delete();
    }
  }

  public void tearDown() {
  }

  /**
   * Test rolling without compression, file option left blank, no stop/start
   */
  public void test1() throws Exception {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MILLISECOND, 0);
    long currentTime = cal.getTimeInMillis();

    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
    rfa.setContext(context);
    rfa.setLayout(layout);

    TimeBasedRollingPolicy tbrp = new TimeBasedRollingPolicy();
    tbrp.setContext(context);
    tbrp.setFileNamePattern(Constants.TEST_DIR_PREFIX + "output/test1-%d{"
        + DATE_PATTERN + "}");
    tbrp.setParent(rfa);
    tbrp.setCurrentTime(currentTime);
    tbrp.start();
    rfa.setRollingPolicy(tbrp);
    rfa.start();

    SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    String[] filenames = new String[3];

    for (int i = 0; i < 3; i++) {
      filenames[i] = Constants.TEST_DIR_PREFIX + "output/test1-"
          + sdf.format(cal.getTime());
      cal.add(Calendar.SECOND, 1);
    }

    // System.out.println("Waiting until next second and 100 millis.");
    tbrp.setCurrentTime(addTime(currentTime, 1100));
    // System.out.println("Done waiting.");

    for (int i = 0; i < 3; i++) {
      rfa.doAppend("Hello---" + i);
      tbrp.setCurrentTime(addTime(tbrp.getCurrentTime(), 500));
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
  public void test2() throws Exception {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MILLISECOND, 0);
    long currentTime = cal.getTimeInMillis();

    RollingFileAppender<Object> rfa1 = new RollingFileAppender<Object>();
    rfa1.setContext(context);
    rfa1.setLayout(layout);

    TimeBasedRollingPolicy tbrp1 = new TimeBasedRollingPolicy();
    tbrp1.setFileNamePattern(Constants.TEST_DIR_PREFIX + "output/test2-%d{"
        + DATE_PATTERN + "}");
    tbrp1.setContext(context);
    tbrp1.setParent(rfa1);
    tbrp1.setCurrentTime(currentTime);
    tbrp1.start();
    rfa1.setRollingPolicy(tbrp1);
    rfa1.start();

    SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    String[] filenames = new String[3];

    for (int i = 0; i < 3; i++) {
      filenames[i] = Constants.TEST_DIR_PREFIX + "output/test2-"
          + sdf.format(cal.getTime());
      cal.add(Calendar.SECOND, 1);
    }

    // System.out.println("Waiting until next second and 100 millis.");
    tbrp1.setCurrentTime(addTime(currentTime, 1100));
    // System.out.println("Done waiting.");

    for (int i = 0; i <= 2; i++) {
      rfa1.doAppend("Hello---" + i);
      tbrp1.setCurrentTime(addTime(tbrp1.getCurrentTime(), 500));
      // Thread.sleep(500);
    }

    rfa1.stop();

    RollingFileAppender<Object> rfa2 = new RollingFileAppender<Object>();
    rfa2.setContext(context);
    rfa2.setLayout(layout);

    TimeBasedRollingPolicy tbrp2 = new TimeBasedRollingPolicy();
    tbrp2.setContext(context);
    tbrp2.setFileNamePattern(Constants.TEST_DIR_PREFIX + "output/test2-%d{"
        + DATE_PATTERN + "}");
    tbrp2.setParent(rfa2);
    tbrp2.setCurrentTime(tbrp1.getCurrentTime());
    tbrp2.start();
    rfa2.setRollingPolicy(tbrp2);
    rfa2.start();

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
  public void test3() throws Exception {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MILLISECOND, 0);
    long currentTime = cal.getTimeInMillis();

    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
    rfa.setContext(context);
    rfa.setLayout(layout);

    TimeBasedRollingPolicy tbrp = new TimeBasedRollingPolicy();
    tbrp.setContext(context);
    tbrp.setFileNamePattern(Constants.TEST_DIR_PREFIX + "output/test3-%d{"
        + DATE_PATTERN + "}.gz");
    tbrp.setParent(rfa);
    tbrp.setCurrentTime(currentTime);
    tbrp.start();
    rfa.setRollingPolicy(tbrp);
    rfa.start();

    SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    String[] filenames = new String[4];

    for (int i = 0; i < 2; i++) {
      filenames[i] = Constants.TEST_DIR_PREFIX + "output/test3-"
          + sdf.format(cal.getTime()) + ".gz";
      cal.add(Calendar.SECOND, 1);
    }

    filenames[3] = Constants.TEST_DIR_PREFIX + "output/test3-"
        + sdf.format(cal.getTime());

    // System.out.println("Waiting until next second and 100 millis.");
    tbrp.setCurrentTime(addTime(currentTime, 1100));
    // System.out.println("Done waiting.");

    for (int i = 0; i < 3; i++) {
      rfa.doAppend("Hello---" + i);
      tbrp.setCurrentTime(addTime(tbrp.getCurrentTime(), 500));
    }

    // for (int i = 0; i < 3; i++) {
    // System.out.println(i + " expected filename [" + filenames[i] + "].");
    // }

    for (int i = 0; i < 2; i++) {
      assertTrue(Compare.gzCompare(filenames[i], Constants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test3." + i + ".gz"));
    }

    assertTrue(Compare.compare(filenames[3], Constants.TEST_DIR_PREFIX
        + "witness/rolling/tbr-test3.2"));
  }

  /**
   * Without compression, file option set, with stop/restart
   */
  public void test4() throws Exception {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MILLISECOND, 0);
    long currentTime = cal.getTimeInMillis();

    RollingFileAppender<Object> rfa1 = new RollingFileAppender<Object>();
    rfa1.setContext(context);
    rfa1.setLayout(layout);
    rfa1.setFile(Constants.TEST_DIR_PREFIX + "output/test4.log");

    TimeBasedRollingPolicy tbrp1 = new TimeBasedRollingPolicy();
    tbrp1.setContext(context);
    // tbrp1.setActiveFileName(Constants.TEST_DIR_PREFIX + "output/test4.log");
    tbrp1.setFileNamePattern(Constants.TEST_DIR_PREFIX + "output/test4-%d{"
        + DATE_PATTERN + "}");
    tbrp1.setParent(rfa1);
    tbrp1.setCurrentTime(currentTime);
    tbrp1.start();
    rfa1.setRollingPolicy(tbrp1);
    rfa1.start();

    SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    String[] filenames = new String[3];

    for (int i = 0; i < 2; i++) {
      filenames[i] = Constants.TEST_DIR_PREFIX + "output/test4-"
          + sdf.format(cal.getTime());
      cal.add(Calendar.SECOND, 1);
    }
    filenames[2] = Constants.TEST_DIR_PREFIX + "output/test4.log";

    // System.out.println("Waiting until next second and 100 millis.");
    tbrp1.setCurrentTime(addTime(currentTime, 1100));
    // System.out.println("Done waiting.");

    for (int i = 0; i <= 2; i++) {
      rfa1.doAppend("Hello---" + i);
      tbrp1.setCurrentTime(addTime(tbrp1.getCurrentTime(), 500));
    }

    rfa1.stop();

    RollingFileAppender<Object> rfa2 = new RollingFileAppender<Object>();
    rfa2.setContext(context);
    rfa2.setLayout(layout);
    rfa2.setFile(Constants.TEST_DIR_PREFIX + "output/test4.log");

    TimeBasedRollingPolicy tbrp2 = new TimeBasedRollingPolicy();
    tbrp2.setContext(context);
    tbrp2.setFileNamePattern(Constants.TEST_DIR_PREFIX + "output/test4-%d{"
        + DATE_PATTERN + "}");
    // tbrp2.setActiveFileName(Constants.TEST_DIR_PREFIX + "output/test4.log");
    tbrp2.setParent(rfa2);
    tbrp2.setCurrentTime(tbrp1.getCurrentTime());
    tbrp2.start();
    rfa2.setRollingPolicy(tbrp2);
    rfa2.start();

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
  public void test5() throws Exception {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MILLISECOND, 0);
    long currentTime = cal.getTimeInMillis();

    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
    rfa.setContext(context);
    rfa.setLayout(layout);
    rfa.setFile(Constants.TEST_DIR_PREFIX + "output/test5.log");

    TimeBasedRollingPolicy tbrp = new TimeBasedRollingPolicy();
    tbrp.setContext(context);
    tbrp.setFileNamePattern(Constants.TEST_DIR_PREFIX + "output/test5-%d{"
        + DATE_PATTERN + "}");
    // tbrp.setActiveFileName(Constants.TEST_DIR_PREFIX + "output/test5.log");
    tbrp.setParent(rfa);
    tbrp.setCurrentTime(currentTime);
    tbrp.start();
    rfa.setRollingPolicy(tbrp);
    rfa.start();

    SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    String[] filenames = new String[4];

    for (int i = 0; i < 2; i++) {
      filenames[i] = Constants.TEST_DIR_PREFIX + "output/test5-"
          + sdf.format(cal.getTime());
      cal.add(Calendar.SECOND, 1);
    }

    filenames[2] = Constants.TEST_DIR_PREFIX + "output/test5.log";

    // System.out.println("Waiting until next second and 100 millis.");
    tbrp.setCurrentTime(addTime(currentTime, 1100));
    // System.out.println("Done waiting.");

    for (int i = 0; i < 3; i++) {
      rfa.doAppend("Hello---" + i);
      tbrp.setCurrentTime(addTime(tbrp.getCurrentTime(), 500));
    }

    for (int i = 0; i < 3; i++) {
      assertTrue(Compare.compare(filenames[i], Constants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test5." + i));
    }
  }

  /**
   * With compression, file option set, no stop/restart,
   */
  public void test6() throws Exception {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MILLISECOND, 0);
    long currentTime = cal.getTimeInMillis();

    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
    rfa.setContext(context);
    rfa.setLayout(layout);
    rfa.setFile(Constants.TEST_DIR_PREFIX + "output/test6.log");

    TimeBasedRollingPolicy tbrp = new TimeBasedRollingPolicy();
    tbrp.setContext(context);
    tbrp.setFileNamePattern(Constants.TEST_DIR_PREFIX + "output/test6-%d{"
        + DATE_PATTERN + "}.gz");
    // tbrp.setActiveFileName(Constants.TEST_DIR_PREFIX + "output/test6.log");
    tbrp.setParent(rfa);
    tbrp.setCurrentTime(currentTime);
    tbrp.start();
    rfa.setRollingPolicy(tbrp);
    rfa.start();

    SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    String[] filenames = new String[4];

    for (int i = 0; i < 2; i++) {
      filenames[i] = Constants.TEST_DIR_PREFIX + "output/test6-"
          + sdf.format(cal.getTime()) + ".gz";
      cal.add(Calendar.SECOND, 1);
    }

    filenames[2] = Constants.TEST_DIR_PREFIX + "output/test6.log";

    // System.out.println("Waiting until next second and 100 millis.");
    tbrp.setCurrentTime(addTime(currentTime, 1100));
    // System.out.println("Done waiting.");

    for (int i = 0; i < 3; i++) {
      rfa.doAppend("Hello---" + i);
      tbrp.setCurrentTime(addTime(tbrp.getCurrentTime(), 500));
    }

    // for (int i = 0; i < 4; i++) {
    // System.out.println(i + " expected filename [" + filenames[i] + "].");
    // }

    for (int i = 0; i < 2; i++) {
      assertTrue(Compare.gzCompare(filenames[i], Constants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test6." + i + ".gz"));
    }

    assertTrue(Compare.compare(filenames[2], Constants.TEST_DIR_PREFIX
        + "witness/rolling/tbr-test6.2"));
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(TimeBasedRollingTest.class);
    return suite;
  }

  static long addTime(long currentTime, long timeToWait) {
    return currentTime + timeToWait;
  }

}
