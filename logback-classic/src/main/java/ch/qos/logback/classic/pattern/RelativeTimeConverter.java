/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
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
