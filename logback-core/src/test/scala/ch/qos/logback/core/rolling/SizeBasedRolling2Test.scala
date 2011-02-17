package ch.qos.logback.core.rolling

import org.junit.Assert.fail

import java.io.File

import org.junit.Before
import org.junit.Test

import ch.qos.logback.core.ContextBase
import ch.qos.logback.core.encoder.DummyEncoder
import ch.qos.logback.core.encoder.EchoEncoder
import ch.qos.logback.core.util.CoreTestConstants

/**
 *
 * Do not forget to call start() when configuring programatically.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * @author Craig P. Motlin
 *
 */
class SizeBasedRolling2Test extends RollingScaffolding {

  @Before
  def setUp {
    setUpScaffolding

    {
      val target = new File(CoreTestConstants.OUTPUT_DIR_PREFIX + "sizeBased-smoke.log")
      target.mkdirs
      target.delete
    }

    {
      val target = new File(CoreTestConstants.OUTPUT_DIR_PREFIX + "sbr-test3.log")
      target.mkdirs
      target.delete
    }
  }


  /**
   * Test whether FixedWindowRollingPolicy throws an exception when the
   * ActiveFileName is not set.
   */
  @Test
  def activeFileNameNotSet {
    // We purposefully use the \n as the line separator.
    // This makes the regression test system independent.

    val rfa = new RollingFileAppender[AnyRef]
    rfa.setEncoder(new DummyEncoder[AnyRef])
    rfa.setContext(new ContextBase)

    val context = new ContextBase
    val fwrp = new FixedWindowRollingPolicy
    fwrp.setContext(context)
    fwrp.setParent(rfa)

    val sbtp = new SizeBasedTriggeringPolicy[AnyRef]
    sbtp.setContext(context)
    sbtp.setMaxFileSize("100")
    sbtp.start

    fwrp.setFileNamePattern(CoreTestConstants.OUTPUT_DIR_PREFIX + "sizeBased-test1.%i")
    try {
      fwrp.start
      fail("The absence of activeFileName option should have caused an exception.")
    }
    catch {
      case expected: IllegalStateException =>
    }
  }

  /**
   * Test basic rolling functionality.
   */
  @Test
  def smoke {
    val context = new ContextBase

    val rfa = new RollingFileAppender[AnyRef]
    rfa.setName("ROLLING")
    rfa.setEncoder(new EchoEncoder[AnyRef])
    rfa.setContext(context)
    // make the .log show first
    rfa.setFile(randomOutputDir + "a-sizeBased-smoke.log")

    val swrp = new FixedWindowRollingPolicy
    swrp.setContext(context)
    val sbtp = new SizeBasedTriggeringPolicy[AnyRef]
    sbtp.setContext(context)

    sbtp.setMaxFileSize("100")
    swrp.setMinIndex(0)
    swrp.setFileNamePattern(randomOutputDir + "sizeBased-smoke.%i")
    swrp.setParent(rfa)
    swrp.start

    rfa.setRollingPolicy(swrp)
    rfa.setTriggeringPolicy(sbtp)
    rfa.start

    val runLength = 45
    val prefix = "hello"

    for (i <- 0 to runLength - 1) {
      Thread.sleep(10)
      rfa.doAppend(prefix + i)
    }

    expectedFilenameList += randomOutputDir + "a-sizeBased-smoke.log"
    expectedFilenameList += randomOutputDir + "sizeBased-smoke.0"
    expectedFilenameList += randomOutputDir + "sizeBased-smoke.1"
    existenceCheck(expectedFilenameList)

    reverseSortedContentCheck(randomOutputDir, runLength, prefix)
  }

  /**
   * Same as testBasic but also with GZ compression.
   */
  @Test
  def test3 {
    val context = new ContextBase
    val rfa = new RollingFileAppender[AnyRef]
    rfa.setEncoder(new EchoEncoder[AnyRef])
    rfa.setContext(context)
    rfa.setFile(randomOutputDir + "a-sbr-test3.log")

    val fwrp = new FixedWindowRollingPolicy
    fwrp.setContext(context)
    val sbtp = new SizeBasedTriggeringPolicy[AnyRef]
    sbtp.setContext(context)

    sbtp.setMaxFileSize("100")
    fwrp.setMinIndex(0)
    // fwrp.setActiveFileName(Constants.TEST_DIR_PREFIX + "output/sbr-test3.log")
    fwrp.setFileNamePattern(randomOutputDir + "sbr-test3.%i.gz")
    fwrp.setParent(rfa)
    fwrp.start
    rfa.setRollingPolicy(fwrp)
    rfa.setTriggeringPolicy(sbtp)
    rfa.start

    val runLength = 40
    val prefix = "hello"
    for (i <- 0 to runLength - 1) {
      Thread.sleep(10)
      rfa.doAppend("hello" + i)
    }

    expectedFilenameList += randomOutputDir + "a-sbr-test3.log"
    expectedFilenameList += randomOutputDir + "sbr-test3.0.gz"
    expectedFilenameList += randomOutputDir + "sbr-test3.1.gz"

    existenceCheck(expectedFilenameList)
    reverseSortedContentCheck(randomOutputDir, runLength, prefix)
  }
}
