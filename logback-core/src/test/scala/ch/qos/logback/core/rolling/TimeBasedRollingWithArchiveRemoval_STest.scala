package ch.qos.logback.core.rolling

import ch.qos.logback.core.{Context, ContextBase}
import ch.qos.logback.core.testUtil.RandomUtil
import helper.RollingCalendar
import org.junit.Assert._
import ch.qos.logback.core.encoder.EchoEncoder
import java.util.concurrent.TimeUnit
import java.io.{FileFilter, File}
import collection.mutable.ListBuffer
import ch.qos.logback.core.util.{StatusPrinter, CoreTestConstants}
import ch.qos.logback.core.CoreConstants._
import java.util.regex.{Matcher, Pattern}
import scala.collection.mutable.{Set, HashSet}
import org.junit.{Ignore, Before, Test}
import java.util.{Date, Calendar}

class TimeBasedRollingWithArchiveRemoval_STest {
  var context: Context = new ContextBase
  var encoder: EchoEncoder[AnyRef] = new EchoEncoder[AnyRef]

  val MONTHLY_DATE_PATTERN: String = "yyyy-MM"

  val MONTHLY_CRONOLOG_DATE_PATTERN: String = "yyyy/MM"
  final val DAILY_CRONOLOG_DATE_PATTERN: String = "yyyy/MM/dd"

  val MILLIS_IN_MINUTE: Long = 60 * 1000
  val MILLIS_IN_HOUR: Long = 60 * MILLIS_IN_MINUTE
  val MILLIS_IN_DAY: Long = 24 * MILLIS_IN_HOUR
  val MILLIS_IN_MONTH: Long = ((365.242199 / 12) * MILLIS_IN_DAY).asInstanceOf[Long]
  val MONTHS_IN_YEAR = 12

  var diff: Int = _
  var randomOutputDir: String = _
  var slashCount: Int = 0

  // by default tbfnatp is an instance of DefaultTimeBasedFileNamingAndTriggeringPolicy
  var tbfnatp: TimeBasedFileNamingAndTriggeringPolicy[AnyRef] = new DefaultTimeBasedFileNamingAndTriggeringPolicy[AnyRef]

  val now = System.currentTimeMillis


  @Before def setUp {
    context.setName("test")
    diff = RandomUtil.getPositiveInt
    randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/"
  }

  def computeSlashCount(datePattern: String): Int = {
    if (datePattern == null) 0
    else datePattern.foldLeft(0)((count, c) => if (c == '/') count + 1 else count)
  }


  // test that the number of files at the end of the test is same as the expected number taking into account end dates
  // near the beginning of a new year. This test has been run in a loop with start date varying over a two years
  // with success.
  @Test def monthlyRolloverOverManyPeriods() {

    slashCount = computeSlashCount(MONTHLY_CRONOLOG_DATE_PATTERN)
    val numPeriods: Int = 40
    val maxHistory: Int = 2
    val fileNamePattern = randomOutputDir + "/%d{" + MONTHLY_CRONOLOG_DATE_PATTERN + "}/clean.txt.zip"
    val (startTime, endTime) = logOverMultiplePeriods(now, fileNamePattern, MILLIS_IN_MONTH, maxHistory, numPeriods)
    val differenceInMonths = RollingCalendar.diffInMonths(startTime, endTime)
    val startTimeAsCalendar = Calendar.getInstance()
    startTimeAsCalendar.setTimeInMillis(startTime)
    val indexOfStartPeriod: Int = startTimeAsCalendar.get(Calendar.MONTH)
    val withExtraFolder = extraFolder(differenceInMonths, MONTHS_IN_YEAR, indexOfStartPeriod, maxHistory)

    def expectedCountWithFolders(): Int = {
      val numLogFiles = (maxHistory + 1)
      val numLogFilesAndFolders = numLogFiles * 2
      var result: Int = numLogFilesAndFolders + slashCount
      if (withExtraFolder) result += 1
      result
    }
    //StatusPrinter.print(context)
    check(expectedCountWithFolders())
  }

  def generateDailyRollover(now: Long, maxHistory: Int, simulatedNumberOfPeriods: Int, startInactivity: Int, numInactivityPeriods: Int) {
    slashCount = computeSlashCount(DAILY_DATE_PATTERN)
    logOverMultiplePeriods(now, randomOutputDir + "clean-%d{" + DAILY_DATE_PATTERN + "}.txt", MILLIS_IN_DAY, maxHistory, simulatedNumberOfPeriods, startInactivity, numInactivityPeriods)
    //StatusPrinter.print(context)
    check(expectedCountWithoutFoldersWithInactivity(maxHistory, simulatedNumberOfPeriods, startInactivity + numInactivityPeriods))
  }

  @Test
  def basicDailyRollover {
    generateDailyRollover(now, maxHistory = 20, simulatedNumberOfPeriods = 20 * 3, startInactivity = 0, numInactivityPeriods = 0)
  }

  // Since the duration of a month (in seconds) varies from month to month, tests with inactivity period must
  // be conducted with daily rollover  not monthly
  @Test def dailyRollover15 {
    generateDailyRollover(now, maxHistory = 5, simulatedNumberOfPeriods = 15, startInactivity = 6, numInactivityPeriods = 3)
  }

  @Test def dailyRolloverWithInactivity70 {
    generateDailyRollover(now, maxHistory = 6, simulatedNumberOfPeriods = 70, startInactivity = 30, numInactivityPeriods = 1)
  }

  @Test def dailyRolloverWithInactivity10 {
    generateDailyRollover(now, maxHistory = 6, simulatedNumberOfPeriods = 10, startInactivity = 3, numInactivityPeriods = 4)
  }


  @Test def dailyRolloverWithSecondPhase {
    slashCount = computeSlashCount(DAILY_DATE_PATTERN)
    val maxHistory = 5
    val simulatedNumberOfPeriods = maxHistory * 2
    val (startTime, endTime) = logOverMultiplePeriods(now, randomOutputDir + "clean-%d{" + DAILY_DATE_PATTERN + "}.txt", MILLIS_IN_DAY, maxHistory, maxHistory * 2)
    logOverMultiplePeriods(endTime + MILLIS_IN_DAY * 10, randomOutputDir + "clean-%d{" + DAILY_DATE_PATTERN + "}.txt", MILLIS_IN_DAY, maxHistory, maxHistory)
    check(expectedCountWithoutFolders(maxHistory))
  }


  @Test def dailyCronologRollover {
    slashCount = computeSlashCount(DAILY_CRONOLOG_DATE_PATTERN)
    logOverMultiplePeriods(now, randomOutputDir + "/%d{" + DAILY_CRONOLOG_DATE_PATTERN + "}/clean.txt.zip", MILLIS_IN_DAY, 8, 8 * 3)
    var expectedDirMin: Int = 9 + slashCount
    var expectDirMax: Int = expectedDirMin + 1 + 1
    expectedFileAndDirCount(9, expectedDirMin, expectDirMax)
  }

  @Test def dailySizeBasedRollover {
    var sizeAndTimeBasedFNATP: SizeAndTimeBasedFNATP[AnyRef] = new SizeAndTimeBasedFNATP[AnyRef]
    sizeAndTimeBasedFNATP.setMaxFileSize("10000")
    tbfnatp = sizeAndTimeBasedFNATP
    slashCount = computeSlashCount(DAILY_DATE_PATTERN)
    logOverMultiplePeriods(now, randomOutputDir + "/%d{" + DAILY_DATE_PATTERN + "}-clean.%i.zip", MILLIS_IN_DAY, 5, 5 * 4)
    checkPatternCompliance(5 + 1 + slashCount, "\\d{4}-\\d{2}-\\d{2}-clean(\\.\\d)(.zip)?")
  }

  @Test def dailyChronologSizeBasedRollover {
    var sizeAndTimeBasedFNATP: SizeAndTimeBasedFNATP[AnyRef] = new SizeAndTimeBasedFNATP[AnyRef]
    sizeAndTimeBasedFNATP.setMaxFileSize("10000")
    tbfnatp = sizeAndTimeBasedFNATP
    slashCount = 1
    logOverMultiplePeriods(now, randomOutputDir + "/%d{" + DAILY_DATE_PATTERN + "}/clean.%i.zip", MILLIS_IN_DAY, 5, 5 * 4)
    checkDirPatternCompliance(6)
  }

  @Test def dailyChronologSizeBasedRolloverWithSecondPhase {
    var sizeAndTimeBasedFNATP: SizeAndTimeBasedFNATP[AnyRef] = new SizeAndTimeBasedFNATP[AnyRef]
    sizeAndTimeBasedFNATP.setMaxFileSize("10000")
    tbfnatp = sizeAndTimeBasedFNATP
    slashCount = 1
    val maxHistory = 5
    val simulatedNumberOfPeriods = maxHistory * 4
    val (startTime, endTime) = logOverMultiplePeriods(now, randomOutputDir + "/%d{" + DAILY_DATE_PATTERN + "}/clean.%i", MILLIS_IN_DAY, maxHistory, 3)
    logOverMultiplePeriods(endTime + MILLIS_IN_DAY * 7, randomOutputDir + "/%d{" + DAILY_DATE_PATTERN + "}/clean.%i", MILLIS_IN_DAY, maxHistory, simulatedNumberOfPeriods)
    checkDirPatternCompliance(maxHistory + 1)
  }


  def logOncePeriod(currentTime: Long, fileNamePattern: String, maxHistory: Int) = {
    val (rfa, tbrp) = buildRollingFileAppender(currentTime, fileNamePattern, maxHistory, true)
    rfa.doAppend("Hello ----------------------------------------------------------" + new Date(currentTime))
    rfa.stop()
  }

  @Test def cleanHistoryOnStart {
    var now = this.now
    val fileNamePattern = randomOutputDir + "clean-%d{" + DAILY_DATE_PATTERN + "}.txt"
    val maxHistory = 3
    for (i <- 0 to 5) {
      logOncePeriod(now, fileNamePattern, maxHistory)
      now = now + MILLIS_IN_DAY
    }
    StatusPrinter.print(context)
    check(expectedCountWithoutFolders(maxHistory))
  }

  def extraFolder(numPeriods: Int, periodsPerEra: Int, beginPeriod: Int, maxHistory: Int): Boolean = {
    println("numPeriods=" + numPeriods + ", beginPeriod=" + beginPeriod + ", maxHistory=" + maxHistory)
    var valueOfLastMonth: Int = ((beginPeriod) + numPeriods) % periodsPerEra
    return (valueOfLastMonth < maxHistory)
  }


  def addTime(currentTime: Long, timeToWait: Long): Long = {
    return currentTime + timeToWait
  }

  def waitForCompression(tbrp: TimeBasedRollingPolicy[AnyRef]): Unit = {
    if (tbrp.future != null && !tbrp.future.isDone) {
      tbrp.future.get(1000, TimeUnit.MILLISECONDS)
    }
  }

  def buildRollingFileAppender(currentTime: Long, fileNamePattern: String, maxHistory: Int,
                               cleanHistoryOnStart: Boolean = false): (RollingFileAppender[AnyRef], TimeBasedRollingPolicy[AnyRef]) = {
    val rfa: RollingFileAppender[AnyRef] = new RollingFileAppender[AnyRef]
    rfa.setContext(context)
    rfa.setEncoder(encoder)
    val tbrp: TimeBasedRollingPolicy[AnyRef] = new TimeBasedRollingPolicy[AnyRef]
    tbrp.setContext(context)
    tbrp.setFileNamePattern(fileNamePattern)
    tbrp.setMaxHistory(maxHistory)
    tbrp.setParent(rfa)
    tbrp.setCleanHistoryOnStart(cleanHistoryOnStart)
    tbrp.timeBasedFileNamingAndTriggeringPolicy = tbfnatp
    tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime)
    tbrp.start
    rfa.setRollingPolicy(tbrp)
    rfa.start
    (rfa, tbrp)
  }

  def logOverMultiplePeriods(currentTime: Long, fileNamePattern: String, periodDurationInMillis: Long, maxHistory: Int,
                 simulatedNumberOfPeriods: Int, startInactivity: Int = 0,
                 numInactivityPeriods: Int = 0): (Long, Long) = {
    val startTime = currentTime
    val (rfa, tbrp) = buildRollingFileAppender(currentTime, fileNamePattern, maxHistory)
    val ticksPerPeriod: Int = 512
    val runLength = simulatedNumberOfPeriods * ticksPerPeriod
    val startInactivityIndex: Int = 1 + startInactivity * ticksPerPeriod
    val endInactivityIndex = startInactivityIndex + numInactivityPeriods * ticksPerPeriod
    val tickDuration = periodDurationInMillis / ticksPerPeriod

    for (i <- 0 to runLength) {
      if (i < startInactivityIndex || i > endInactivityIndex) {
        rfa.doAppend("Hello ----------------------------------------------------------" + i)
      }
      tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(addTime(tbrp.timeBasedFileNamingAndTriggeringPolicy.getCurrentTime, tickDuration))
      if (i % (ticksPerPeriod / 2) == 0) {
        waitForCompression(tbrp)
      }
    }

    println("Last date" + new Date(tbrp.timeBasedFileNamingAndTriggeringPolicy.getCurrentTime()));
    waitForCompression(tbrp)
    rfa.stop
    (startTime, tbrp.timeBasedFileNamingAndTriggeringPolicy.getCurrentTime)
  }

  def expectedCountWithoutFolders(maxHistory: Int): Int = {
    return maxHistory + 1
  }

  def expectedCountWithoutFoldersWithInactivity(maxHistory: Int, totalPeriods: Int, endOfInactivity: Int): Int = {
    val availableHistory = (totalPeriods + 1) - endOfInactivity;
    val actualHistory = scala.math.min(availableHistory, maxHistory + 1)
    return actualHistory
  }


  def genericFindMatching(matchFunc: (File, String) => Boolean, dir: File, fileList: ListBuffer[File], pattern: String = null, includeDirs: Boolean = false) {
    if (dir.isDirectory) {
      val `match` : Array[File] = dir.listFiles(new FileFilter {
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
    val dir: File = new File(randomOutputDir)
    val fileList = new ListBuffer[File]
    findFilesInFolderRecursivelyByPatterMatch(dir, fileList, "clean")
    val dirList = new ListBuffer[File]
    findAllFoldersInFolderRecursively(dir, dirList)
    val msg = "expectedDirCountMin=" + expectedDirCountMin + ", expectedDirCountMax=" + expectedDirCountMax + " actual value=" + dirList.size
    assertTrue(msg, expectedDirCountMin <= dirList.size && dirList.size <= expectedDirCountMax)
  }

  def check(expectedCount: Int): Unit = {
    val dir: File = new File(randomOutputDir)
    val fileList = new ListBuffer[File]
    findAllDirsOrStringContainsFilesRecursively(dir, fileList, "clean")
    assertEquals(expectedCount, fileList.size)
  }

  def groupByClass(fileList: ListBuffer[File], regex: String): Set[String] = {
    val p: Pattern = Pattern.compile(regex)
    val set = new HashSet[String]
    for (f <- fileList) {
      val n: String = f.getName
      val m: Matcher = p.matcher(n)
      m.matches
      val begin: Int = m.start(1)
      val reduced: String = n.substring(0, begin)
      set.add(reduced)
    }
    return set
  }

  def checkPatternCompliance(expectedClassCount: Int, regex: String) {
    val dir: File = new File(randomOutputDir)
    val fileList = new ListBuffer[File]
    findFilesInFolderRecursivelyByPatterMatch(dir, fileList, regex)
    val set: Set[String] = groupByClass(fileList, regex)
    assertEquals(expectedClassCount, set.size)
  }

  def checkDirPatternCompliance(expectedClassCount: Int): Unit = {
    val dir: File = new File(randomOutputDir)
    val fileList = new ListBuffer[File]
    findAllFoldersInFolderRecursively(dir, fileList)
    for (f <- fileList) {
      assertTrue(f.list.length >= 1)
    }
    assertEquals(expectedClassCount, fileList.size)
  }
}