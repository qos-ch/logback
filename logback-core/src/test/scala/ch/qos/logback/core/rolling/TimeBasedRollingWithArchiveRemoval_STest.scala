package ch.qos.logback.core.rolling

import org.junit.{Before, Test}
import ch.qos.logback.core.{Context, ContextBase}
import ch.qos.logback.core.testUtil.RandomUtil
import ch.qos.logback.core.util.CoreTestConstants
import org.junit.Assert._
import ch.qos.logback.core.encoder.EchoEncoder
import java.util.concurrent.TimeUnit
import java.io.{FileFilter, File}
import collection.mutable.ListBuffer

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

  def addTime(currentTime: Long, timeToWait: Long): Long = {
    return currentTime + timeToWait
  }

  def waitForCompression(tbrp: TimeBasedRollingPolicy[AnyRef]): Unit = {
    if (tbrp.future != null && !tbrp.future.isDone) {
      tbrp.future.get(800, TimeUnit.MILLISECONDS)
    }
  }

  def doRollover(fileNamePattern: String, periodDurationInMillis: Long, maxHistory: Int, simulatedNumberOfPeriods: Int): Unit = {
    var currentTime: Long = System.currentTimeMillis
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
  }

  def expectedCountWithoutFolders(maxHistory: Int): Int = {
    return maxHistory + 1
  }

  def findFoldersInFolderRecursively(dir: File, fileList: ListBuffer[File]) {
    if (dir.isDirectory) {
      var `match` : Array[File] = dir.listFiles(new FileFilter {
        def accept(f: File): Boolean = {
          return f.isDirectory
        }
      })
      for (f <- `match`) {
        fileList += f
        findFoldersInFolderRecursively(f, fileList)
      }
    }
  }

  def findAllInFolderRecursivelyByStringContains(dir: File, fileList: ListBuffer[File], pattern: String): Unit = {
    if (dir.isDirectory) {
      var `match` : Array[File] = dir.listFiles(new FileFilter {
        def accept(f: File): Boolean = {
          return (f.isDirectory || f.getName.contains(pattern))
        }
      })
      for (f <- `match`) {
        fileList += f
        if (f.isDirectory) {
          findAllInFolderRecursivelyByStringContains(f, fileList, pattern)
        }
      }
    }
  }

  def check(expectedCount: Int): Unit = {
    var dir: File = new File(randomOutputDir)
    val fileList = new ListBuffer[File]
    findAllInFolderRecursivelyByStringContains(dir, fileList, "clean")
    assertEquals(expectedCount, fileList.size)
  }
}