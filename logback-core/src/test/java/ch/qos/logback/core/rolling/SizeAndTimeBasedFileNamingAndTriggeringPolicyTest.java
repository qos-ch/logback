/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.rolling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.layout.EchoLayout;
import ch.qos.logback.core.testUtil.FileToBufferUtil;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;

public class SizeAndTimeBasedFileNamingAndTriggeringPolicyTest {
  static final String DATE_PATTERN = "yyyy-MM-dd_HH_mm_ss";

  int diff = RandomUtil.getPositiveInt();
  String randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/";

  SizeAndTimeBasedFileNamingAndTriggeringPolicy<Object> satbfnatPolicy = new SizeAndTimeBasedFileNamingAndTriggeringPolicy<Object>();

  SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

  EchoLayout<Object> layout = new EchoLayout<Object>();
  Context context = new ContextBase();

  RollingFileAppender<Object> rfa1 = new RollingFileAppender<Object>();
  TimeBasedRollingPolicy<Object> tbrp1 = new TimeBasedRollingPolicy<Object>();
  Calendar cal = Calendar.getInstance();
  long currentTime; // initialized in setUp()
  long nextRolloverThreshold; // initialized in setUp()
  List<String> expectedFilenameList = new ArrayList<String>();

  int fileSize = 0;
  int fileIndexCounter = 0;
  int sizeThreshold;

  @Before
  public void setUp() {
    context.setName("test");
    cal.set(Calendar.MILLISECOND, 333);
    currentTime = cal.getTimeInMillis();
    recomputeRolloverThreshold(currentTime);
    System.out.println(randomOutputDir);
    // System.out.println("at setUp() currentTime="
    // + sdf.format(new Date(currentTime)));

  }

  // assuming rollover every second
  void recomputeRolloverThreshold(long ct) {
    long delta = ct % 1000;
    nextRolloverThreshold = (ct - delta) + 1000;
  }

  void initRFA(RollingFileAppender<Object> rfa, String filename) {
    rfa.setContext(context);
    rfa.setLayout(layout);
    if (filename != null) {
      rfa.setFile(filename);
    }
  }

  void initTRBP(RollingFileAppender<Object> rfa,
      TimeBasedRollingPolicy<Object> tbrp, String filenamePattern,
      int sizeThreshold, long givenTime, long lastCheck) {

    tbrp.setContext(context);
    satbfnatPolicy.setMaxFileSize("" + sizeThreshold);
    tbrp.setTimeBasedTriggering(satbfnatPolicy);
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

  @Test
  public void noCompression_FileBSet_NoRestart_1() throws Exception {
    String testId = "test1";
    String file = randomOutputDir + "toto.log";
    initRFA(rfa1, file);
    sizeThreshold = 300;
    initTRBP(rfa1, tbrp1, randomOutputDir + testId + "-%d{" + DATE_PATTERN
        + "}-%i.txt", sizeThreshold, currentTime, 0);

    addExpectedFileName(testId, getDateOfCurrentPeriodsStart(),
        fileIndexCounter, false);

    incCurrentTime(100);
    tbrp1.timeBasedTriggering.setCurrentTime(currentTime);

    int runLength = 100;
    String prefix = "Hello -----------------";
    
    for (int i = 0; i < runLength; i++) {
      String msg = prefix + i;
      rfa1.doAppend(msg);
      addExpectedFileNamedIfItsTime(testId, msg, false);
      incCurrentTime(20);
      tbrp1.timeBasedTriggering.setCurrentTime(currentTime);
    }

    massageExpectedFilesToCorresponToCurrentTarget(file);
    existenceCheck(expectedFilenameList);
    contentCheck(runLength, prefix);
  }

  @Test
  public void noCompression_FileNotSet_NoRestart_2() throws Exception {
    String testId = "test1";
    initRFA(rfa1, null);
    sizeThreshold = 300;
    initTRBP(rfa1, tbrp1, randomOutputDir + testId + "-%d{" + DATE_PATTERN
        + "}-%i.txt", sizeThreshold, currentTime, 0);

    addExpectedFileName(testId, getDateOfCurrentPeriodsStart(),
        fileIndexCounter, false);

    incCurrentTime(100);
    tbrp1.timeBasedTriggering.setCurrentTime(currentTime);

    int runLength = 100;
    String prefix = "Hello -----------------";
    
    for (int i = 0; i < runLength; i++) {
      String msg = prefix + i;
      rfa1.doAppend(msg);
      addExpectedFileNamedIfItsTime(testId, msg, false);
      incCurrentTime(20);
      tbrp1.timeBasedTriggering.setCurrentTime(currentTime);
    }

    existenceCheck(expectedFilenameList);
    contentCheck(runLength, prefix);
  }

  
  void existenceCheck(String filename) {
    assertTrue("File " + filename + " does not exist", new File(filename)
        .exists());
  }

  void contentCheck(int runLength, String prefix) throws IOException {
    File outputDir = new File(randomOutputDir);
    File[] fileArray = outputDir.listFiles();
    List<String> stringList = new ArrayList<String>();
    for (File file : fileArray) {
      FileToBufferUtil.readIntoList(file, stringList);
    }
    
    List<String> witnessList = new ArrayList<String>();
    
    for(int i = 0; i < runLength; i++) {
      witnessList.add(prefix+i);
    }
    assertEquals(witnessList, stringList);
  }

  void existenceCheck(List<String> filenameList) {
    for (String filename : filenameList) {
      assertTrue("File " + filename + " does not exist", new File(filename)
          .exists());
    }
  }

  void massageExpectedFilesToCorresponToCurrentTarget(String file) {
    // we added one too many files by date
    expectedFilenameList.remove(expectedFilenameList.size() - 1);
    expectedFilenameList.add(file);
  }

  boolean passThresholdTime(long nextRolloverThreshold) {
    return currentTime >= nextRolloverThreshold;
  }

  void addExpectedFileNamedIfItsTime(String testId, String msg,
      boolean gzExtension) {
    fileSize += msg.getBytes().length;

    if (passThresholdTime(nextRolloverThreshold)) {
      fileIndexCounter = 0;
      fileSize = 0;
      addExpectedFileName(testId, getDateOfCurrentPeriodsStart(),
          fileIndexCounter, gzExtension);
      recomputeRolloverThreshold(currentTime);
      return;
    }

    // windows can delay file size changes, so we only allow for
    // fileIndexCounter 0 and 1
    if ((fileIndexCounter < 1) && fileSize > sizeThreshold) {
      addExpectedFileName(testId, getDateOfCurrentPeriodsStart(),
          ++fileIndexCounter, gzExtension);
      fileSize = 0;
      return;
    }

  }

  void addExpectedFileName(String testId, Date date, int fileIndexCounter,
      boolean gzExtension) {

    String fn = randomOutputDir + testId + "-" + sdf.format(date) + "-"
        + fileIndexCounter + ".txt";
    if (gzExtension) {
      fn += ".gz";
    }
    expectedFilenameList.add(fn);
  }

  Date getDateOfCurrentPeriodsStart() {
    long delta = currentTime % 1000;
    return new Date(currentTime - delta);
  }

  void incCurrentTime(long increment) {
    currentTime += increment;
  }
}
