/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.control;


import junit.framework.*;

public class RandomUtilTest extends TestCase {

  public void testGaussian() {

    long sum = 0;
    int len = 100000;
    int AVERAGE = 5;
    for(int i = 0; i < len; i++) {
      sum += RandomUtil.gaussianAsPositiveInt(AVERAGE, 2);
    }

    double resultingAverage =   sum/(1.0*len);
    //System.out.println("Resulting average is "+resultingAverage);

    assertTrue("Expected "+AVERAGE+" but got "+resultingAverage, Math.abs(resultingAverage-AVERAGE)<0.1);
  }
}