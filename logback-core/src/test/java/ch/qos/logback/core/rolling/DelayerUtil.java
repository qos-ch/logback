/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
