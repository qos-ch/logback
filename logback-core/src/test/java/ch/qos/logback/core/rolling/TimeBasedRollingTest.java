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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import ch.qos.logback.core.layout.EchoLayout;
import ch.qos.logback.core.util.Compare;
import ch.qos.logback.core.util.Constants;

/**
 * A rather exhaustive set of tests. Tests include leaving the ActiveFileName
 * argument blank, or setting it, with and without compression, and tests with
 * or without stopping/restarting the RollingFileAppender.
 * 
 * The regression tests log a few times using a RollingFileAppender. Then, they
 * predict the names of the files which should be generated and compare them
 * with witness files.
 * 
 * <pre>
 *  Compression    ActiveFileName  Stop/Restart 
 *  Test1      NO              BLANK          NO
 *  Test2      NO              BLANK          YES
 *  Test3      YES             BLANK          NO
 *  Test4      NO                SET          YES 
 *  Test5      NO                SET          NO
 *  Test6      YES               SET          NO
 * </pre>
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class TimeBasedRollingTest extends TestCase {

  static final String DATE_PATTERN = "yyyy-MM-dd_HH_mm_ss";

  EchoLayout layout = new EchoLayout();

  public TimeBasedRollingTest(String name) {
    super(name);
  }

  public void setUp() {
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
   * Test rolling without compression, activeFileName left blank, no stop/start
   */
  public void test1() throws Exception {

    RollingFileAppender rfa = new RollingFileAppender();
    rfa.setLayout(layout);

    TimeBasedRollingPolicy tbrp = new TimeBasedRollingPolicy();
    tbrp.setFileNamePattern(Constants.TEST_DIR_PREFIX + "output/test1-%d{"
        + DATE_PATTERN + "}");
    tbrp.start();
    rfa.setRollingPolicy(tbrp);
    rfa.start();

    SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    String[] filenames = new String[3];

    Calendar cal = Calendar.getInstance();

    for (int i = 0; i < 3; i++) {
      filenames[i] = Constants.TEST_DIR_PREFIX + "output/test1-"
          + sdf.format(cal.getTime());
      cal.add(Calendar.SECOND, 1);
    }
    
    System.out.println("Waiting until next second and 100 millis.");
    delayUntilNextSecond(100);
    System.out.println("Done waiting.");
    
    long now = System.currentTimeMillis();
    for (int i = 0; i < 20; i++) {
      rfa.doAppend("Hello---" + i);
      Thread.sleep(50);
    }
    long newNow = System.currentTimeMillis();
    System.out.println("Done waiting. Waited: " + (newNow - now));
    
    for (int i = 0; i < 3; i++) {
      System.out.println(i + " expected filename [" + filenames[i] + "].");
    }

    for (int i = 0; i < 3; i++) {
      assertTrue(Compare.compare(filenames[i], Constants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test1." + i));
    }
  }

  /**
   * No compression, with stop/restart, activeFileName left blank
   */
  public void test2() throws Exception {
    RollingFileAppender rfa1 = new RollingFileAppender();
    rfa1.setLayout(layout);

    TimeBasedRollingPolicy tbrp1 = new TimeBasedRollingPolicy();
    tbrp1.setFileNamePattern(Constants.TEST_DIR_PREFIX + "output/test2-%d{"
        + DATE_PATTERN + "}");
    tbrp1.start();
    rfa1.setRollingPolicy(tbrp1);
    rfa1.start();

    SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    String[] filenames = new String[3];

    Calendar cal = Calendar.getInstance();

    for (int i = 0; i < 3; i++) {
      filenames[i] = Constants.TEST_DIR_PREFIX + "output/test2-"
          + sdf.format(cal.getTime());
      cal.add(Calendar.SECOND, 1);
    }

    System.out.println("Waiting until next second and 100 millis.");
    delayUntilNextSecond(100);
    System.out.println("Done waiting.");

    for (int i = 0; i <= 10; i++) {
      rfa1.doAppend("Hello---" + i);
      Thread.sleep(50);
    }

    rfa1.stop();

    RollingFileAppender rfa2 = new RollingFileAppender();
    rfa2.setLayout(layout);

    TimeBasedRollingPolicy tbrp2 = new TimeBasedRollingPolicy();
    tbrp2.setFileNamePattern(Constants.TEST_DIR_PREFIX + "output/test2-%d{"
        + DATE_PATTERN + "}");
    tbrp2.start();
    rfa2.setRollingPolicy(tbrp2);
    rfa2.start();

    for (int i = 3; i <= 10; i++) {
      rfa2.doAppend("Hello---" + i);
      Thread.sleep(50);
    }

    for (int i = 0; i < 3; i++) {
      assertTrue(Compare.compare(filenames[i], Constants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test2." + i));
    }
  }

  /**
   * With compression, activeFileName left blank, no stop/restart
   */
  public void test3() throws Exception {
    RollingFileAppender rfa = new RollingFileAppender();
    rfa.setLayout(layout);

    TimeBasedRollingPolicy tbrp = new TimeBasedRollingPolicy();
    tbrp.setFileNamePattern(Constants.TEST_DIR_PREFIX + "output/test3-%d{"
        + DATE_PATTERN + "}.gz");
    tbrp.start();
    rfa.setRollingPolicy(tbrp);
    rfa.start();

    SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    String[] filenames = new String[4];

    Calendar cal = Calendar.getInstance();

    for (int i = 0; i < 2; i++) {
      filenames[i] = Constants.TEST_DIR_PREFIX + "output/test3-"
          + sdf.format(cal.getTime()) + ".gz";
      cal.add(Calendar.SECOND, 1);
    }

    filenames[3] = "src/test/output/test3-" + sdf.format(cal.getTime());

    System.out.println("Waiting until next second and 100 millis.");
    delayUntilNextSecond(100);
    System.out.println("Done waiting.");

    for (int i = 0; i < 20; i++) {
      rfa.doAppend("Hello---" + i);
      Thread.sleep(50);
    }

//    for (int i = 0; i < 3; i++) {
//      System.out.println(i + " expected filename [" + filenames[i] + "].");
//    }

    for (int i = 0; i < 2; i++) {
      assertTrue(Compare.gzCompare(filenames[i], Constants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test3." + i + ".gz"));
    }

    assertTrue(Compare.compare(filenames[3], Constants.TEST_DIR_PREFIX
        + "witness/rolling/tbr-test3.2"));
  }

  /**
   * Without compression, activeFileName set, with stop/restart
   */
  public void test4() throws Exception {
    RollingFileAppender rfa1 = new RollingFileAppender();
    rfa1.setLayout(layout);

    TimeBasedRollingPolicy tbrp1 = new TimeBasedRollingPolicy();
    tbrp1.setActiveFileName(Constants.TEST_DIR_PREFIX + "output/test4.log");
    tbrp1.setFileNamePattern(Constants.TEST_DIR_PREFIX + "output/test4-%d{"
        + DATE_PATTERN + "}");
    tbrp1.start();
    rfa1.setRollingPolicy(tbrp1);
    rfa1.start();

    SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    String[] filenames = new String[3];

    Calendar cal = Calendar.getInstance();

    for (int i = 0; i < 2; i++) {
      filenames[i] = Constants.TEST_DIR_PREFIX + "output/test4-"
          + sdf.format(cal.getTime());
      cal.add(Calendar.SECOND, 1);
    }
    filenames[2] = Constants.TEST_DIR_PREFIX + "output/test4.log";

    System.out.println("Waiting until next second and 100 millis.");
    delayUntilNextSecond(100);
    System.out.println("Done waiting.");

    for (int i = 0; i <= 20; i++) {
      rfa1.doAppend("Hello---" + i);
      Thread.sleep(50);
    }

    rfa1.stop();

    RollingFileAppender rfa2 = new RollingFileAppender();
    rfa2.setLayout(layout);

    TimeBasedRollingPolicy tbrp2 = new TimeBasedRollingPolicy();
    tbrp2.setFileNamePattern(Constants.TEST_DIR_PREFIX + "output/test4-%d{"
        + DATE_PATTERN + "}");
    tbrp2.setActiveFileName(Constants.TEST_DIR_PREFIX + "output/test4.log");
    tbrp2.start();
    rfa2.setRollingPolicy(tbrp2);
    rfa2.start();

    for (int i = 1; i <= 5; i++) {
      rfa2.doAppend("Hello---" + i);
      Thread.sleep(20);
    }

    for (int i = 0; i < 3; i++) {
      assertTrue(Compare.compare(filenames[i], Constants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test4." + i));
    }
  }

  /**
   * No compression, activeFileName set, without stop/restart
   */
  public void test5() throws Exception {
    RollingFileAppender rfa = new RollingFileAppender();
    rfa.setLayout(layout);

    TimeBasedRollingPolicy tbrp = new TimeBasedRollingPolicy();
    tbrp.setFileNamePattern(Constants.TEST_DIR_PREFIX + "output/test5-%d{"
        + DATE_PATTERN + "}");
    tbrp.setActiveFileName(Constants.TEST_DIR_PREFIX + "output/test5.log");
    tbrp.start();
    rfa.setRollingPolicy(tbrp);
    rfa.start();

    SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    String[] filenames = new String[4];

    Calendar cal = Calendar.getInstance();

    for (int i = 0; i < 2; i++) {
      filenames[i] = Constants.TEST_DIR_PREFIX + "output/test5-"
          + sdf.format(cal.getTime());
      cal.add(Calendar.SECOND, 1);
    }

    filenames[2] = Constants.TEST_DIR_PREFIX + "output/test5.log";

    System.out.println("Waiting until next second and 100 millis.");
    delayUntilNextSecond(100);
    System.out.println("Done waiting.");

    for (int i = 0; i < 20; i++) {
      rfa.doAppend("Hello---" + i);
      Thread.sleep(50);
    }

    for (int i = 0; i < 3; i++) {
      assertTrue(Compare.compare(filenames[i], Constants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test5." + i));
    }
  }

  /**
   * With compression, activeFileName set, no stop/restart,
   */
  public void test6() throws Exception {
    RollingFileAppender rfa = new RollingFileAppender();
    rfa.setLayout(layout);

    TimeBasedRollingPolicy tbrp = new TimeBasedRollingPolicy();
    tbrp.setFileNamePattern(Constants.TEST_DIR_PREFIX + "output/test6-%d{"
        + DATE_PATTERN + "}.gz");
    tbrp.setActiveFileName(Constants.TEST_DIR_PREFIX + "output/test6.log");
    tbrp.start();
    rfa.setRollingPolicy(tbrp);
    rfa.start();

    SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    String[] filenames = new String[4];

    Calendar cal = Calendar.getInstance();

    for (int i = 0; i < 2; i++) {
      filenames[i] = Constants.TEST_DIR_PREFIX + "output/test6-"
          + sdf.format(cal.getTime()) + ".gz";
      cal.add(Calendar.SECOND, 1);
    }

    filenames[2] = Constants.TEST_DIR_PREFIX + "output/test6.log";

    System.out.println("Waiting until next second and 100 millis.");
    delayUntilNextSecond(100);
    System.out.println("Done waiting.");

    for (int i = 0; i < 20; i++) {
      rfa.doAppend("Hello---" + i);
      Thread.sleep(50);
    }

    for (int i = 0; i < 4; i++) {
      // System.out.println(i + " expected filename [" + filenames[i] + "].");
    }

    for (int i = 0; i < 2; i++) {
      assertTrue(Compare.gzCompare(filenames[i], Constants.TEST_DIR_PREFIX
          + "witness/rolling/tbr-test6." + i + ".gz"));
    }

    assertTrue(Compare.compare(filenames[2], Constants.TEST_DIR_PREFIX
        + "witness/rolling/tbr-test6.2"));
  }

  // public void testWithJoran1() throws Exception {
  // JoranConfigurator jc = new JoranConfigurator();
  // jc.doConfigure("./input/rolling/time1.xml",
  // LogManager.getLoggerRepository());
  // jc.dumpErrors();
  //    
  // String datePattern = "yyyy-MM-dd_HH_mm_ss";
  //
  // SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
  // String[] filenames = new String[4];
  //
  // Calendar cal = Calendar.getInstance();
  //
  // for (int i = 0; i < 4; i++) {
  // filenames[i] = "output/test1-" + sdf.format(cal.getTime());
  // cal.add(Calendar.SECOND, 1);
  // }
  //
  // System.out.println("Waiting until next second and 100 millis.");
  // delayUntilNextSecond(100);
  // System.out.println("Done waiting.");
  //
  // for (int i = 0; i < 5; i++) {
  // logger.debug("Hello---" + i);
  // Thread.sleep(500);
  // }
  //
  // for (int i = 0; i < 4; i++) {
  // //System.out.println(i + " expected filename [" + filenames[i] + "].");
  // }
  //
  // for (int i = 0; i < 4; i++) {
  // assertTrue(Compare.compare(filenames[i], "witness/rolling/tbr-test1." +
  // i));
  // }
  //    
  // }
  //  
  // public void XXXtestWithJoran10() throws Exception {
  // JoranConfigurator jc = new JoranConfigurator();
  // jc.doConfigure("./input/rolling/time2.xml",
  // LogManager.getLoggerRepository());
  // jc.dumpErrors();
  //    
  // String datePattern = "yyyy-MM-dd";
  //
  // SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
  // String[] filenames = new String[0];
  //
  // Calendar cal = Calendar.getInstance();
  //
  // filenames[0] = "output/test1-" + sdf.format(cal.getTime());
  //
  // for (int i = 0; i < 5; i++) {
  // logger.debug("Hello---" + i);
  // Thread.sleep(500);
  // }
  //
  //
  // for (int i = 0; i < 1; i++) {
  // assertTrue(Compare.compare(filenames[i], "witness/rolling/tbr-test10." +
  // i));
  // }
  //    
  // }

  void delayUntilNextSecond(int millis) {
    long now = System.currentTimeMillis();
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date(now));

    cal.set(Calendar.MILLISECOND, millis);
    cal.add(Calendar.SECOND, 1);

    long next = cal.getTime().getTime();

    try {
      Thread.sleep(next - now);
    } catch (Exception e) {
    }
  }

  void delayUntilNextMinute(int seconds) {
    long now = System.currentTimeMillis();
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date(now));

    cal.set(Calendar.SECOND, seconds);
    cal.add(Calendar.MINUTE, 1);

    long next = cal.getTime().getTime();

    try {
      Thread.sleep(next - now);
    } catch (Exception e) {
    }
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    // CompressTest requires external copying
    // suite.addTestSuite(CompressTest.class);
    // suite.addTest(new TimeBasedRollingTest("test1"));
    suite.addTestSuite(TimeBasedRollingTest.class);
    return suite;
  }

}
