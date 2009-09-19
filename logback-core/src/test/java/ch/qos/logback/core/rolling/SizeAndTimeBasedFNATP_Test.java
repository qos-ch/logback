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


import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.util.StatusPrinter;

public class SizeAndTimeBasedFNATP_Test extends
    ScaffoldingForRollingTests {

  SizeAndTimeBasedFNATP<Object> sizeAndTimeBasedFNATP;
  RollingFileAppender<Object> rfa1 = new RollingFileAppender<Object>();
  TimeBasedRollingPolicy<Object> tbrp1 = new TimeBasedRollingPolicy<Object>();

  RollingFileAppender<Object> rfa2 = new RollingFileAppender<Object>();
  TimeBasedRollingPolicy<Object> tbrp2 = new TimeBasedRollingPolicy<Object>();

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

    sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP<Object>();
    tbrp.setContext(context);
    sizeAndTimeBasedFNATP.setMaxFileSize("" + sizeThreshold);
    tbrp.setTimeBasedFileNamingAndTriggeringPolicy(sizeAndTimeBasedFNATP);
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
  public void noCompression_FileSet_NoRestart_1() throws Exception {
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
  public void noCompression_FileBlank_NoRestart_2() throws Exception {
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

  @Test
  public void noCompression_FileBlank_WithStopStart_3() throws Exception {
    String testId = "test3";
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
    
    int i = 0;
    
    for (; i < runLength; i++) {
      String msg = prefix + i;
      rfa1.doAppend(msg);
      addExpectedFileNamedIfItsTime(testId, msg, false);
      incCurrentTime(20);
      tbrp1.timeBasedTriggering.setCurrentTime(currentTime);
    }
    rfa1.stop();
    initRFA(rfa2, null);
    initTRBP(rfa2, tbrp2, randomOutputDir + testId + "-%d{"
        + DATE_PATTERN_WITH_SECONDS + "}-%i.txt", sizeThreshold, currentTime, 0);

    runLength *= 2;
    for (; i < runLength; i++) {
      String msg = prefix + i;
      addExpectedFileNamedIfItsTime(testId, msg, false);
      rfa2.doAppend(msg);
      incCurrentTime(100);
      tbrp2.timeBasedTriggering.setCurrentTime(currentTime);
    }
    StatusPrinter.print(context);
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

    String fn = randomOutputDir + testId + "-" + SDF.format(date) + "-"
        + fileIndexCounter + ".txt";
    if (gzExtension) {
      fn += ".gz";
    }
    expectedFilenameList.add(fn);
  }

}
