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

import java.util.*;

/**
 * An abstract implementation of the ComponentTracker interface.
 *
 * @author Ceki Gulcu
 * @author Tommy Becker
 * @author David Roussel
 * @param <C>
 */
abstract public class AbstractComponentTracker<C> implements ComponentTracker<C> {
  private static final boolean ACCESS_ORDERED = true;

  // Components in lingering state last 10 seconds
  final static long LINGERING_TIMEOUT = 10*CoreConstants.MILLIS_IN_ONE_SECOND;

  protected int maxComponents;
  protected long timeout;


  Map<String, Entry> mainMap = new LinkedHashMap<String, Entry>(16, .75f, ACCESS_ORDERED) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, Entry> mapEntry) {
      if (size() > maxComponents) {
        C component = mapEntry.getValue().component;
        stop(component);
        return true;
      }
      return false;
    }
  };

  Map<String, Entry> lingerersMap = new LinkedHashMap<String, Entry>(16, .75f, ACCESS_ORDERED);
  long lastCheck = 0;

  /**
   * Stop or clean the component.
   * @param component
   */
  abstract protected void stop(C component);

  /**
   * Build a component based on the key.
   * @param key
   * @return
   */
  abstract protected C buildComponent(String key);

  /**
   * Components can declare themselves stale. Such components may be
   * removed before they time out.
   *
   * @param c
   * @return
   */
  protected abstract boolean isComponentStale(C c);


  public int getComponentCount() {
    return mainMap.size()+lingerersMap.size();
  }

  /**
   * Get an entry from the mainMap, if not found search the lingerersMap.
   * @param key
   * @return
   */
  private Entry getFromEitherMap(String key) {
    Entry entry = mainMap.get(key);
    if(entry != null)
      return entry;
    else {
      return lingerersMap.get(key);
    }
  }

  public synchronized C getOrCreate(String key, long timestamp) {
    Entry entry = getFromEitherMap(key);
    if (entry == null) {
      C c = buildComponent(key);
      entry = new Entry(key, c, timestamp);
      // new entries go into the main map
      mainMap.put(key, entry);
    } else {
      entry.setTimestamp(timestamp);
    }
    return entry.component;
  }

  /**
   * Clear (and detach) components which are stale. Components which have not
   * been accessed for more than a user-specified duration are deemed stale.
   *
   *
   * @param now
   */
  public synchronized void removeStaleComponents(long now) {
    if (isTooSoonForRemovalIteration(now)) return;
    removeStaleComponentsFromMainMap(now);
    removeStaleComponentsFromLingerersMap(now);
  }

  private void removeStaleComponentsFromMainMap(long now) {
    Iterator<Map.Entry<String, Entry>> iter = mainMap.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<String, Entry> mapEntry = iter.next();
      Entry entry = mapEntry.getValue();
      if (isEntryStale(entry, now)) {
        iter.remove();
        C c = entry.component;
        stop(c);
      } else {
        // if an entry is not stale, then the following entries won't be stale either
        break;
      }
    }
  }

  private void removeStaleComponentsFromLingerersMap(long now) {
    Iterator<Map.Entry<String, Entry>> iter = lingerersMap.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<String, Entry> mapEntry = iter.next();
      Entry entry = mapEntry.getValue();
      if (isEntryDoneLingering(entry, now)) {
        iter.remove();
        C c = entry.component;
        stop(c);
      } else {
        // if an entry is not stale, then the following entries won't be stale either
        break;
      }
    }
  }

  private boolean isTooSoonForRemovalIteration(long now) {
    if (lastCheck + CoreConstants.MILLIS_IN_ONE_SECOND > now) {
      return true;
    }
    lastCheck = now;
    return false;
  }


  private boolean isEntryStale(Entry entry, long now) {
    // stopped or improperly started appenders are considered stale
    // see also http://jira.qos.ch/browse/LBCLASSIC-316
    C c = entry.component;
    if(!isComponentStale(c))
      return true;

    return ((entry.timestamp + timeout) < now);
  }

  private boolean isEntryDoneLingering(Entry entry, long now) {
    return ((entry.timestamp + LINGERING_TIMEOUT) < now);
  }

  protected Set<String> keyList() {
    return mainMap.keySet();
  }


  /**
   * Mark component identified by 'key' as having reached its end-of-life.
   * @param key
   */
  public void endOfLife(String key) {
    Entry entry = mainMap.remove(key);
    entry.lingering = true;
    lingerersMap.put(key, entry);
  }

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  public int getMaxComponents() {
    return maxComponents;
  }

  public void setMaxComponents(int maxComponents) {
    this.maxComponents = maxComponents;
  }

  // ================================================================
  private class Entry {
    String key;
    C component;
    long timestamp;
    boolean lingering = false;

    Entry(String k, C c, long timestamp) {
      this.key = k;
      this.component = c;
      this.timestamp = timestamp;
    }

    public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
      return key.hashCode();
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
      if (component == null) {
        if (other.component != null)
          return false;
      } else if (!component.equals(other.component))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return "(" + key + ", " + component + ")";
    }
  }
}
