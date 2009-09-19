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
package ch.qos.logback.classic.turbo;

import java.util.LinkedHashMap;
import java.util.Map;

class LRUMessageCache extends LinkedHashMap<String, Integer> {

  // LinkedHashMap permits null elements to be inserted
  
  private static final long serialVersionUID = 1L;
  
  final int cacheSize;
  
  LRUMessageCache(int cacheSize) {
    super((int) (cacheSize * (4.0f / 3)), 0.75f, true);
    if (cacheSize < 1) {
      throw new IllegalArgumentException("Cache size cannnot be smaller than 1");
    }
    this.cacheSize = cacheSize;
  }
  
  int getMessageCountAndThenIncrement(String msg) {
    // don't insert null elements
    if(msg == null) {
      return 0;
    }
    
    Integer i = super.get(msg);
    if(i == null) {
      i = 0;
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
