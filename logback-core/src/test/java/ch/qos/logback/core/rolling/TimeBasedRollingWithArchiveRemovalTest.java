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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.layout.EchoLayout;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;

public class TimeBasedRollingWithArchiveRemovalTest {

  Context context = new ContextBase();
  EchoLayout<Object> layout = new EchoLayout<Object>();

  static final String MONTHLY_DATE_PATTERN = "yyyy-MM";
  static final String MONTHLY_CROLOLOG_DATE_PATTERN = "yyyy/MM";

  static final String DAILY_DATE_PATTERN = "yyyy-MM-dd";
  static final String DAILY_CROLOLOG_DATE_PATTERN = "yyyy/MM/dd";

  static final long MILLIS_IN_MINUTE = 60 * 1000;
  static final long MILLIS_IN_HOUR = 60 * MILLIS_IN_MINUTE;
  static final long MILLIS_IN_DAY = 24 * MILLIS_IN_HOUR;
  static final long MILLIS_IN_MONTH = 30 * MILLIS_IN_DAY;

  int diff = RandomUtil.getPositiveInt();
  protected String randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff
      + "/";
  int slashCount;

  // by default tbfnatp is an instance of
  // DefaultTimeBasedFileNamingAndTriggeringPolicy
  TimeBasedFileNamingAndTriggeringPolicy<Object> tbfnatp = new DefaultTimeBasedFileNamingAndTriggeringPolicy<Object>();

  @Before
  public void setUp()  {
    context.setName("test");
  }

  @After
  public void tearDown() throws Exception {
  }

  int computeSlashCount(String datePattern) {
    int fromIndex = 0;
    int count = 0;
    while (true) {
      int i = datePattern.indexOf('/', fromIndex);
      if (i == -1) {
        break;
      } else {
        count++;
        fromIndex = i + 1;
        if (fromIndex >= datePattern.length()) {
          break;
        }
      }
    }
    return count;
  }

  @Test
  public void montlyRollover() throws Exception {
    slashCount = computeSlashCount(MONTHLY_DATE_PATTERN);
    // large maxPeriod, a 3 times as many number of periods to simulate
    doRollover(randomOutputDir + "clean-%d{" + MONTHLY_DATE_PATTERN + "}.txt",
        MILLIS_IN_MONTH, 20, 20 * 3);
    check(expectedCountWithoutFolders(20));
  }

  @Test
  public void montlyRolloverOverManyPeriods() throws Exception {
    System.out.println("randomOutputDir=" + randomOutputDir);
    // small maxHistory, many periods
    slashCount = computeSlashCount(MONTHLY_CROLOLOG_DATE_PATTERN);
    int numPeriods = 40;
    int maxHistory = 2;

    doRollover(randomOutputDir + "/%d{" + MONTHLY_CROLOLOG_DATE_PATTERN
        + "}/clean.txt.zip", MILLIS_IN_MONTH, maxHistory, numPeriods);
    int beginPeriod = Calendar.getInstance().get(Calendar.MONTH);
    boolean extraFolder = extraFolder(numPeriods, 12, beginPeriod, maxHistory);
    check(expectedCountWithFolders(2, extraFolder));
  }

  @Test
  public void dailyRollover() throws Exception {
    slashCount = computeSlashCount(DAILY_DATE_PATTERN);
    doRollover(
        randomOutputDir + "clean-%d{" + DAILY_DATE_PATTERN + "}.txt.zip",
        MILLIS_IN_DAY, 5, 5 * 3);
    check(expectedCountWithoutFolders(5));
  }

  @Test
  public void dailyCronologRollover() throws Exception {
    slashCount = computeSlashCount(DAILY_CROLOLOG_DATE_PATTERN);
    doRollover(randomOutputDir + "/%d{" + DAILY_CROLOLOG_DATE_PATTERN
        + "}/clean.txt.zip", MILLIS_IN_DAY, 8, 8 * 3);
    int expectedDirMin = 9 + slashCount;
    int expectDirMax = expectedDirMin + 1 + 1; // plus 1 of stepping into a
    // new month, and another 1 into
    // a new year
    expectedFileAndDirCount(9, expectedDirMin, expectDirMax);
  }

  @Test
  public void dailySizeBasedRollover() throws Exception {
    SizeAndTimeBasedFNATP<Object> sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP<Object>();
    sizeAndTimeBasedFNATP.setMaxFileSize("10000");
    tbfnatp = sizeAndTimeBasedFNATP;

    slashCount = computeSlashCount(DAILY_DATE_PATTERN);
    doRollover(
        randomOutputDir + "/%d{" + DAILY_DATE_PATTERN + "}-clean.%i.zip",
        MILLIS_IN_DAY, 5, 5 * 4);

    // make .zip optional so that if for one reason or another, no size-based
    // rollover occurs on the last period, that the last period is still
    // accounted
    // for
    checkPatternCompliance(5 + 1 + slashCount,
        "\\d{4}-\\d{2}-\\d{2}-clean(\\.\\d)(.zip)?");
  }

  @Test
  public void dailyChronologSizeBasedRollover() throws Exception {
    SizeAndTimeBasedFNATP<Object> sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP<Object>();
    sizeAndTimeBasedFNATP.setMaxFileSize("10000");
    tbfnatp = sizeAndTimeBasedFNATP;

    slashCount = 1;
    doRollover(
        randomOutputDir + "/%d{" + DAILY_DATE_PATTERN + "}/clean.%i.zip",
        MILLIS_IN_DAY, 5, 5 * 4);
    checkDirPatternCompliance(6);
  }

  void doRollover(String fileNamePattern, long periodDurationInMillis,
      int maxHistory, int simulatedNumberOfPeriods) throws Exception {
    long currentTime = System.currentTimeMillis();

    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
    rfa.setContext(context);
    rfa.setLayout(layout);
    // rfa.setFile(Constants.OUTPUT_DIR_PREFIX + "clean.txt");
    TimeBasedRollingPolicy<Object> tbrp = new TimeBasedRollingPolicy<Object>();
    tbrp.setContext(context);
    tbrp.setFileNamePattern(fileNamePattern);

    tbrp.setMaxHistory(maxHistory);
    tbrp.setParent(rfa);
    tbrp.timeBasedTriggering = tbfnatp;
    tbrp.timeBasedTriggering.setCurrentTime(currentTime);
    tbrp.start();
    rfa.setRollingPolicy(tbrp);
    rfa.start();

    // lots of ticks per period
    int ticksPerPeriod = 512;
    long runLength = simulatedNumberOfPeriods * ticksPerPeriod;

    for (long i = 0; i < runLength; i++) {
      rfa
          .doAppend("Hello ----------------------------------------------------------"
              + i);
      tbrp.timeBasedTriggering.setCurrentTime(addTime(tbrp.timeBasedTriggering
          .getCurrentTime(), periodDurationInMillis / ticksPerPeriod));

      // wait every now and then for the compression job
      // otherwise, we might rollover a file for a given period before a previous
      // period's compressor had a chance to run
      if(i % (ticksPerPeriod/2) == 0) {
        waitForCompression(tbrp);
      }
    }
    waitForCompression(tbrp);
    rfa.stop();
  }

  void waitForCompression(TimeBasedRollingPolicy<Object> tbrp)
      throws InterruptedException, ExecutionException, TimeoutException {
    if (tbrp.future != null && !tbrp.future.isDone()) {
      tbrp.future.get(200, TimeUnit.MILLISECONDS);
    }
  }

  void findAllFoldersRecursively(File dir, List<File> fileList) {
    if (dir.isDirectory()) {
      File[] match = dir.listFiles(new FileFilter() {
        public boolean accept(File f) {
          return (f.isDirectory());
        }
      });
      for (File f : match) {
        fileList.add(f);
        findAllFoldersRecursively(f, fileList);
      }
    }
  }

  void findAllInFolderRecursivelyByStringContains(File dir,
      List<File> fileList, final String pattern) {
    if (dir.isDirectory()) {
      File[] match = dir.listFiles(new FileFilter() {
        public boolean accept(File f) {
          return (f.isDirectory() || f.getName().contains(pattern));
        }
      });
      for (File f : match) {
        fileList.add(f);
        if (f.isDirectory()) {
          findAllInFolderRecursivelyByStringContains(f, fileList, pattern);
        }
      }
    }
  }

  void findFilesInFolderRecursivelyByPatterMatch(File dir, List<File> fileList,
      final String pattern) {
    if (dir.isDirectory()) {
      File[] match = dir.listFiles(new FileFilter() {
        public boolean accept(File f) {
          return (f.isDirectory() || f.getName().matches(pattern));
        }
      });
      for (File f : match) {
        if (f.isDirectory()) {
          findFilesInFolderRecursivelyByPatterMatch(f, fileList, pattern);
        } else {
          fileList.add(f);
        }
      }
    }
  }

  void findFoldersInFolderRecursively(File dir, List<File> fileList) {
    if (dir.isDirectory()) {
      File[] match = dir.listFiles(new FileFilter() {
        public boolean accept(File f) {
          return f.isDirectory();
        }
      });
      for (File f : match) {
        fileList.add(f);
        findFoldersInFolderRecursively(f, fileList);
      }
    }
  }

  int expectedCountWithoutFolders(int maxHistory) {
    // maxHistory plus the currently active file
    return maxHistory + 1;
  }

  // sometimes, after a number of periods, there is an extra folder
  // from the previous "era" because the latest period - maxHistory, enters the
  // bound of the previous era. For example, with a maxHistory of 2, on 2009-09,
  // after 40 periods, the current period is 2013-01. Going back two months, the
  // year is 2012, and not 2013 (the current year).
  boolean extraFolder(int numPeriods, int periodsPerEra, int beginPeriod,
      int maxHistory) {
    int adjustedBegin = beginPeriod + 1;
    int remainder = ((adjustedBegin) + numPeriods) % periodsPerEra;
    return (remainder < maxHistory + 1);
  }

  int expectedCountWithFolders(int maxHistory, boolean extraFolder) {
    // each slash adds a new directory
    // + one file and one directory per archived log file
    int result = (maxHistory + 1) * 2 + slashCount;
    if (extraFolder)
      result++;
    return result;
  }

  void check(int expectedCount) {
    File dir = new File(randomOutputDir);
    List<File> fileList = new ArrayList<File>();
    findAllInFolderRecursivelyByStringContains(dir, fileList, "clean");
    assertEquals(expectedCount, fileList.size());
  }

  void expectedFileAndDirCount(int expectedFileAndDirCount,
      int expectedDirCountMin, int expectedDirCountMax) {
    File dir = new File(randomOutputDir);
    List<File> fileList = new ArrayList<File>();
    findFilesInFolderRecursivelyByPatterMatch(dir, fileList, "clean");

    List<File> dirList = new ArrayList<File>();
    findAllFoldersRecursively(dir, dirList);
    assertTrue("expectedDirCountMin=" + expectedDirCountMin
        + ", expectedDirCountMax=" + expectedDirCountMax + " actual value="
        + dirList.size(), expectedDirCountMin <= dirList.size()
        && dirList.size() <= expectedDirCountMax);
  }

  void checkPatternCompliance(int expectedClassCount, String regex) {
    File dir = new File(randomOutputDir);
    List<File> fileList = new ArrayList<File>();
    findFilesInFolderRecursivelyByPatterMatch(dir, fileList, regex);
    Set<String> set = groupByClass(fileList, regex);
    assertEquals(expectedClassCount, set.size());
  }

  void checkDirPatternCompliance(int expectedClassCount) {
    File dir = new File(randomOutputDir);
    List<File> fileList = new ArrayList<File>();
    findFoldersInFolderRecursively(dir, fileList);
    for (File f : fileList) {
      assertTrue(f.list().length >= 1);
    }
    assertEquals(expectedClassCount, fileList.size());
  }

  // reduce file names differing by index number into the same group
  // for example, 2009-11-01-clean.0.zip, 2009-11-01-clean.1.zip and
  // 2009-11-01-clean-2 are reduced into the same string (group)
  // 2009-11-01-clean
  Set<String> groupByClass(List<File> fileList, String regex) {
    Pattern p = Pattern.compile(regex);
    Set<String> set = new HashSet<String>();
    for (File f : fileList) {
      String n = f.getName();
      Matcher m = p.matcher(n);
      m.matches();
      int begin = m.start(1);
      String reduced = n.substring(0, begin);
      set.add(reduced);
    }
    System.out.println(set);
    return set;
  }

  static long addTime(long currentTime, long timeToWait) {
    return currentTime + timeToWait;
  }

}
