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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class CyclicBufferTracker_TImpl<E> implements CyclicBufferTracker<E> {

  int bufferSize = DEFAULT_BUFFER_SIZE;
  int maxNumBuffers = DEFAULT_NUMBER_OF_BUFFERS;

  List<TEntry<E>> entryList = new LinkedList<TEntry<E>>();
  long lastCheck = 0;

  public int getBufferSize() {
    return bufferSize;
  }

  public void setBufferSize(int size) {
  }

  public int getMaxNumberOfBuffers() {
    return maxNumBuffers;
  }

  public void setMaxNumberOfBuffers(int maxNumBuffers) {
    this.maxNumBuffers = maxNumBuffers;
  }

  private TEntry<E> getEntry(String k) {
    for (int i = 0; i < entryList.size(); i++) {
      TEntry<E> te = entryList.get(i);
      if (te.key.equals(k)) {
        return te;
      }
    }
    return null;
  }

  List<String> keyList() {
    Collections.sort(entryList);

    List<String> result = new LinkedList<String>();
    for (int i = 0; i < entryList.size(); i++) {
      TEntry<E> te = entryList.get(i);
      result.add(te.key);
    }
    return result;
  }


  public CyclicBuffer<E> getOrCreate(String key, long timestamp) {
    TEntry<E> te = getEntry(key);
    if (te == null) {
      CyclicBuffer<E> cb = new CyclicBuffer<E>(bufferSize);
      te = new TEntry<E>(key, cb, timestamp);
      entryList.add(te);
      if (entryList.size() >= maxNumBuffers) {
        entryList.remove(0);
      }
      return cb;
    } else {
      te.timestamp = timestamp;
      Collections.sort(entryList);
      return te.value;
    }

  }

  public void removeBuffer(String k) {
    for (int i = 0; i < entryList.size(); i++) {
      TEntry<E> te = entryList.get(i);
      if (te.key.equals(k)) {
        entryList.remove(i);
        return;
      }
    }
  }

  private boolean isEntryStale(TEntry<E> entry, long now) {
    return ((entry.timestamp + THRESHOLD) < now);
  }

  public void clearStaleBuffers(long now) {
    if (lastCheck + CoreConstants.MILLIS_IN_ONE_SECOND > now) {
      return;
    }
    lastCheck = now;
    Collections.sort(entryList);
    while (entryList.size() != 0 && isEntryStale(entryList.get(0), now)) {
      entryList.remove(0);
    }
  }

  public int size() {
    return entryList.size();
  }


  // ==================================================================

  private class TEntry<X> implements Comparable<TEntry<?>> {

    String key;
    CyclicBuffer<E> value;
    long timestamp;

    TEntry(String k, CyclicBuffer<E> v, long timestamp) {
      this.key = k;
      this.value = v;
      this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((key == null) ? 0 : key.hashCode());
      return result;
    }

    public int compareTo(TEntry<?> o) {
      if (!(o instanceof TEntry)) {
        throw new IllegalArgumentException("arguments must be of type " + TEntry.class);
      }

      TEntry<?> other = (TEntry<?>) o;
      if (timestamp > other.timestamp) {
        return 1;
      }
      if (timestamp == other.timestamp) {
        return 0;
      }
      return -1;
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
      final TEntry<?> other = (TEntry<?>) obj;
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
