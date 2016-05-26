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

import org.junit.{Before, Test}
import ch.qos.logback.core.encoder.EchoEncoder
import java.util.zip.{ZipEntry, ZipFile}
import org.junit.Assert._
import ch.qos.logback.core.util.{CoreTestConstants, StatusPrinter}

class SizeBasedRolling_STest extends RollingScaffolding {

  
  var rfa: RollingFileAppender[AnyRef] = new RollingFileAppender[AnyRef]
  var fwrp: FixedWindowRollingPolicy = new FixedWindowRollingPolicy
  var sizeBasedTriggeringPolicy = new SizeBasedTriggeringPolicy[AnyRef]
  var encoder: EchoEncoder[AnyRef] = new EchoEncoder[AnyRef]

  @Before
  def setUp: Unit = {
    setUpScaffolding
    fwrp.setContext(context)
    fwrp.setParent(rfa)
    rfa.setContext(context)
    sizeBasedTriggeringPolicy.setContext(context)
  }

  private def initRFA(filename: String): Unit = {
    rfa.setEncoder(encoder)
    if (filename != null) {
      rfa.setFile(filename)
    }
  }


  /**
   * Test whether FixedWindowRollingPolicy throws an exception when the
   * ActiveFileName is not set.
   */
  @Test(expected = classOf[IllegalStateException])
  def activeFileNameNotSet() {
    sizeBasedTriggeringPolicy.setMaxFileSize("100")
    sizeBasedTriggeringPolicy.start

    fwrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "sizeBased-test1.%i")
    fwrp.start
    // The absence of activeFileName option should cause an exception.
  }


  def generic(testName: String, fileName: String, filenamePattern: String, expectedFilenameList: List[String]) {
    rfa.setName("ROLLING")
    initRFA(randomOutputDir + fileName)

    sizeBasedTriggeringPolicy.setMaxFileSize("100");
    fwrp.setMinIndex(0);
    fwrp.setFileNamePattern(randomOutputDir + filenamePattern);

    rfa.triggeringPolicy = sizeBasedTriggeringPolicy
    rfa.rollingPolicy = fwrp

    fwrp.start
    sizeBasedTriggeringPolicy.start
    rfa.start

    val runLength = 40
    val prefix = "hello"
    for (i <- 0 until runLength) {
      Thread.sleep(10)
      rfa.doAppend(prefix + i)
    }
    rfa.stop()

    existenceCheck(expectedFilenameList)
    reverseSortedContentCheck(randomOutputDir, runLength, prefix)

  }

  @Test
  def smoke() {
    expectedFilenameList = expectedFilenameList ::: List(randomOutputDir + "a-sizeBased-smoke.log")
    expectedFilenameList = expectedFilenameList ::: List(randomOutputDir + "sizeBased-smoke.0")
    expectedFilenameList = expectedFilenameList ::: List(randomOutputDir + "sizeBased-smoke.1")
    generic("zipped", "a-sizeBased-smoke.log", "sizeBased-smoke.%i", expectedFilenameList)

  }
  @Test
  def gz() {
    println(randomOutputDir)
    expectedFilenameList = expectedFilenameList ::: List(randomOutputDir + "a-sbr-gzed.log")
    expectedFilenameList = expectedFilenameList ::: List(randomOutputDir + "sbr-gzed.0.gz")
    expectedFilenameList = expectedFilenameList ::: List(randomOutputDir + "sbr-gzed.1.gz")
    generic("gzed", "a-sbr-gzed.log", "sbr-gzed.%i.gz", expectedFilenameList)
  }

  // see also LBCORE-199
  @Test
  def zipped() {
    println(randomOutputDir)
    expectedFilenameList = expectedFilenameList ::: List(randomOutputDir + "a-sbr-zipped.log")
    expectedFilenameList = expectedFilenameList ::: List(randomOutputDir + "sbr-zipped.0.zip")
    expectedFilenameList = expectedFilenameList ::: List(randomOutputDir + "sbr-zipped.1.zip")
    generic("zipped", "a-sbr-zipped.log", "sbr-zipped.%i.zip", expectedFilenameList)
    zipEntryNameCheck(expectedFilenameList.filter(_.endsWith(".zip")), "sbr-zipped.20\\d{2}-\\d{2}-\\d{2}_\\d{4}")
  }
}