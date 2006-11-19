/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.rolling;

import java.util.Calendar;
import java.util.Date;

public class DelayerUtil {
  
  // delay until millis in the next second
  static void delayUntilNextSecond(int millis) {
    long now = System.currentTimeMillis();
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date(now));

    cal.set(Calendar.MILLISECOND, millis);
    cal.add(Calendar.SECOND, 1);

    long next = cal.getTime().getTime();

    try {
      Thread.sleep(next - now);
    } catch (Exception e) {
    }
  }

  static void delayUntilNextMinute(int seconds) {
    long now = System.currentTimeMillis();
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date(now));

    cal.set(Calendar.SECOND, seconds);
    cal.add(Calendar.MINUTE, 1);

    long next = cal.getTime().getTime();

    try {
      Thread.sleep(next - now);
    } catch (Exception e) {
    }
  }

}
