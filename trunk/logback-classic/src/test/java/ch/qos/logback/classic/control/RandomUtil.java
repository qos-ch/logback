/** 
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2005, QOS.ch, LOGBack.com
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.control;

import java.util.Random;

import ch.qos.logback.classic.Level;

public class RandomUtil {
  private final static long SEED = 74130;

  private final static Random random = new Random(SEED);
  private final static int AVERAGE_ID_LEN = 32;
  private final static int AVERAGE_ID_DEV = 16;

  private final static int AVERAGE_CHILDREN_COUNT = 30;
  private final static int CHILDREN_COUNT_VAR = 10;

  public static boolean oneInFreq(int freq) {
    return (random.nextInt(freq) % freq) == 0;
  }

  public static Level randomLevel() {
    int rl = random.nextInt(4);
    switch (rl) {
      case 0:
        return Level.DEBUG;
      case 1:
        return Level.INFO;
      case 2:
        return Level.WARN;
      case 3:
        return Level.ERROR;
      default:
        throw new IllegalStateException("rl should have been a value between 0 to 3, but it is " + rl);
    }
  }

  public static String randomLoggerName(int average, int stdDeviation) {
    int depth = gaussianAsPositiveInt(average, stdDeviation);
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < depth; i++) {
      if (i != 0) {
        buf.append('.');
      }
      buf.append(randomId());
    }
    return buf.toString();
  }

  public static String randomId() {

    int len = gaussianAsPositiveInt(AVERAGE_ID_LEN, AVERAGE_ID_DEV);
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < len; i++) {
      int offset = random.nextInt(26);
      char c = (char) ('a' + offset);
      buf.append(c);
    }
    return buf.toString();
  }

  /**
   * Approximate a gaussian distrib with only only positive integer values
   *
   * @param average
   * @param stdDeviation
   * @return
   */
  public static int gaussianAsPositiveInt(int average, int stdDeviation) {
    if (average < 1) {
      throw new IllegalArgumentException("The average must not be smaller than 1.");
    }

    if (stdDeviation < 1) {
      throw new IllegalArgumentException("The stdDeviation must not be smaller than 1.");
    }

    double d = random.nextGaussian() * stdDeviation + average;
    int result = 1;
    if (d > 1.0) {
      result = (int) Math.round(d);
    }
    return result;
  }

  /**
   * Returns 3 for root, 3 for children of root, 9 for offspring of generation 2 and 3, and for generations 4
   * and later, return 0 wuth probabbility 0.5 and a guassion (average=AVERAGE_CHILDREN_COUNT) with probability 0.5.
   *
   * @param name
   * @return
   */
  public static int randomChildrenCount(String name) {
    if ("".equals(name)) {
      return 3;
    }
    int dots = dotCount(name);
    if (dots == 1) {
      return 3;
    } else if (dots == 2 || dots == 3) {
      return 9;
    } else {
      if (hasChildren(0.5)) {
        return gaussianAsPositiveInt(AVERAGE_CHILDREN_COUNT, CHILDREN_COUNT_VAR);
      } else {
        return 0;
      }
    }

  }

  /**
   * Returns true with probability p.
   *
   * @param p
   * @return
   */
  static boolean hasChildren(double p) {
    if (p < 0 || p > 1.0) {
      throw new IllegalArgumentException("p must be a value between 0 and 1.0, it was " + p + " instead.");
    }
    double r = random.nextDouble();
    if (r < p) {
      return true;
    } else {
      return false;
    }
  }

  static int dotCount(String s) {
    int count = 0;
    int len = s.length();
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      if (c == '.') {
        count++;
      }
    }
    return count;
  }
}
