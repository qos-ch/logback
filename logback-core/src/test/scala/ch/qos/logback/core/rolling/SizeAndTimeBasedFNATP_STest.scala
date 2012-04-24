package ch.qos.logback.core.rolling

/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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

import ch.qos.logback.core.encoder.EchoEncoder
import ch.qos.logback.core.status.InfoStatus
import java.util.Date
import org.junit.{Ignore, Test}
import org.junit.Before
import java.io.File
import ch.qos.logback.core.util.StatusPrinter

/**
 * @author Ceki G&uuml;c&uuml;
 */
class SizeAndTimeBasedFNATP_STest extends RollingScaffolding {
  private var sizeAndTimeBasedFNATP: SizeAndTimeBasedFNATP[AnyRef] = null
  private val rfa1: RollingFileAppender[AnyRef] = new RollingFileAppender[AnyRef]
  private val tbrp1: TimeBasedRollingPolicy[AnyRef] = new TimeBasedRollingPolicy[AnyRef]
  private val rfa2: RollingFileAppender[AnyRef] = new RollingFileAppender[AnyRef]
  private val tbrp2: TimeBasedRollingPolicy[AnyRef] = new TimeBasedRollingPolicy[AnyRef]

  private val encoder: EchoEncoder[AnyRef] = new EchoEncoder[AnyRef]
  var fileSize = 0
  var fileIndexCounter = 0
  var sizeThreshold = 0

  @Before
  def setUp() {
    setUpScaffolding
  }

  private def initRollingFileAppender(rfa: RollingFileAppender[AnyRef], filename: String): Unit = {
    rfa.setContext(context)
    rfa.setEncoder(encoder)
    if (filename != null) {
      rfa.setFile(filename)
    }
  }

  private def initPolicies(rfa: RollingFileAppender[AnyRef], tbrp: TimeBasedRollingPolicy[AnyRef], filenamePattern: String, sizeThreshold: Int, givenTime: Long, lastCheck: Long): Unit = {
    sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP[AnyRef]
    tbrp.setContext(context)
    sizeAndTimeBasedFNATP.setMaxFileSize("" + sizeThreshold)
    tbrp.setTimeBasedFileNamingAndTriggeringPolicy(sizeAndTimeBasedFNATP)
    tbrp.setFileNamePattern(filenamePattern)
    tbrp.setParent(rfa)
    tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(givenTime)
    rfa.setRollingPolicy(tbrp)
    tbrp.start
    rfa.start
  }

  private def addExpectedFileNamedIfItsTime(randomOutputDir: String, testId: String, msg: String, compressionSuffix: String) {
    fileSize = fileSize + msg.getBytes.length
    if (passThresholdTime(nextRolloverThreshold)) {
      fileIndexCounter = 0
      fileSize = 0
      addExpectedFileName_ByFileIndexCounter(randomOutputDir, testId, getMillisOfCurrentPeriodsStart, fileIndexCounter, compressionSuffix)
      recomputeRolloverThreshold(currentTime)
      return
    }

    // windows can delay file size changes, so we only allow for
    // fileIndexCounter 0
    if ((fileIndexCounter < 1) && fileSize > sizeThreshold) {
      addExpectedFileName_ByFileIndexCounter(randomOutputDir, testId, getMillisOfCurrentPeriodsStart, fileIndexCounter, compressionSuffix)
      fileIndexCounter = fileIndexCounter + 1 
      fileSize = 0
    }
  }

  def generic(testId: String, stem: String, withSecondPhase: Boolean, compressionSuffix: String = "") {
    var file = if (stem != null) randomOutputDir + stem else null
    initRollingFileAppender(rfa1, file)
    sizeThreshold = 300
    initPolicies(rfa1, tbrp1, randomOutputDir + testId + "-%d{" + DATE_PATTERN_WITH_SECONDS + "}-%i.txt"+compressionSuffix, sizeThreshold, currentTime, 0)
    addExpectedFileName_ByFileIndexCounter(randomOutputDir, testId, getMillisOfCurrentPeriodsStart, fileIndexCounter, compressionSuffix)
    incCurrentTime(100)
    tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime)
    var runLength: Int = 100
    var prefix: String = "Hello -----------------"

    for (i <- 0 until runLength) {
      var msg: String = prefix + i
      rfa1.doAppend(msg)
      addExpectedFileNamedIfItsTime(randomOutputDir, testId, msg, compressionSuffix)
      incCurrentTime(20)
      tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime)
    }


    if(withSecondPhase) {
      secondPhase
      runLength = runLength*2
    }

    if (stem != null)
      massageExpectedFilesToCorresponToCurrentTarget(file, true)

    Thread.sleep(20)
    // wait for compression to finish
    if(tbrp1.future != null) {
      tbrp1.future.get()
    }
    
    StatusPrinter.print(context)
    expectedFilenameList.foreach(println(_))
    existenceCheck(expectedFilenameList)
    sortedContentCheck(randomOutputDir, runLength, prefix)

    def secondPhase() {
      rfa1.stop();
   
      if(stem != null) {
        val f = new File(file);
        f.setLastModified(currentTime);
      }
   
      sm.add(new InfoStatus("Time when rfa1 is stopped: " + new Date(currentTime), this));
      sm.add(new InfoStatus("currentTime%1000=" + (currentTime % 1000), this));

      initRollingFileAppender(rfa2, file);
      initPolicies(rfa2, tbrp2, randomOutputDir + testId + "-%d{"
              + DATE_PATTERN_WITH_SECONDS + "}-%i.txt"+compressionSuffix, sizeThreshold, currentTime, 0);

      for (i <- runLength until runLength * 2) {
        incCurrentTime(100);
        tbrp2.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
        var msg = prefix + i
        rfa2.doAppend(msg);
        addExpectedFileNamedIfItsTime(randomOutputDir, testId, msg, compressionSuffix);
      }         
    }
  }

  val FIRST_PHASE_ONLY = false
  val WITH_SECOND_PHASE = true

  @Test
  def noCompression_FileSet_NoRestart_1 {
    generic("test1", "toto.log", FIRST_PHASE_ONLY)
  }

  @Test
  def noCompression_FileBlank_NoRestart_2 {
    generic("test2", null, FIRST_PHASE_ONLY)
  }

  @Test
  def noCompression_FileBlank_WithStopStart_3 {
    generic("test3", null, WITH_SECOND_PHASE)
  }
  
  @Test
  def noCompression_FileSet_WithStopStart_4 {
    generic("test4", "test4.log", WITH_SECOND_PHASE)
  }

  @Test
  def withGZCompression_FileSet_NoRestart_5 {
    generic("test5", "toto.log", FIRST_PHASE_ONLY, ".gz")
  }

  @Test
  def withGZCompression_FileBlank_NoRestart_6 {
    generic("test6", null, FIRST_PHASE_ONLY, ".gz")
  }

  @Test
  def withZipCompression_FileSet_NoRestart_7 {
    generic("test7", "toto.log", FIRST_PHASE_ONLY, ".zip")
    checkZipEntryMatchesZipFilename(expectedFilenameList.filter(_.endsWith(".zip")), "test7-20\\d{2}-\\d{2}-\\d{2}_\\d{2}_\\d{2}_\\d{2}-\\d")

  }
}
