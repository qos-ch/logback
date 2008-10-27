/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.status.StatusManager;

public class BasicStatusManager implements StatusManager {

  public static final int MAX_COUNT = 200;

  // This method is synchronized on the instance.
  // Code
  int count = 0;
  
  // reading SynchronizedCollection the mutex is the returned 
  // synchronized list, we make use of this fact in getCopyOfStatusList
  final List<Status> statusList = Collections
      .synchronizedList(new ArrayList<Status>());
  int level = Status.INFO;

  // reading SynchronizedCollection the mutex is the returned 
  // synchronized list, we make use of this fact in getCopyOfStatusListnerList
  final List<StatusListener> statusListenerList = Collections
      .synchronizedList(new ArrayList<StatusListener>());

  /**
   * Add a new status object.
   * 
   * @param Status
   *                the status message to add
   */
  public void add(Status newStatus) {
    if (count > MAX_COUNT) {
      return;
    }
    count++;

    if (newStatus.getLevel() > level) {
      level = newStatus.getLevel();
    }
    statusList.add(newStatus);
    fireStatusAddEvent(newStatus);
  }
  
  private void fireStatusAddEvent(Status status) {
    synchronized (statusListenerList) {
      for(StatusListener sl : statusListenerList) {
        sl.addStatusEvent(status);
      }    
    }  
  }

  public List<Status> getCopyOfStatusList() {
    synchronized (statusList) {
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
    statusListenerList.add(listener);
  }

  public void remove(StatusListener listener) {
    statusListenerList.remove(listener);
  }

  public List<StatusListener> getCopyOfStatusListenerList() {
    synchronized (statusListenerList) {
      return new ArrayList<StatusListener>(statusListenerList);
    }
  }

}
