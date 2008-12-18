/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.sift;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.qos.logback.core.Appender;

/**
 * Track appenders by a key. When an appender is not used for
 * longer than THRESHOLD, stop it.
 * @author Ceki Gulcu
 */
public class AppenderTrackerImpl<E> implements AppenderTracker<E> {

  Map<String, Entry> map = new HashMap<String, Entry>();
 
  Entry head; // least recently used entries are towards the head
  Entry tail; // most recently used entries are towards the tail

  long lastCheck = 0;

  AppenderTrackerImpl() {
    head = new Entry(null, null, 0);
    tail = head;
  }

  /* (non-Javadoc)
   * @see ch.qos.logback.classic.hoard.AppenderTracker#put(java.lang.String, ch.qos.logback.core.Appender, long)
   */
  public synchronized void put(String key, Appender<E> value, long timestamp) {
    Entry entry = map.get(key);
    if (entry == null) {
      entry = new Entry(key, value, timestamp);
      map.put(key, entry);
    }
    moveToTail(entry);
  }

  /* (non-Javadoc)
   * @see ch.qos.logback.classic.hoard.AppenderTracker#get(java.lang.String, long)
   */
  public synchronized Appender<E> get(String key, long timestamp) {
    Entry existing = map.get(key);
    if (existing == null) {
      return null;
    } else {
      existing.setTimestamp(timestamp);
      moveToTail(existing);
      return existing.value;
    }
  }

  
  /* (non-Javadoc)
   * @see ch.qos.logback.classic.hoard.AppenderTracker#stopStaleAppenders(long)
   */
  public synchronized void stopStaleAppenders(long now) {
    if (lastCheck + MILLIS_IN_ONE_SECOND > now) {
      return;
    }
    lastCheck = now;
    while (head.value != null && isEntryStale(head,now)) {
      Appender appender = head.value;
      //System.out.println("  stopping "+appender);
      appender.stop();
      removeHead();
    }
  } 

  public List<String> keyList() {
    List<String> result = new LinkedList<String>();
    Entry e = head;
    while (e != tail) {
      result.add(e.key);
      e = e.next;
    }
    return result;
  }
  
  
  final private boolean isEntryStale(Entry entry, long now) {
    return ((entry.timestamp + THRESHOLD) < now);
  }

  
  private void removeHead() {
    // System.out.println("RemoveHead called");
    map.remove(head.key);
    head = head.next;
    head.prev = null;
  }

  private void moveToTail(Entry e) {
    rearrangePreexistingLinks(e);
    rearrangeTailLinks(e);
  }

  private void rearrangePreexistingLinks(Entry e) {
    if (e.prev != null) {
      e.prev.next = e.next;
    }
    if (e.next != null) {
      e.next.prev = e.prev;
    }
    if (head == e) {
      head = e.next;
    }
  }

  private void rearrangeTailLinks(Entry e) {
    if (head == tail) {
      head = e;
    }
    Entry preTail = tail.prev;
    if (preTail != null) {
      preTail.next = e;
    }
    e.prev = preTail;
    e.next = tail;
    tail.prev = e;
  }

  public void dump() {
    Entry e = head;
    System.out.print("N:");
    while (e != null) {
      // System.out.print(e+"->");
      System.out.print(e.key + ", ");
      e = e.next;
    }
    System.out.println();
  }



  public List<Appender<E>> valueList() {
    List<Appender<E>> result = new LinkedList<Appender<E>>();
    Entry e = head;
    while (e != tail) {
      result.add(e.value);
      e = e.next;
    }
    return result;
  }
  
  // ================================================================
  private class Entry {
    Entry next;
    Entry prev;

    String key;
    Appender<E> value;
    long timestamp;

    Entry(String k, Appender<E> v, long timestamp) {
      this.key = k;
      this.value = v;
      this.timestamp = timestamp;
    }

    public long getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((key == null) ? 0 : key.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final Entry other = (Entry) obj;
      if (key == null) {
        if (other.key != null)
          return false;
      } else if (!key.equals(other.key))
        return false;
      if (value == null) {
        if (other.value != null)
          return false;
      } else if (!value.equals(other.value))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return "(" + key + ", " + value + ")";
    }
  }

}
