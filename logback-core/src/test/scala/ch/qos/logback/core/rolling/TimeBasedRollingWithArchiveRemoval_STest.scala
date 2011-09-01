package ch.qos.logback.core.rolling

import ch.qos.logback.core.{Context, ContextBase}
import ch.qos.logback.core.testUtil.RandomUtil
import helper.RollingCalendar
import org.junit.Assert._
import ch.qos.logback.core.encoder.EchoEncoder
import java.util.concurrent.TimeUnit
import java.io.{FileFilter, File}
import collection.mutable.ListBuffer
import java.util.Calendar
import ch.qos.logback.core.util.{StatusPrinter, CoreTestConstants}
import ch.qos.logback.core.CoreConstants._
import java.util.regex.{Matcher, Pattern}
import scala.collection.mutable.{Set, HashSet}
import org.junit.{Ignore, Before, Test}

/**
 * Created by IntelliJ IDEA.
 * User: ceki
 * Date: 29.08.11
 * Time: 18:08
 * To change this template use File | Settings | File Templates.
 */

class TimeBasedRollingWithArchiveRemoval_STest {
  var context: Context = new ContextBase
  var encoder: EchoEncoder[AnyRef] = new EchoEncoder[AnyRef]

  val MONTHLY_DATE_PATTERN: String = "yyyy-MM"
  val MONTHLY_CROLOLOG_DATE_PATTERN: String = "yyyy/MM"
  final val DAILY_CROLOLOG_DATE_PATTERN: String = "yyyy/MM/dd"

  val MILLIS_IN_MINUTE: Long = 60 * 1000
  val MILLIS_IN_HOUR: Long = 60 * MILLIS_IN_MINUTE
  val MILLIS_IN_DAY: Long = 24 * MILLIS_IN_HOUR
  val MILLIS_IN_MONTH: Long = ((365.0 / 12) * MILLIS_IN_DAY).asInstanceOf[Long]

  var diff: Int = RandomUtil.getPositiveInt
  var randomOutputDir: String = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/"
  var slashCount: Int = 0

  // by default tbfnatp is an instance of DefaultTimeBasedFileNamingAndTriggeringPolicy
  var tbfnatp: TimeBasedFileNamingAndTriggeringPolicy[AnyRef] = new DefaultTimeBasedFileNamingAndTriggeringPolicy[AnyRef]

  @Before def setUp {
    context.setName("test")
  }

  def computeSlashCount(datePattern: String): Int = {
    if (datePattern == null) 0
    else datePattern.foldLeft(0)((count, c) => if (c == '/') count + 1 else count)
  }

  @Test
  def montlyRollover {
    slashCount = computeSlashCount(MONTHLY_DATE_PATTERN)
    doRollover(randomOutputDir + "clean-%d{" + MONTHLY_DATE_PATTERN + "}.txt", MILLIS_IN_MONTH, 20, 20 * 3)
    check(expectedCountWithoutFolders(20))
  }

  @Test def monthlyRolloverOverManyPeriods {
    System.out.println("randomOutputDir=" + randomOutputDir)
    slashCount = computeSlashCount(MONTHLY_CROLOLOG_DATE_PATTERN)
    var numPeriods: Int = 40
    var maxHistory: Int = 2

    val (startTime, endTime) = doRollover(randomOutputDir + "/%d{" + MONTHLY_CROLOLOG_DATE_PATTERN + "}/clean.txt.zip", MILLIS_IN_MONTH, maxHistory, numPeriods)
    val differenceInMonths = RollingCalendar.diffInMonths(startTime, endTime)
    var indexOfStartPeriod: Int = Calendar.getInstance.get(Calendar.MONTH)
    val withExtraFolder = extraFolder(differenceInMonths, 12, indexOfStartPeriod, maxHistory)
    StatusPrinter.print(context)
    check(expectedCountWithFolders(2, withExtraFolder))
  }

  @Test def dailyRollover {
    slashCount = computeSlashCount(DAILY_DATE_PATTERN)
    doRollover(randomOutputDir + "clean-%d{" + DAILY_DATE_PATTERN + "}.txt.zip", MILLIS_IN_DAY, 5, 5 * 3)
    check(expectedCountWithoutFolders(5))
  }

  @Test def dailyCronologRollover {
    slashCount = computeSlashCount(DAILY_CROLOLOG_DATE_PATTERN)
    doRollover(randomOutputDir + "/%d{" + DAILY_CROLOLOG_DATE_PATTERN + "}/clean.txt.zip", MILLIS_IN_DAY, 8, 8 * 3)
    var expectedDirMin: Int = 9 + slashCount
    var expectDirMax: Int = expectedDirMin + 1 + 1
    expectedFileAndDirCount(9, expectedDirMin, expectDirMax)
  }

  @Test def dailySizeBasedRollover {
    var sizeAndTimeBasedFNATP: SizeAndTimeBasedFNATP[AnyRef] = new SizeAndTimeBasedFNATP[AnyRef]
    sizeAndTimeBasedFNATP.setMaxFileSize("10000")
    tbfnatp = sizeAndTimeBasedFNATP
    slashCount = computeSlashCount(DAILY_DATE_PATTERN)
    doRollover(randomOutputDir + "/%d{" + DAILY_DATE_PATTERN + "}-clean.%i.zip", MILLIS_IN_DAY, 5, 5 * 4)
    checkPatternCompliance(5 + 1 + slashCount, "\\d{4}-\\d{2}-\\d{2}-clean(\\.\\d)(.zip)?")
  }

  @Test def dailyChronologSizeBasedRollover {
    var sizeAndTimeBasedFNATP: SizeAndTimeBasedFNATP[AnyRef] = new SizeAndTimeBasedFNATP[AnyRef]
    sizeAndTimeBasedFNATP.setMaxFileSize("10000")
    tbfnatp = sizeAndTimeBasedFNATP
    slashCount = 1
    doRollover(randomOutputDir + "/%d{" + DAILY_DATE_PATTERN + "}/clean.%i.zip", MILLIS_IN_DAY, 5, 5 * 4)
    checkDirPatternCompliance(6)
  }


  // this test requires changing the current working directory which is impossible in Java
  @Ignore
  @Test def dailyChronologSizeBasedRolloverWhenLogFilenameDoesNotContainDirectory: Unit = {
    var sizeAndTimeBasedFNATP: SizeAndTimeBasedFNATP[AnyRef] = new SizeAndTimeBasedFNATP[AnyRef]
    sizeAndTimeBasedFNATP.setMaxFileSize("10000")
    tbfnatp = sizeAndTimeBasedFNATP
    slashCount = 1
    doRollover("clean.%d{" + DAILY_DATE_PATTERN + "}.%i.zip", MILLIS_IN_DAY, 5, 5 * 4)
    checkDirPatternCompliance(6)
  }

  def extraFolder(numPeriods: Int, periodsPerEra: Int, beginPeriod: Int, maxHistory: Int): Boolean = {
    var valueOfLastMonth: Int = ((beginPeriod) + numPeriods) % periodsPerEra
    return (valueOfLastMonth < maxHistory)
  }

  def expectedCountWithFolders(maxHistory: Int, extraFolder: Boolean): Int = {
    val numLogFiles = (maxHistory + 1)
    val numLogFilesAndFolders = numLogFiles*2
    var result: Int = numLogFilesAndFolders + slashCount
    if (extraFolder) result += 1
    result
  }

  def addTime(currentTime: Long, timeToWait: Long): Long = {
    return currentTime + timeToWait
  }

  def waitForCompression(tbrp: TimeBasedRollingPolicy[AnyRef]): Unit = {
    if (tbrp.future != null && !tbrp.future.isDone) {
      tbrp.future.get(800, TimeUnit.MILLISECONDS)
    }
  }

  def doRollover(fileNamePattern: String, periodDurationInMillis: Long, maxHistory: Int, simulatedNumberOfPeriods: Int): (Long, Long) = {
    var currentTime: Long = System.currentTimeMillis
    val startTime = currentTime
    var rfa: RollingFileAppender[AnyRef] = new RollingFileAppender[AnyRef]
    rfa.setContext(context)
    rfa.setEncoder(encoder)
    var tbrp: TimeBasedRollingPolicy[AnyRef] = new TimeBasedRollingPolicy[AnyRef]
    tbrp.setContext(context)
    tbrp.setFileNamePattern(fileNamePattern)
    tbrp.setMaxHistory(maxHistory)
    tbrp.setParent(rfa)
    tbrp.timeBasedFileNamingAndTriggeringPolicy = tbfnatp
    tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime)
    tbrp.start
    rfa.setRollingPolicy(tbrp)
    rfa.start
    var ticksPerPeriod: Int = 512
    var runLength = simulatedNumberOfPeriods * ticksPerPeriod

    for (i <- 0 to runLength) {
      rfa.doAppend("Hello ----------------------------------------------------------" + i)
      tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(addTime(tbrp.timeBasedFileNamingAndTriggeringPolicy.getCurrentTime, periodDurationInMillis / ticksPerPeriod))
      if (i % (ticksPerPeriod / 2) == 0) {
        waitForCompression(tbrp)
      }

    }
    waitForCompression(tbrp)
    rfa.stop
    (startTime, tbrp.timeBasedFileNamingAndTriggeringPolicy.getCurrentTime)
  }

  def expectedCountWithoutFolders(maxHistory: Int): Int = {
    return maxHistory + 1
  }


  def genericFindMatching(matchFunc: (File, String) => Boolean, dir: File, fileList: ListBuffer[File], pattern: String = null, includeDirs: Boolean = false) {
    if (dir.isDirectory) {
      var `match` : Array[File] = dir.listFiles(new FileFilter {
        def accept(f: File): Boolean = {
          return f.isDirectory() || matchFunc(f, pattern)
        }
      })
      for (f <- `match`) {
        if (f.isDirectory) {
          if (includeDirs) fileList += f
          genericFindMatching(matchFunc, f, fileList, pattern, includeDirs)
        } else
          fileList += f
      }
    }
  }

  def findAllFoldersInFolderRecursively(dir: File, fileList: ListBuffer[File]) {
    genericFindMatching((f, p) => false, dir, fileList, null, true);
  }

  def findAllDirsOrStringContainsFilesRecursively(dir: File, fileList: ListBuffer[File], pattern: String): Unit = {
    genericFindMatching((f, pattern) => (f.getName.contains(pattern)), dir, fileList, pattern, true)
  }

  def findFilesInFolderRecursivelyByPatterMatch(dir: File, fileList: ListBuffer[File], pattern: String): Unit = {
    genericFindMatching((f, p) => f.getName.matches(pattern), dir, fileList, pattern);
  }


  def expectedFileAndDirCount(expectedFileAndDirCount: Int, expectedDirCountMin: Int, expectedDirCountMax: Int): Unit = {
    var dir: File = new File(randomOutputDir)
    var fileList = new ListBuffer[File]
    findFilesInFolderRecursivelyByPatterMatch(dir, fileList, "clean")
    var dirList = new ListBuffer[File]
    findAllFoldersInFolderRecursively(dir, dirList)
    assertTrue("expectedDirCountMin=" + expectedDirCountMin + ", expectedDirCountMax=" + expectedDirCountMax + " actual value=" + dirList.size, expectedDirCountMin <= dirList.size && dirList.size <= expectedDirCountMax)
  }

  def check(expectedCount: Int): Unit = {
    var dir: File = new File(randomOutputDir)
    val fileList = new ListBuffer[File]
    findAllDirsOrStringContainsFilesRecursively(dir, fileList, "clean")
    assertEquals(expectedCount, fileList.size)
  }

  def groupByClass(fileList: ListBuffer[File], regex: String): Set[String] = {
    var p: Pattern = Pattern.compile(regex)
    val set = new HashSet[String]
    for (f <- fileList) {
      var n: String = f.getName
      var m: Matcher = p.matcher(n)
      m.matches
      var begin: Int = m.start(1)
      var reduced: String = n.substring(0, begin)
      set.add(reduced)
    }
    return set
  }

  def checkPatternCompliance(expectedClassCount: Int, regex: String) {
    var dir: File = new File(randomOutputDir)
    var fileList = new ListBuffer[File]
    findFilesInFolderRecursivelyByPatterMatch(dir, fileList, regex)
    var set: Set[String] = groupByClass(fileList, regex)
    assertEquals(expectedClassCount, set.size)
  }

  def checkDirPatternCompliance(expectedClassCount: Int): Unit = {
    var dir: File = new File(randomOutputDir)
    var fileList = new ListBuffer[File]
    findAllFoldersInFolderRecursively(dir, fileList)
    for (f <- fileList) {
      assertTrue(f.list.length >= 1)
    }
    assertEquals(expectedClassCount, fileList.size)
  }
}