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
import java.util.Iterator;
import java.util.List;

import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;

public class BasicStatusManager implements StatusManager {

  public static final int MAX_COUNT = 200;

  int count = 0;
  List<Status> statusList = new ArrayList<Status>();
  int level = Status.INFO;

  // This method is synchronized on the instance.
  // Code iterating on this.iterator is expected to
  // also synchronize on this (the BasicStatusManager instance)
  public synchronized void add(Status newStatus) {
    // System.out.println(newStatus);
    if (count > MAX_COUNT) {
      return;
    }
    count++;

    if (newStatus.getLevel() > level) {
      level = newStatus.getLevel();
    }
    statusList.add(newStatus);
  }

  public Iterator<Status> iterator() {
    return statusList.iterator();
  }

  public int getLevel() {
    return level;
  }

  public int getCount() {
    return count;
  }

}
