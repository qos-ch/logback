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
  String timesmapStr = null;
  
  public String convert(ILoggingEvent event) {
    long timestamp = event.getTimeStamp();
    
    // if called multiple times within the same millisecond
    // return old value
    if(timestamp == lastTimestamp) {
      return timesmapStr;
    } else {
      lastTimestamp = timestamp;
      timesmapStr = Long.toString(timestamp - event.getContextBirthTime());
      return timesmapStr;
    }
  }
}
