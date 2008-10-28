/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.spi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ch.qos.logback.core.Appender;

/**
 * A straightforward implementation of the {@link AppenderAttachable} interface.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class AppenderAttachableImpl<E> implements AppenderAttachable<E> {

  final private List<Appender<E>> appenderList = new ArrayList<Appender<E>>();
  final private ReadWriteLock rwLock = new ReentrantReadWriteLock();
  private final Lock r = rwLock.readLock();
  private final Lock w = rwLock.writeLock();

  /**
   * Attach an appender. If the appender is already in the list in won't be
   * added again.
   */
  public void addAppender(Appender<E> newAppender) {
    if (newAppender == null) {
      throw new IllegalArgumentException("Null argument disallowed");
    }
    w.lock();
    if (!appenderList.contains(newAppender)) {
      appenderList.add(newAppender);
    }
    w.unlock();
  }

  /**
   * Call the <code>doAppend</code> method on all attached appenders.
   */
  public int appendLoopOnAppenders(E e) {
    int size = 0;
    r.lock();
    try {
      for (Appender<E> appender : appenderList) {
        appender.doAppend(e);
        size++;
      }
    } finally {
      r.unlock();
    }
    return size;
  }

  /**
   * Get all attached appenders as an Enumeration. If there are no attached
   * appenders <code>null</code> is returned.
   * 
   * @return Iterator An iterator of attached appenders.
   */
  public Iterator<Appender<E>> iteratorForAppenders() {
    List<Appender<E>> copy;
    r.lock();
    copy = new ArrayList<Appender<E>>(appenderList);
    r.unlock();
    return copy.iterator();
  }

  /**
   * Look for an attached appender named as <code>name</code>.
   * 
   * <p>
   * Return the appender with that name if in the list. Return null otherwise.
   * 
   */
  public Appender<E> getAppender(String name) {
    if (name == null) {
      return null;
    }
    r.lock();

    for (Appender<E> appender : appenderList) {
      if (name.equals(appender.getName())) {
        r.unlock();
        return appender;
      }
    }
    r.unlock();
    return null;
  }

  /**
   * Returns <code>true</code> if the specified appender is in the list of
   * attached appenders, <code>false</code> otherwise.
   * 
   * @since 1.2
   */
  public boolean isAttached(Appender appender) {
    if (appender == null) {
      return false;
    }
    r.lock();
    for (Appender<E> a : appenderList) {
      if (a == appender) {
        r.unlock();
        return true;
      }
    }
    r.unlock();
    return false;
  }

  /**
   * Remove and stop all previously attached appenders.
   */
  public void detachAndStopAllAppenders() {
    try {
     w.lock();
      for (Appender<E> a : appenderList) {
       a.stop();
     }
    appenderList.clear();
    } finally {
      w.unlock();
    }
  }

  /**
   * Remove the appender passed as parameter form the list of attached
   * appenders.
   */
  public boolean detachAppender(Appender appender) {
    if (appender == null) {
      return false;
    }
    w.lock();
    boolean result = appenderList.remove(appender);
    w.unlock();
    return result;
  }

  /**
   * Remove the appender with the name passed as parameter form the list of
   * appenders.
   */
  public boolean detachAppender(String name) {
    if (name == null) {
      return false;
    }
    w.lock();
    for (Appender<E> a : appenderList) {
      if (name.equals((a).getName())) {
        w.unlock();
        return appenderList.remove(a);
      }
    }
    w.unlock();
    return false;
  }
}
