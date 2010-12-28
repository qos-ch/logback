/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2010, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.rolling

import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ch.qos.logback.core.util.Compare
import ch.qos.logback.core.util.CoreTestConstants
import ch.qos.logback.core.encoder.EchoEncoder
import java.io.File

class TimeBasedRolling2Test extends RollingScaffolding {

  private[rolling] var rfa1: RollingFileAppender[AnyRef] = new RollingFileAppender[AnyRef]
  private[rolling] var tbrp1: TimeBasedRollingPolicy[AnyRef] = new TimeBasedRollingPolicy[AnyRef]
  private[rolling] var rfa2: RollingFileAppender[AnyRef] = new RollingFileAppender[AnyRef]
  private[rolling] var tbrp2: TimeBasedRollingPolicy[AnyRef] = new TimeBasedRollingPolicy[AnyRef]

  private[rolling] var encoder: EchoEncoder[AnyRef] = new EchoEncoder[AnyRef]

  @Before
  def setUp: Unit = {
    setUpScaffolding
  }

  private[rolling] def initRFA(rfa: RollingFileAppender[AnyRef], filename: String): Unit = {
    rfa.setContext(context)
    rfa.setEncoder(encoder)
    if (filename != null) {
      rfa.setFile(filename)
    }
  }

  private[rolling] def initTRBP(rfa: RollingFileAppender[AnyRef], tbrp: TimeBasedRollingPolicy[AnyRef], filenamePattern: String, givenTime: Long): Unit = {
    tbrp.setContext(context)
    tbrp.setFileNamePattern(filenamePattern)
    tbrp.setParent(rfa)
    tbrp.timeBasedFileNamingAndTriggeringPolicy = new DefaultTimeBasedFileNamingAndTriggeringPolicy[AnyRef]
    tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(givenTime)
    rfa.setRollingPolicy(tbrp)
    tbrp.start
    rfa.start
  }

  def genericTest(testId: String, compressionSuffix: String, fileOptionIsSet: Boolean, waitDuration: Int): Unit = {
    val withCompression = compressionSuffix.length > 0
    val fileName = if (fileOptionIsSet) testId2FileName(testId) else null;
    initRFA(rfa1, fileName);

    initTRBP(rfa1, tbrp1, randomOutputDir + testId + "-%d{"
      + DATE_PATTERN_WITH_SECONDS + "}" + compressionSuffix, currentTime);

    // compute the current filename
    addExpectedFileName_ByDate(randomOutputDir, testId, getDateOfCurrentPeriodsStart, withCompression);

    incCurrentTime(1100);
    tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);

    for (i <- 0 until 3) {
      rfa1.doAppend("Hello---" + i);
      addExpectedFileNamedIfItsTime_ByDate(randomOutputDir, testId, withCompression && (i != 2))
      incCurrentTime(500);
      tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime)
      if (withCompression)
        waitForCompression(tbrp1)
    }
    rfa1.stop

    if (waitDuration != 0) {
      doRestart(testId, fileOptionIsSet, waitDuration);
    }

    if (fileOptionIsSet) {
      massageExpectedFilesToCorresponToCurrentTarget(testId + ".log")
    }

    var i = 0;
    for (fn <- expectedFilenameList) {
      val suffix: String = if (withCompression) addGZIfNotLast(i, compressionSuffix) else ""
      val witnessFileName: String = CoreTestConstants.TEST_DIR_PREFIX + "witness/rolling/tbr-" + testId + "." + i.toString + suffix
      assertTrue(Compare.compare(fn, witnessFileName));
      i += 1
    }

  }

  def doRestart(testId: String, fileOptionIsSet: Boolean, waitDuration: Int) {
    // change the timestamp of the currently actively file
    var activeFile: File = new File(rfa1.getFile)
    activeFile.setLastModified(currentTime)

    incCurrentTime(waitDuration)

    val fileName = if (fileOptionIsSet) testId2FileName(testId) else null;
    initRFA(rfa2, fileName)
    initTRBP(rfa2, tbrp2, randomOutputDir + testId + "-%d{" + DATE_PATTERN_WITH_SECONDS + "}", currentTime)
    for (i <- 0 until 3) {
      rfa2.doAppend("World---" + i)
      addExpectedFileNamedIfItsTime_ByDate(randomOutputDir, testId, false)
      incCurrentTime(100)
      tbrp2.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime)
    }
  }

  val NO_RESTART = 0
  val WITH_RESTART = 1
  val WITH_RESTART_AND_LONG_WAIT = 2000

  @Test
  def noCompression_FileBlank_NoRestart_1 = {
    genericTest("test1", "", FILE_OPTION_BLANK, NO_RESTART)
  }

  @Test
  def withCompression_FileBlank_NoRestart_2 = {
    genericTest("test2", ".gz", FILE_OPTION_BLANK, NO_RESTART);
  }

  @Test
  def noCompression_FileBlank_StopRestart_3 = {
    genericTest("test3", "", FILE_OPTION_BLANK, WITH_RESTART);
  }

  @Test
  def noCompression_FileSet_StopRestart_4 = {
    genericTest("test4", "", FILE_OPTION_SET, WITH_RESTART);
  }

  @Test
  def noCompression_FileSet_StopRestart_WithLongWait_4B = {
    genericTest("test4B", "", FILE_OPTION_SET, WITH_RESTART_AND_LONG_WAIT);
  }

  @Test
  def noCompression_FileSet_NoRestart_5 = {
    genericTest("test5", "", FILE_OPTION_SET, NO_RESTART);
  }

  @Test
  def withCompression_FileSet_NoRestart_6 = {
    genericTest("test6", ".gz", FILE_OPTION_SET, NO_RESTART);
  }

  @Test
  def withMissingTargetDir = {
    genericTest("missingTargetDir", "", FILE_OPTION_SET, NO_RESTART);
  }
}