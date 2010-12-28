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

import ch.qos.logback.core.{ContextBase, Context}
import ch.qos.logback.core.util.CoreTestConstants
import ch.qos.logback.core.testUtil.RandomUtil
import java.util.{Date, Calendar}
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat

trait RollingScaffolding {
  final val DATE_PATTERN_WITH_SECONDS = "yyyy-MM-dd_HH_mm_ss"
  final val SDF = new SimpleDateFormat(DATE_PATTERN_WITH_SECONDS)
  private[rolling] var context: Context = new ContextBase
  private[rolling] var diff: Int = RandomUtil.getPositiveInt
  protected var currentTime: Long = 0L
  protected var randomOutputDir: String = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/"
  private[rolling] var cal: Calendar = Calendar.getInstance
  protected var nextRolloverThreshold: Long = 0;
  protected var expectedFilenameList: List[String] = Nil

  val FILE_OPTION_SET = true
  val FILE_OPTION_BLANK = false


  def setUpScaffolding: Unit = {
    context.setName("test")
    cal.set(Calendar.MILLISECOND, 333)
    currentTime = cal.getTimeInMillis
    nextRolloverThreshold = recomputeRolloverThreshold(currentTime)
  }

  protected def incCurrentTime(increment: Long): Unit = {
    currentTime += increment
  }

  protected def getDateOfCurrentPeriodsStart: Date = {
    var delta: Long = currentTime % 1000
    return new Date(currentTime - delta)
  }

  protected def addExpectedFileName_ByDate(outputDir: String, testId: String, date: Date, gzExtension: Boolean): Unit = {
    var fn: String = outputDir + testId + "-" + SDF.format(date)
    if (gzExtension) {
      fn += ".gz"
    }
    expectedFilenameList += fn

  }

  protected def addExpectedFileNamedIfItsTime_ByDate(outputDir: String, testId: String, gzExtension: Boolean): Unit = {
    if (passThresholdTime(nextRolloverThreshold)) {
      addExpectedFileName_ByDate(outputDir, testId, getDateOfCurrentPeriodsStart, gzExtension)
      nextRolloverThreshold = recomputeRolloverThreshold(currentTime)
    }
  }

  protected def passThresholdTime(nextRolloverThreshold: Long): Boolean = {
    return currentTime >= nextRolloverThreshold
  }

  protected def recomputeRolloverThreshold(ct: Long): Long = {
    var delta: Long = ct % 1000
    (ct - delta) + 1000
  }

  private[rolling] def addGZIfNotLast(i: Int, suff: String): String = {
    var lastIndex: Int = expectedFilenameList.size - 1
    if (i != lastIndex) suff else ""
  }

  private[rolling] def waitForCompression(tbrp: TimeBasedRollingPolicy[AnyRef]): Unit = {
    if (tbrp.future != null && !tbrp.future.isDone) {
      tbrp.future.get(200, TimeUnit.MILLISECONDS)
    }
  }

  private[rolling] def testId2FileName(testId: String): String = {
    return randomOutputDir + testId + ".log"
  }

  // =========================================================================
  // utility methods
  // =========================================================================
  private[rolling] def massageExpectedFilesToCorresponToCurrentTarget(file: String): Unit = {
    expectedFilenameList = expectedFilenameList.dropRight(1)
    expectedFilenameList +=  (randomOutputDir + file)
  }

}