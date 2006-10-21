/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.status;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

abstract public class StatusBase implements Status {

  static private final List<Status> EMPTY_LIST = new ArrayList<Status>(0);
  
  int level;
  final String message;
  final Object origin;
  List<Status> childrenList;
  Throwable throwable;

  StatusBase(int level, String msg, Object origin) {
    this(level, msg, origin, null);
  }

  StatusBase(int level, String msg, Object origin, Throwable t) {
    this.level = level;
    this.message = msg;
    this.origin = origin;
    this.throwable = t;
  }

  public synchronized void add(Status child) {
    if (child == null) {
      throw new NullPointerException("Null values are not valid Status.");
    }
    if (childrenList == null) {
      childrenList = new ArrayList<Status>();
    }
    childrenList.add(child);
  }

  public synchronized boolean hasChildren() {
    return ((childrenList != null) && (childrenList.size() > 0));
  }

  public synchronized Iterator<Status> iterator() {
    if (childrenList != null) {
      return childrenList.iterator();
    } else {
      return EMPTY_LIST.iterator();
    }
  }

  public synchronized boolean remove(Status statusToRemove) {
    if (childrenList == null) {
      return false;
    }

    // TODO also search in childrens' childrens
    return childrenList.remove(statusToRemove);

  }

  public int getLevel() {
    return level;
  }

  public int getEffectiveLevel() {
    int result = level;
    int effLevel;

    Iterator it = iterator();
    Status s;
    while (it.hasNext()) {
      s = (Status) it.next();
      effLevel = s.getEffectiveLevel();
      if (effLevel > result) {
        result = effLevel;
      }
    }

    return result;
  }

  public String getMessage() {
    return message;
  }

  public Object getOrigin() {
    return origin;
  }

  public Throwable getThrowable() {
    return throwable;
  }

  /**
   * @Override
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    switch (getEffectiveLevel()) {
    case INFO:
      buf.append("INFO");
      break;
    case WARN:
      buf.append("WARN");
      break;
    case ERROR:
      buf.append("ERROR");
      break;
    }
    if (origin != null) {
      buf.append(" in ");
      buf.append(origin);
      buf.append(" -");
    }

    buf.append(" ");
    buf.append(message);

    if (throwable != null) {
      buf.append(" ");
      buf.append(throwable);
    }

    return buf.toString();
  }

}
