/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.turbo;

import java.util.LinkedHashMap;
import java.util.Map;

class LRUMessageCache extends LinkedHashMap<String, Integer> {

  private static final long serialVersionUID = 1L;
  
  final int cacheSize;
  
  LRUMessageCache(int cacheSize) {
    super((int) (cacheSize * (4.0f / 3)), 0.75f, true);
    if (cacheSize < 1) {
      throw new IllegalArgumentException("Cache size cannnot be smaller than 1");
    }
    this.cacheSize = cacheSize;
  }
  
  int getMessageCount(String msg) {
    Integer i = super.get(msg);
    if(i == null) {
      i = 1;
    } else {
      i = new Integer(i.intValue()+1);
    }
    super.put(msg, i);
    return i;
  }
  
  protected boolean removeEldestEntry(Map.Entry eldest) {
    return (size() > cacheSize);
  }
}
