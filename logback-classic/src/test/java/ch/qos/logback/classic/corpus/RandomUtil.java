/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.corpus;

import java.util.Random;

public class RandomUtil {

  
  /**
   * Approximate a gaussian distrib with only positive integer values
   * 
   * @param average
   * @param stdDeviation
   * @return
   */
  static public int gaussianAsPositiveInt(Random random, int average, int stdDeviation) {
    if (average < 1) {
      throw new IllegalArgumentException(
          "The average must not be smaller than 1.");
    }

    if (stdDeviation < 1) {
      throw new IllegalArgumentException(
          "The stdDeviation must not be smaller than 1.");
    }

    double d = random.nextGaussian() * stdDeviation + average;
    int result = 1;
    if (d > 1.0) {
      result = (int) Math.round(d);
    }
    return result;
  }
}
