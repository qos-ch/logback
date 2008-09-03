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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An lru cache based on Java's LinkedHashMap.
 * 
 * @author Ceki Gulcu
 *
 * @param <K>
 * @param <V>
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
  private static final long serialVersionUID = -6592964689843698200L;

  final int cacheSize;

  public LRUCache(int cacheSize) {
    super((int) (cacheSize*(4.0f/3)), 0.75f, true);
    if(cacheSize < 1) {
      throw new IllegalArgumentException("Cache size cannnot be smaller than 1");
   } 
    this.cacheSize = cacheSize;
  }
  
  protected boolean removeEldestEntry(Map.Entry eldest) {
    return (size() > cacheSize);
  }
  
  List<K> keyList() {
    return new ArrayList<K>(keySet());
  }
}
