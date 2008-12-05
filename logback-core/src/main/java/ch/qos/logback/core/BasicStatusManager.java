/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.status.StatusManager;

public class BasicStatusManager implements StatusManager {

  public static final int MAX_COUNT = 200;

  int count = 0;

  // protected access was requested in http://jira.qos.ch/browse/LBCORE-36
  final protected List<Status> statusList = new ArrayList<Status>();
  final protected Object statusListLock = new Object();

  int level = Status.INFO;

  // protected access was requested in http://jira.qos.ch/browse/LBCORE-36
  final protected List<StatusListener> statusListenerList = new ArrayList<StatusListener>();
  final protected Object statusListenerListLock = new Object();

  // Note on synchronization
  // This class contains two separate locks statusListLock and
  // statusListenerListLock guarding respectively the statusList and
  // statusListenerList fields. The locks are used internally
  // without cycles. They are exposed to derived classes which should be careful
  // not to create deadlock cycles.

  /**
   * Add a new status object.
   * 
   * @param Status
   *                the status message to add
   */
  public void add(Status newStatus) {
    // LBCORE-72: fire event before the count check
    fireStatusAddEvent(newStatus);

    if (count > MAX_COUNT) {
      return;
    }
    count++;

    if (newStatus.getLevel() > level) {
      level = newStatus.getLevel();
    }

    synchronized (statusListLock) {
      statusList.add(newStatus);
    }

  }

  private void fireStatusAddEvent(Status status) {
    synchronized (statusListenerListLock) {
      for (StatusListener sl : statusListenerList) {
        sl.addStatusEvent(status);
      }
    }
  }

  public List<Status> getCopyOfStatusList() {
    synchronized (statusListLock) {
      return new ArrayList<Status>(statusList);
    }
  }

  public int getLevel() {
    return level;
  }

  public int getCount() {
    return count;
  }

  public void add(StatusListener listener) {
    synchronized (statusListenerListLock) {
      statusListenerList.add(listener);
    }
  }

  public void remove(StatusListener listener) {
    synchronized (statusListenerListLock) {
      statusListenerList.remove(listener);
    }
  }

  public List<StatusListener> getCopyOfStatusListenerList() {
    synchronized (statusListenerListLock) {
      return new ArrayList<StatusListener>(statusListenerList);
    }
  }

}
