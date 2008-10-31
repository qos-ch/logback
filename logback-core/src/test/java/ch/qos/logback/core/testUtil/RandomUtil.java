/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.testUtil;

import java.util.Random;

public class RandomUtil {

  private static Random random = new Random();

  public static int getRandomServerPort() {
    int r = random.nextInt(20000);
    // the first 1024 ports are usually reserved for the OS
    return r + 1024;
  }

}
