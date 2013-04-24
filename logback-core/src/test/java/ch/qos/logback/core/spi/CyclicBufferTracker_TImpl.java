/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
public class CyclicBufferTracker_TImpl<E> implements ComponentTracker<CyclicBuffer<E>> {

  int bufferSize = CyclicBufferTrackerImpl.DEFAULT_BUFFER_SIZE;
  int maxComponents = CyclicBufferTrackerImpl.DEFAULT_NUMBER_OF_BUFFERS;

  List<TEntry<E>> mainList = new LinkedList<TEntry<E>>();
  List<TEntry<E>> lingererList = new LinkedList<TEntry<E>>();

  long lastCheck = 0;


  private TEntry<E> getEntry(List<TEntry<E>> list,String k) {
    for (int i = 0; i < list.size(); i++) {
      TEntry<E> te = list.get(i);
      if (te.key.equals(k)) {
        return te;
      }
    }
    return null;
  }

  private TEntry getFromEitherMap(String key) {
    TEntry entry = getEntry(mainList, key);
    if(entry != null)
      return entry;
    else {
      return getEntry(lingererList, key);
    }
  }


  List<String> keyList() {
    Collections.sort(mainList);
    List<String> result = new LinkedList<String>();
    for (int i = 0; i < mainList.size(); i++) {
      TEntry<E> te = mainList.get(i);
      result.add(te.key);
    }
    return result;
  }



  public CyclicBuffer<E> getOrCreate(String key, long timestamp) {
    TEntry<E> te = getFromEitherMap(key);
    if (te == null) {
      CyclicBuffer<E> cb = new CyclicBuffer<E>(bufferSize);
      te = new TEntry<E>(key, cb, timestamp);
      mainList.add(te);
      if (mainList.size() > maxComponents) {
        Collections.sort(mainList);
        mainList.remove(0);
      }
    } else {
      te.timestamp = timestamp;
      Collections.sort(mainList);
    }
    return te.value;
  }

  public void endOfLife(String k) {
    TEntry<E> te = null;
    boolean found = false;
    for (int i = 0; i < mainList.size(); i++) {
      te = mainList.get(i);
      if (te.key.equals(k)) {
        mainList.remove(i);
        found = true;
        break;
      }
    }
    if(found) {
      lingererList.add(te);
    }
  }

  private boolean isEntryStale(TEntry<E> entry, long now) {
    return ((entry.timestamp + DEFAULT_TIMEOUT) < now);
  }
  private boolean isEntryDoneLingering(TEntry<E> tEntry, long now) {
    return ((tEntry.timestamp + AbstractComponentTracker.LINGERING_TIMEOUT) < now);
  }

  public void removeStaleComponents(long now) {
    if (isTooSoonForRemovalIteration(now)) return;
    removeStaleComponentsFromMainList(now);
    removeStaleComponentsFromLingerersList(now);
  }

  private void removeStaleComponentsFromMainList(long now) {
    Collections.sort(mainList);
    while (mainList.size() != 0 && isEntryStale(mainList.get(0), now)) {
      mainList.remove(0);
    }
  }

  private void removeStaleComponentsFromLingerersList(long now) {
    Collections.sort(lingererList);
    while (lingererList.size() != 0 && isEntryDoneLingering(lingererList.get(0), now)) {
      lingererList.remove(0);
    }
  }

  private boolean isTooSoonForRemovalIteration(long now) {
    if (lastCheck + CoreConstants.MILLIS_IN_ONE_SECOND > now) {
      return true;
    }
    lastCheck = now;
    return false;
  }

  public int getComponentCount() {
    return mainList.size() + lingererList.size();
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
