/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic.spi;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.qos.logback.classic.util.TeztHelper;
import ch.qos.logback.core.util.SystemInfo;

public class PackagingDataCalculatorTest {

  public void verify(ThrowableProxy tp) {
    for (StackTraceElementProxy step : tp.getStackTraceElementProxyArray()) {
      if (step != null) {
        assertNotNull(step.getClassPackagingData());
      }
    }
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

  @Test
  // Test http://jira.qos.ch/browse/LBCLASSIC-125
  public void noClassDefFoundError() {
    Throwable t = new Throwable("x");
    ThrowableProxy tp = new ThrowableProxy(t);
    StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
    StackTraceElement bogusSTE = new StackTraceElement(MyBogus.class.getName(),
        "myMethod", "myFile", 12);
    StackTraceElementProxy bogusSTEP;// = new StackTraceElementProxy(bogusSTE);
    System.out.println(stepArray.length);
    for (int i = 0; i < stepArray.length; i++) {
      System.out.println(i);
      
      stepArray[i] = new StackTraceElementProxy(bogusSTE);
    }
    PackagingDataCalculator pdc = tp.getPackagingDataCalculator();
    pdc.calculate(tp);
    System.out.println(ThrowableProxyUtil.asString(tp));
    
    System.out.println(new MyBogus());
    
  }
}
