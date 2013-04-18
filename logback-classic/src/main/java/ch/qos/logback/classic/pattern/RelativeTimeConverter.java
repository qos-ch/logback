/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class RelativeTimeConverter extends ClassicConverter {

  long lastTimestamp = -1;
  String timesmapCache = null;

  public String convert(ILoggingEvent event) {
    long now = event.getTimeStamp();

    synchronized (this) {
      // update timesmapStrCache only if now !=  lastTimestamp
      if (now != lastTimestamp) {
        lastTimestamp = now;
        timesmapCache = Long.toString(now - event.getLoggerContextVO().getBirthTime());      
      }
      return timesmapCache;
    }
  }
}
