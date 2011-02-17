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
import org.junit.Assert.assertEquals

import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.io.File
import java.util.{ArrayList, Date, Calendar}

import ch.qos.logback.core.{ContextBase, Context}
import ch.qos.logback.core.util.CoreTestConstants
import ch.qos.logback.core.testUtil.{FileToBufferUtil, RandomUtil}
import helper.FileFilterUtil

trait RollingScaffolding {
  final val DATE_PATTERN_WITH_SECONDS = "yyyy-MM-dd_HH_mm_ss"
  final val SDF = new SimpleDateFormat(DATE_PATTERN_WITH_SECONDS)
  private[rolling] var context: Context = new ContextBase
  private[rolling] var diff = RandomUtil.getPositiveInt
  protected var currentTime = 0L
  protected var randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/"
  private[rolling] var cal = Calendar.getInstance
  protected var nextRolloverThreshold = 0L;
  protected var expectedFilenameList: List[String] = Nil

  val FILE_OPTION_SET = true
  val FILE_OPTION_BLANK = false


  def setUpScaffolding {
    context.setName("test")
    cal.set(Calendar.MILLISECOND, 333)
    currentTime = cal.getTimeInMillis
    recomputeRolloverThreshold()
  }

  protected def incCurrentTime(increment: Long) {
    currentTime += increment
  }

  protected def getDateOfCurrentPeriodsStart = {
    val delta: Long = currentTime % 1000
    new Date(currentTime - delta)
  }

  protected def addExpectedFileName_ByDate(outputDir: String, testId: String, date: Date, gzExtension: Boolean) {
    var fn: String = outputDir + testId + "-" + SDF.format(date)
    if (gzExtension) {
      fn += ".gz"
    }
    expectedFilenameList += fn
  }

  protected def addExpectedFileNamedIfItsTime_ByDate(outputDir: String, testId: String, gzExtension: Boolean) {
    if (passThresholdTime(nextRolloverThreshold)) {
      addExpectedFileName_ByDate(outputDir, testId, getDateOfCurrentPeriodsStart, gzExtension)
      recomputeRolloverThreshold()
    }
  }

  protected def passThresholdTime(nextRolloverThreshold: Long) = currentTime >= nextRolloverThreshold

  protected def recomputeRolloverThreshold() {
    val delta = currentTime % 1000
    nextRolloverThreshold = (currentTime - delta) + 1000
  }

  private[rolling] def addGZIfNotLast(i: Int, suff: String) = {
    val lastIndex = expectedFilenameList.size - 1
    if (i != lastIndex) suff else ""
  }

  private[rolling] def waitForCompression(tbrp: TimeBasedRollingPolicy[AnyRef]) {
    if (tbrp.future != null && !tbrp.future.isDone) {
      tbrp.future.get(200, TimeUnit.MILLISECONDS)
    }
  }

  private[rolling] def testId2FileName(testId: String) = randomOutputDir + testId + ".log"

  // =========================================================================
  // utility methods
  // =========================================================================
  private[rolling] def massageExpectedFilesToCorresponToCurrentTarget(file: String) {
    expectedFilenameList = expectedFilenameList.dropRight(1)
    expectedFilenameList += (randomOutputDir + file)
  }

  def getFilesInDirectory(outputDirStr: String) = new File(outputDirStr).listFiles

  def fileContentCheck(fileArray: Array[File], runLength: Int, prefix: String) {
    val stringList = new ArrayList[String]
    for (file <- fileArray) {
      FileToBufferUtil.readIntoList(file, stringList)
    }
    val witnessList = new ArrayList[String]

    for (i <- 0 to runLength - 1) {
      witnessList.add(prefix + i)
    }
    assertEquals(witnessList, stringList)
  }

  def reverseSortedContentCheck(outputDirStr: String, runLength: Int, prefix: String): Unit = {
    val fileArray = getFilesInDirectory(outputDirStr)
    FileFilterUtil.reverseSortFileArrayByName(fileArray)
    fileContentCheck(fileArray, runLength, prefix)
  }

  def existenceCheck(filenameList: List[String]) {
    for (filename <- filenameList) {
      assertTrue("File " + filename + " does not exist", new File(filename).exists)
    }
  }
}