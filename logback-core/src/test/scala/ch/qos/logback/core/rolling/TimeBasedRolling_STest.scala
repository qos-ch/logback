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
import ch.qos.logback.core.encoder.EchoEncoder
import java.io.{FileOutputStream, File}
import ch.qos.logback.core.util.{StatusPrinter, Compare, CoreTestConstants}
import ch.qos.logback.core.testUtil.Env
import java.security.PrivateKey

class TimeBasedRolling_STest extends RollingScaffolding {

  private var rfa1: RollingFileAppender[AnyRef] = new RollingFileAppender[AnyRef]
  private var tbrp1: TimeBasedRollingPolicy[AnyRef] = new TimeBasedRollingPolicy[AnyRef]
  private var rfa2: RollingFileAppender[AnyRef] = new RollingFileAppender[AnyRef]
  private var tbrp2: TimeBasedRollingPolicy[AnyRef] = new TimeBasedRollingPolicy[AnyRef]

  private var encoder: EchoEncoder[AnyRef] = new EchoEncoder[AnyRef]

  @Before
  def setUp: Unit = {
    setUpScaffolding
  }

  private def initRFA(rfa: RollingFileAppender[AnyRef], filename: String): Unit = {
    rfa.setContext(context)
    rfa.setEncoder(encoder)
    if (filename != null) {
      rfa.setFile(filename)
    }
  }

 private def initTRBP(rfa: RollingFileAppender[AnyRef], tbrp: TimeBasedRollingPolicy[AnyRef], filenamePattern: String, givenTime: Long): Unit = {
    tbrp.setContext(context)
    tbrp.setFileNamePattern(filenamePattern)
    tbrp.setParent(rfa)
    tbrp.timeBasedFileNamingAndTriggeringPolicy = new DefaultTimeBasedFileNamingAndTriggeringPolicy[AnyRef]
    tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(givenTime)
    rfa.setRollingPolicy(tbrp)
    tbrp.start
    rfa.start
  }

  type CheckFunction = ((String, Boolean, String) => Unit)
  def genericTest(checkFunction: CheckFunction)(testId: String, compressionSuffix: String, fileOptionIsSet: Boolean, waitDuration: Int): Unit = {
    val withCompression = compressionSuffix.length > 0
    val fileName = if (fileOptionIsSet) testId2FileName(testId) else null;
    initRFA(rfa1, fileName);

    initTRBP(rfa1, tbrp1, randomOutputDir + testId + "-%d{"
      + DATE_PATTERN_WITH_SECONDS + "}" + compressionSuffix, currentTime);

    // compute the current filename
    addExpectedFileName_ByDate(randomOutputDir, testId, getMillisOfCurrentPeriodsStart, withCompression);

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
      massageExpectedFilesToCorresponToCurrentTarget(fileName)
    }

    checkFunction(testId, withCompression, compressionSuffix)
  }

  // defaultTest uses the defaultCheck function
  val defaultTest = genericTest(defaultCheck)_

  def defaultCheck(testId: String, withCompression: Boolean, compressionSuffix:String) = {
    var i = 0;
    for (fn <- expectedFilenameList) {
      val suffix: String = if (withCompression) addGZIfNotLast(i, compressionSuffix) else ""
      val witnessFileName: String = CoreTestConstants.TEST_DIR_PREFIX + "witness/rolling/tbr-" + testId + "." + i.toString + suffix
      assertTrue(Compare.compare(fn, witnessFileName));
      i += 1
    }
  }

  def zCheck(testId: String, withCompression: Boolean, compressionSuffix:String) = {
    val lastFile = expectedFilenameList.last
    val witnessFileName: String = CoreTestConstants.TEST_DIR_PREFIX + "witness/rolling/tbr-" + testId
    println(lastFile+"  "+witnessFileName)
    assertTrue(Compare.compare(lastFile, witnessFileName));

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
    defaultTest("test1", "", FILE_OPTION_BLANK, NO_RESTART)
  }

  @Test
  def withCompression_FileBlank_NoRestart_2 = {
    defaultTest("test2", ".gz", FILE_OPTION_BLANK, NO_RESTART);
  }

  @Test
  def noCompression_FileBlank_StopRestart_3 = {
    defaultTest("test3", "", FILE_OPTION_BLANK, WITH_RESTART);
  }

  @Test
  def noCompression_FileSet_StopRestart_4 = {
    defaultTest("test4", "", FILE_OPTION_SET, WITH_RESTART);
  }

  @Test
  def noCompression_FileSet_StopRestart_WithLongWait_4B = {
    defaultTest("test4B", "", FILE_OPTION_SET, WITH_RESTART_AND_LONG_WAIT);
  }

  @Test
  def noCompression_FileSet_NoRestart_5 = {
    defaultTest("test5", "", FILE_OPTION_SET, NO_RESTART);
  }

  @Test
  def withCompression_FileSet_NoRestart_6 = {
    defaultTest("test6", ".gz", FILE_OPTION_SET, NO_RESTART);
  }

  @Test
  def withMissingTargetDir = {
    defaultTest("missingTargetDir", "", FILE_OPTION_SET, NO_RESTART);
  }


  @Test
  def failed_rename: Unit = {

    if(!Env.isWindows) null

    var fos: FileOutputStream = null
    try {
      val fileName = testId2FileName("failed_rename");
      val file= new File(fileName)
      file.getParentFile.mkdirs

      fos = new FileOutputStream(fileName)
      genericTest(zCheck)("failed_rename", "", FILE_OPTION_SET, NO_RESTART)

    } finally {
      StatusPrinter.print(context)
      if(fos != null) fos.close;
    }
  }



}