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

  private def addExpectedFileName(randomOutputDir: String, testId: String, millis: Long, fileIndexCounter: Int, gzExtension: Boolean): Unit = {
    var fn: String = randomOutputDir + testId + "-" + SDF.format(millis) + "-" + fileIndexCounter + ".txt"
    if (gzExtension) {
      fn += ".gz"
    }
     expectedFilenameList = expectedFilenameList ::: List(fn)
  }

  private def addExpectedFileNamedIfItsTime(randomOutputDir: String, testId: String, msg: String, gzExtension: Boolean): Unit = {
      fileSize = fileSize + msg.getBytes.length
      if (passThresholdTime(nextRolloverThreshold)) {
        fileIndexCounter = 0
        fileSize = 0
        addExpectedFileName(randomOutputDir, testId, getMillisOfCurrentPeriodsStart, fileIndexCounter, gzExtension)
        recomputeRolloverThreshold(currentTime)
        return
      }

    // windows can delay file size changes, so we only allow for
    // fileIndexCounter 0 and 1
      if ((fileIndexCounter < 1) && fileSize > sizeThreshold) {
        addExpectedFileName(randomOutputDir, testId, getMillisOfCurrentPeriodsStart, ({
          fileIndexCounter += 1; fileIndexCounter - 1
        }), gzExtension)
        fileSize = 0
        return
      }
    }

  @Test
  def noCompression_FileSet_NoRestart_1 {
    var testId: String = "test1"
    var file: String = randomOutputDir + "toto.log"
    initRollingFileAppender(rfa1, file)
    sizeThreshold = 300
    initPolicies(rfa1, tbrp1, randomOutputDir + testId + "-%d{" + DATE_PATTERN_WITH_SECONDS + "}-%i.txt", sizeThreshold, currentTime, 0)
    addExpectedFileName(randomOutputDir, testId, getMillisOfCurrentPeriodsStart, fileIndexCounter, false)
    incCurrentTime(100)
    tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime)
    var runLength: Int = 100
    var prefix: String = "Hello -----------------"

    for (i <- 0 until runLength) {
      var msg: String = prefix + i
      rfa1.doAppend(msg)
      addExpectedFileNamedIfItsTime(randomOutputDir, testId, msg, false)
      incCurrentTime(20)
      tbrp1.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime)
    }
    massageExpectedFilesToCorresponToCurrentTarget(file)
    expectedFilenameList.foreach(println(_))
 
    existenceCheck(expectedFilenameList)
    sortedContentCheck(randomOutputDir, runLength, prefix)
  }
}
