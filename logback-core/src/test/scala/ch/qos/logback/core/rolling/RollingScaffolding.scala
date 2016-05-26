/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2010, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 * or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.rolling

import ch.qos.logback.core.{ContextBase, Context}
import helper.{FileNamePattern, FileFilterUtil}
import java.util.concurrent.TimeUnit
import ch.qos.logback.core.util.{CachingDateFormatter, CoreTestConstants}
import org.junit.Assert._
import java.io.File
import ch.qos.logback.core.testUtil.{FileToBufferUtil, RandomUtil}
import java.util.{ArrayList, Date, Calendar}
import java.util.zip.{ZipEntry, ZipFile}

trait RollingScaffolding {
  final val DATE_PATTERN_WITH_SECONDS = "yyyy-MM-dd_HH_mm_ss"
  final val SDF = new CachingDateFormatter(DATE_PATTERN_WITH_SECONDS)

  val context: Context = new ContextBase
  val sm = context.getStatusManager()

  var diff: Int = RandomUtil.getPositiveInt

  protected var currentTime: Long = 0L
  protected var randomOutputDir: String = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/"
  var cal: Calendar = Calendar.getInstance
  protected var nextRolloverThreshold: Long = 0;
  protected var expectedFilenameList: List[String] = Nil

  val FILE_OPTION_SET = true
  val FILE_OPTION_BLANK = false


  def setUpScaffolding: Unit = {
    context.setName("test")
    cal.set(Calendar.MILLISECOND, 333)
    currentTime = cal.getTimeInMillis
    recomputeRolloverThreshold(currentTime)
  }

  protected def incCurrentTime(increment: Long): Unit = {
    currentTime += increment
  }

  protected def getMillisOfCurrentPeriodsStart: Long = {
    var delta: Long = currentTime % 1000
    return (currentTime - delta)
  }

  protected def getDateOfCurrentPeriodsStart: Date = {
    var delta: Long = currentTime % 1000
    return new Date(currentTime - delta)
  }

  protected def addExpectedFileName_ByDate(patternStr: String, millis: Long): Unit = {
    val fileNamePattern = new FileNamePattern(patternStr, context)
    var fn: String = fileNamePattern.convert(new Date(millis))
    println("fn=" + fn)
    expectedFilenameList = expectedFilenameList ::: List(fn)

  }

  protected def addExpectedFileName_ByFileIndexCounter(randomOutputDir: String, testId: String, millis: Long, fileIndexCounter: Int, compressionSuffix: String): Unit = {
    var fn: String = randomOutputDir + testId + "-" + SDF.format(millis) + "-" + fileIndexCounter + ".txt" + compressionSuffix
    expectedFilenameList = expectedFilenameList ::: List(fn)
  }


  protected def addExpectedFileNamedIfItsTime_ByDate(fileNamePatternStr: String): Unit = {
    if (passThresholdTime(nextRolloverThreshold)) {
      addExpectedFileName_ByDate(fileNamePatternStr, getMillisOfCurrentPeriodsStart)
      recomputeRolloverThreshold(currentTime)
    }
  }

  protected def passThresholdTime(nextRolloverThreshold: Long): Boolean = {
    return currentTime >= nextRolloverThreshold
  }

  protected def recomputeRolloverThreshold(ct: Long) {
    var delta: Long = ct % 1000
    nextRolloverThreshold = (ct - delta) + 1000
  }

  private[rolling] def addGZIfNotLast(i: Int, suff: String): String = {
    var lastIndex: Int = expectedFilenameList.size - 1
    if (i != lastIndex) suff else ""
  }

  private[rolling] def waitForCompression(tbrp: TimeBasedRollingPolicy[AnyRef]): Unit = {
    if (tbrp.future != null && !tbrp.future.isDone) {
      tbrp.future.get(400, TimeUnit.MILLISECONDS)
    }
  }

  private[rolling] def testId2FileName(testId: String): String = {
    return randomOutputDir + testId + ".log"
  }

  // =========================================================================
  // utility methods
  // =========================================================================
  private[rolling] def massageExpectedFilesToCorresponToCurrentTarget(file: String, fileOptionIsSet: Boolean): Unit = {
    val last: String = expectedFilenameList.last
    expectedFilenameList = expectedFilenameList.dropRight(1)

    if (fileOptionIsSet) {
      expectedFilenameList = expectedFilenameList ::: List(file)
    } else if (last.endsWith(".gz")) {
      val stem = last.dropRight(3)
      expectedFilenameList = expectedFilenameList ::: List(stem)
    }
  }

  def existenceCheck(filenameList: List[String]): Unit = {
    for (filename <- filenameList) {
      assertTrue("File " + filename + " does not exist", new File(filename).exists)
    }
  }

  def getFilesInDirectory(outputDirStr: String): Array[File] = {
    var outputDir: File = new File(outputDirStr)
    return outputDir.listFiles
  }

  def sortedContentCheck(outputDirStr: String, runLength: Int, prefix: String) {
    var fileArray: Array[File] = getFilesInDirectory(outputDirStr)
    FileFilterUtil.sortFileArrayByName(fileArray)
    fileContentCheck(fileArray, runLength, prefix)
  }

  def reverseSortedContentCheck(outputDirStr: String, runLength: Int, prefix: String) {
    var fileArray: Array[File] = getFilesInDirectory(outputDirStr)
    FileFilterUtil.reverseSortFileArrayByName(fileArray)
    fileContentCheck(fileArray, runLength, prefix)
  }

  def fileContentCheck(fileArray: Array[File], runLength: Int, prefix: String): Unit = {
    var stringList: ArrayList[String] = new ArrayList[String]

    for (file <- fileArray) {
      FileToBufferUtil.readIntoList(file, stringList)
    }
    var witnessList: List[String] = Nil
    for (i <- 0 until runLength) {
      witnessList = (prefix + i) :: witnessList
    }
    witnessList = witnessList.reverse
    import scala.collection.JavaConversions.asScalaBuffer
    assertEquals(witnessList, asScalaBuffer(stringList))
  }

  def zipEntryNameCheck(expectedFilenameList: List[String], pattern: String): Unit = {
    expectedFilenameList.foreach(filepath => checkZipEntryName(filepath, pattern))
  }

  def checkZipEntryMatchesZipFilename(expectedFilenameList: List[String], pattern: String): Unit = {
    expectedFilenameList.foreach(filepath => checkZipEntryName(filepath, stripStemFromZipFilename(filepath)))

    def stripStemFromZipFilename(filepath: String) = {
      val filepathAsFile = new File(filepath)
      val stem = filepathAsFile.getName
      stem.dropRight(".zip".length)
    }
  }

  def checkZipEntryName(filepath: String, pattern: String): Unit = {
    System.out.println("Checking [" + filepath + "]")
    var zf: ZipFile = new ZipFile(filepath)
    var entries: java.util.Enumeration[_ <: ZipEntry] = zf.entries
    assert((entries.hasMoreElements))
    var firstZipEntry: ZipEntry = entries.nextElement
    assert((!entries.hasMoreElements))
    System.out.println("Testing zip entry [" + firstZipEntry.getName + "]")
    assertTrue(firstZipEntry.getName.matches(pattern))
  }

}