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


import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class SizeAndTimeBasedFileNamingAndTriggeringPolicyTest extends
    ScaffoldingForRollingTests {

  SizeAndTimeBasedFileNamingAndTriggeringPolicy<Object> satbfnatPolicy = new SizeAndTimeBasedFileNamingAndTriggeringPolicy<Object>();
  RollingFileAppender<Object> rfa1 = new RollingFileAppender<Object>();
  TimeBasedRollingPolicy<Object> tbrp1 = new TimeBasedRollingPolicy<Object>();

  int fileSize = 0;
  int fileIndexCounter = 0;
  int sizeThreshold;

  @Before
  @Override
  public void setUp() {
    super.setUp();
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
    initTRBP(rfa1, tbrp1, randomOutputDir + testId + "-%d{"
        + DATE_PATTERN_WITH_SECONDS + "}-%i.txt", sizeThreshold, currentTime, 0);

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
    sortedContentCheck(randomOutputDir, runLength, prefix);
  }

  @Test
  public void noCompression_FileNotSet_NoRestart_2() throws Exception {
    String testId = "test1";
    initRFA(rfa1, null);
    sizeThreshold = 300;
    initTRBP(rfa1, tbrp1, randomOutputDir + testId + "-%d{"
        + DATE_PATTERN_WITH_SECONDS + "}-%i.txt", sizeThreshold, currentTime, 0);

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
    sortedContentCheck(randomOutputDir, runLength, prefix);
  }

  void massageExpectedFilesToCorresponToCurrentTarget(String file) {
    // we added one too many files by date
    expectedFilenameList.remove(expectedFilenameList.size() - 1);
    expectedFilenameList.add(file);
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

}
