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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.sift.AppenderTracker;

/**
 * This is an alternative (slower) implementation of AppenderTracker for testing
 * purposes.
 * 
 * @author Ceki Gulcu
 */
public class AppenderTrackerTImpl implements AppenderTracker<Object> {

  List<TEntry> entryList = new LinkedList<TEntry>();
  long lastCheck = 0;

  public AppenderTrackerTImpl() {
  }

  @SuppressWarnings("unchecked")
  synchronized public void put(String k, Appender<Object> appender,
      long timestamp) {
    TEntry te = getEntry(k);
    if (te != null) {
      te.timestamp = timestamp;
    } else {
      te = new TEntry(k, appender, timestamp);
      entryList.add(te);
    }
    Collections.sort(entryList);
  }

  @SuppressWarnings("unchecked")
  synchronized public Appender<Object> get(String k, long timestamp) {
    TEntry te = getEntry(k);
    if (te == null) {
      return null;
    } else {
      te.timestamp = timestamp;
      Collections.sort(entryList);
      return te.appender;
    }
  }

  synchronized public void stopStaleAppenders(long timestamp) {
    if (lastCheck + MILLIS_IN_ONE_SECOND > timestamp) {
      return;
    }
    lastCheck = timestamp;
    while (entryList.size() != 0 && isEntryStale(entryList.get(0), timestamp)) {
      entryList.remove(0);
    }
  }

  final private boolean isEntryStale(TEntry entry, long now) {
    return ((entry.timestamp + THRESHOLD) < now);
  }

  synchronized public List<String> keyList() {
    List<String> keyList = new ArrayList<String>();
    for (TEntry e : entryList) {
      keyList.add(e.key);
    }
    return keyList;
  }

  synchronized public List<Appender<Object>> valueList() {
    List<Appender<Object>> appenderList = new ArrayList<Appender<Object>>();
    for (TEntry e : entryList) {
      appenderList.add(e.appender);
    }
    return appenderList;
  }

  private TEntry getEntry(String k) {
    for (int i = 0; i < entryList.size(); i++) {
      TEntry te = entryList.get(i);
      if (te.key.equals(k)) {
        return te;
      }
    }
    return null;
  }
}
