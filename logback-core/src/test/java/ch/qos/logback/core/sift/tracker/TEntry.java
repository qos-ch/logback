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
package ch.qos.logback.core.sift.tracker;

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
