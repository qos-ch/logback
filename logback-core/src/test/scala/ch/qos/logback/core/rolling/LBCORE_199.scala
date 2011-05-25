package ch.qos.logback.core.rolling

import org.junit.{Before, Test}
import ch.qos.logback.core.encoder.EchoEncoder
import ch.qos.logback.core.util.StatusPrinter

/**
 * Created by IntelliJ IDEA.
 * User: ceki
 * Date: 09.03.11
 * Time: 18:11
 * To change this template use File | Settings | File Templates.
 */

class LBCORE_199 extends RollingScaffolding {

  private[rolling] var rfa: RollingFileAppender[AnyRef] = new RollingFileAppender[AnyRef]
  private[rolling] var fwrp: FixedWindowRollingPolicy[AnyRef] = new FixedWindowRollingPolicy[AnyRef]
  private[rolling] var triggeringPolicy = new SizeBasedTriggeringPolicy[AnyRef]
  private[rolling] var encoder: EchoEncoder[AnyRef] = new EchoEncoder[AnyRef]

  @Before
  def setUp: Unit = {
    setUpScaffolding
    fwrp.setContext(context)
    rfa.setContext(context)
    triggeringPolicy.setContext(context)
  }

  private[rolling] def initRFA(filename: String): Unit = {
    rfa.setEncoder(encoder)
    if (filename != null) {
      rfa.setFile(filename)
    }
  }

  @Test
  def smoke() {
    initRFA("toto.log")
    fwrp.setFileNamePattern("tests.%i.log.zip")
    fwrp.minIndex = 1
    fwrp.maxIndex = 3
    fwrp.setParent(rfa)
    fwrp.start
    triggeringPolicy.setMaxFileSize("20")
    triggeringPolicy.start
    rfa.triggeringPolicy = triggeringPolicy
    rfa.rollingPolicy = fwrp
    rfa.start

    for (i <- 1 to 100) {
      Thread.sleep(10)
      rfa.doAppend("hello "+i)
    }

    StatusPrinter.print(context)

  }


}