/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.hoard.tracker;

import ch.qos.logback.core.Appender;

public class TEntry implements Comparable {

  String key;
  long timestamp;
  Appender<Object> appender;
  
  TEntry(String key, Appender<Object> appender, long timestamp) {
    this.key = key;
    this.appender = appender;
    this.timestamp = timestamp;
  }

  public int compareTo(Object o) {
    if(!(o instanceof TEntry)) {
      throw new IllegalArgumentException("arguments must be of type "+TEntry.class);
    }
    
    TEntry other = (TEntry) o;
    if(timestamp > other.timestamp) {
      return 1;
    }
    if(timestamp == other.timestamp) {
      return 0;
    }
    return -1;
  }
  
  @Override
  public String toString() {
    return "("+key+","+timestamp+")";
  }
}
