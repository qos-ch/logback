/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.pattern.lru;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This is an alternative (slower) implementation of LRUCache for testing
 * purposes.
 * 
 * @author Ceki Gulcu
 */
public class T_LRUCache<K> {

  int sequenceNumber;
  final int cacheSize;
  List<T_Entry<K>> cacheList = new LinkedList<T_Entry<K>>();

  public T_LRUCache(int size) {
    this.cacheSize = size;
  }

  @SuppressWarnings("unchecked")
  public void put(K k) {
    sequenceNumber++;
    T_Entry<K> te = getEntry(k);
    if (te != null) {
      te.sequenceNumber = sequenceNumber;
    } else {
      te = new T_Entry<K>(k, sequenceNumber);
      cacheList.add(te);
    }
    Collections.sort(cacheList);
    while(cacheList.size() > cacheSize) {
      cacheList.remove(0);    
    }
  }

  @SuppressWarnings("unchecked")
  public K get(K k) {
    T_Entry<K> te = getEntry(k);
    if (te == null) {
      return null;
    } else {
      te.sequenceNumber = ++sequenceNumber;
      Collections.sort(cacheList);
      return te.k;
    }
  }

  public List<K> ketList() {
    List<K> keyList = new ArrayList<K>();
    for (T_Entry<K> e : cacheList) {
      keyList.add(e.k);
    }
    return keyList;
  }

  private T_Entry<K> getEntry(K k) {
    for (int i = 0; i < cacheList.size(); i++) {
      T_Entry<K> te = cacheList.get(i);
      if (te.k.equals(k)) {
        return te;
      }
    }
    return null;
  }
  
  public void dump() {
    System.out.print("T:");
    for (T_Entry<K> te : cacheList) {
      //System.out.print(te.toString()+"->");
      System.out.print(te.k+", ");
    }
    System.out.println();
  }

}
