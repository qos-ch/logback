/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.spi;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.helpers.CyclicBuffer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class CyclicBufferTrackerImpl<E> implements CyclicBufferTracker<E> {

  int bufferSize = DEFAULT_BUFFER_SIZE;
  int maxNumBuffers = DEFAULT_NUMBER_OF_BUFFERS;
  int bufferCount = 0;

  // 5 minutes
  static final int DELAY_BETWEEN_CLEARING_STALE_BUFFERS = 300 * CoreConstants.MILLIS_IN_ONE_SECOND;


  boolean isStarted = false;

  private Map<String, Entry> map = new HashMap<String, Entry>();

  private Entry head; // least recently used entries are towards the head
  private Entry tail; // most recently used entries are towards the tail
  long lastCheck = 0;


  public CyclicBufferTrackerImpl() {
    head = new Entry(null, null, 0);
    tail = head;
  }

  public int getBufferSize() {
    return bufferSize;
  }

  public void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
  }

  public int getMaxNumberOfBuffers() {
    return maxNumBuffers;
  }

  public void setMaxNumberOfBuffers(int maxNumBuffers) {
    this.maxNumBuffers = maxNumBuffers;
  }

  public CyclicBuffer<E> getOrCreate(String key, long timestamp) {
    Entry existing = map.get(key);
    if (existing == null) {
      return processNewEntry(key, timestamp);
    } else {
      existing.setTimestamp(timestamp);
      moveToTail(existing);
      return existing.value;
    }
  }

  public void removeBuffer(String key) {
    Entry existing = map.get(key);
    if (existing != null) {
      bufferCount--;
      map.remove(key);
      unlink(existing);
      CyclicBuffer<E> cb = existing.value;
      if(cb != null) {
        cb.clear();
      }
    }
  }

  private CyclicBuffer<E> processNewEntry(String key, long timestamp) {
    CyclicBuffer<E> cb = new CyclicBuffer<E>(bufferSize);
    Entry entry = new Entry(key, cb, timestamp);
    map.put(key, entry);
    bufferCount++;
    linkBeforeTail(entry);
    if (bufferCount >= maxNumBuffers) {
      removeHead();
    }
    return cb;
  }

  private void removeHead() {
    CyclicBuffer<E> cb = head.value;
    if (cb != null) {
      cb.clear();
    }
    map.remove(head.key);
    bufferCount--;
    head = head.next;
    head.prev = null;
  }

  private void moveToTail(Entry e) {
    unlink(e);
    linkBeforeTail(e);
  }

  private void unlink(Entry e) {
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


  public synchronized void clearStaleBuffers(long now) {
    if (lastCheck + DELAY_BETWEEN_CLEARING_STALE_BUFFERS > now) {
      return;
    }
    lastCheck = now;

    while (head.value != null && isEntryStale(head, now)) {
      removeHead();
    }
  }

  public int size() {
    return map.size();
  }

  private boolean isEntryStale(Entry entry, long now) {
    return ((entry.timestamp + THRESHOLD) < now);
  }

  List<String> keyList() {
    List<String> result = new LinkedList<String>();
    Entry e = head;
    while (e != tail) {
      result.add(e.key);
      e = e.next;
    }
    return result;
  }

  private void linkBeforeTail(Entry e) {
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

  // ================================================================

  private class Entry {
    Entry next;
    Entry prev;

    String key;
    CyclicBuffer<E> value;
    long timestamp;

    Entry(String k, CyclicBuffer<E> v, long timestamp) {
      this.key = k;
      this.value = v;
      this.timestamp = timestamp;
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
      @SuppressWarnings("unchecked")
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
