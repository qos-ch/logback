package ch.qos.logback.classic.spi;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.util.TeztHelper;
import ch.qos.logback.core.util.SystemInfo;

public class BasicCPDCTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  public void verify(ThrowableProxy tp) {
    for (StackTraceElementProxy step : tp.getStackTraceElementProxyArray()) {
      if (step != null) {
        assertNotNull(step.getClassPackagingData());
      }
    }
  }

  @Test
  public void otherJD() {
    System.out.println(SystemInfo.getJavaVendor());
  }

  @Test
  public void integration() throws Exception {

  }

  @Test
  public void smoke() throws Exception {
    Throwable t = new Throwable("x");
    ThrowableProxy tp = new ThrowableProxy(t);
    PackagingDataCalculator pdc = tp.getPackagingDataCalculator();
    pdc.calculate(tp);
    verify(tp);
    tp.fullDump();
  }

  @Test
  public void nested() throws Exception {
    Throwable t = TeztHelper.makeNestedException(3);
    ThrowableProxy tp = new ThrowableProxy(t);
    PackagingDataCalculator pdc = tp.getPackagingDataCalculator();
    pdc.calculate(tp);
    verify(tp);
  }

  public void doCalculateClassPackagingData(
      boolean withClassPackagingCalculation) {
    try {
      throw new Exception("testing");
    } catch (Throwable e) {
      ThrowableProxy tp = new ThrowableProxy(e);
      if (withClassPackagingCalculation) {
        PackagingDataCalculator pdc = tp.getPackagingDataCalculator();
        pdc.calculate(tp);
      }
    }
  }

  double loop(int len, boolean withClassPackagingCalculation) {
    long start = System.nanoTime();
    for (int i = 0; i < len; i++) {
      doCalculateClassPackagingData(withClassPackagingCalculation);
    }
    return (1.0 * System.nanoTime() - start) / len / 1000;
  }

  @Test
  public void perfTest() {
    int len = 1000;
    loop(len, false);
    loop(len, true);

    double d0 = loop(len, false);
    System.out.println("without packaging info " + d0 + " microseconds");

    double d1 = loop(len, true);
    System.out.println("with    packaging info " + d1 + " microseconds");

    int slackFactor = 8;
    if (!SystemInfo.getJavaVendor().contains("Sun")) {
      // be more lenient with other JDKs
      slackFactor = 10;
    }
    assertTrue("computing class packaging data (" + d1
        + ") should have been less than " + slackFactor
        + " times the time it takes to process an exception "
        + (d0 * slackFactor), d0 * slackFactor > d1);

  }
}
