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
package ch.qos.logback.classic.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.spi.MDCAdapter;

/**
 * A <em>Mapped Diagnostic Context</em>, or MDC in short, is an instrument for
 * distinguishing interleaved log output from different sources. Log output is
 * typically interleaved when a server handles multiple clients
 * near-simultaneously.
 * <p/>
 * <b><em>The MDC is managed on a per thread basis</em></b>. A child thread
 * automatically inherits a <em>copy</em> of the mapped diagnostic context of
 * its parent.
 * <p/>
 * <p/>
 * For more information about MDC, please refer to the online manual at
 * http://logback.qos.ch/manual/mdc.html
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class LogbackMDCAdapter implements MDCAdapter {


  // We no longer use CopyOnInheritThreadLocal in order to solve LBCLASSIC-183
  // Initially the contents of the thread local in parent and child threads
  // reference the same map. However, as soon as a thread invokes the put()
  // method, the maps diverge as they should.
  final InheritableThreadLocal<HashMap<String, String>> copyOnInheritThreadLocal = new InheritableThreadLocal<HashMap<String, String>>();

  private static final int WRITE_OPERATION = 1;
  private static final int READ_OPERATION = 2;

  final ThreadLocal<Integer> lastOperation = new ThreadLocal<Integer>();



  public LogbackMDCAdapter() {
  }

  private Integer getAndSetLastOperation(int op) {
    Integer lastOp = lastOperation.get();
    lastOperation.set(WRITE_OPERATION);
    return lastOp;
  }

  /**
   * Put a context value (the <code>val</code> parameter) as identified with the
   * <code>key</code> parameter into the current thread's context map. Note that
   * contrary to log4j, the <code>val</code> parameter can be null.
   * <p/>
   * <p/>
   * If the current thread does not have a context map it is created as a side
   * effect of this call.
   * <p/>
   * <p/>
   * Each time a value is added, a new instance of the map is created. This is
   * to be certain that the serialization process will operate on the updated
   * map and not send a reference to the old map, thus not allowing the remote
   * logback component to see the latest changes.
   *
   * @throws IllegalArgumentException in case the "key" parameter is null
   */
  public void put(String key, String val) throws IllegalArgumentException {
    if (key == null) {
      throw new IllegalArgumentException("key cannot be null");
    }

    HashMap<String, String> oldMap = copyOnInheritThreadLocal.get();
    Integer lastOp = getAndSetLastOperation(WRITE_OPERATION);

    if(lastOp == null || lastOp.intValue() == READ_OPERATION) {
      HashMap<String, String> newMap = new HashMap<String, String>();
      if (oldMap != null) {
        newMap.putAll(oldMap);
      }
      // the newMap replaces the old one for serialisation's sake
      copyOnInheritThreadLocal.set(newMap);
      newMap.put(key, val);
    } else {
      oldMap.put(key, val);
    }
  }

  /**
   * Get the context identified by the <code>key</code> parameter.
   * <p/>
   * <p/>
   * This method has no side effects.
   */
  public String get(String key) {
    HashMap<String, String> hashMap = copyOnInheritThreadLocal.get();

    if ((hashMap != null) && (key != null)) {
      return hashMap.get(key);
    } else {
      return null;
    }
  }


  /**
   * Remove the the context identified by the <code>key</code> parameter.
   * <p/>
   * <p/>
   * Each time a value is removed, a new instance of the map is created. This is
   * to be certain that the serialization process will operate on the updated
   * map and not send a reference to the old map, thus not allowing the remote
   * logback component to see the latest changes.
   */
  public void remove(String key) {
    if (key == null) {
      return;
    }
    HashMap<String, String> oldMap = copyOnInheritThreadLocal.get();
    if(oldMap == null) return;

    Integer lastOp = getAndSetLastOperation(WRITE_OPERATION);

    if(lastOp == null || lastOp.intValue() == READ_OPERATION) {
      HashMap<String, String> newMap = new HashMap<String, String>();
      newMap.putAll(oldMap);
      // the newMap replaces the old one for serialisation's sake
      copyOnInheritThreadLocal.set(newMap);
      newMap.remove(key);
    } else {
      oldMap.remove(key);
    }
  }

  /**
   * Clear all entries in the MDC.
   */
  public void clear() {
    HashMap<String, String> hashMap = copyOnInheritThreadLocal.get();
    Integer lastOp = getAndSetLastOperation(WRITE_OPERATION);
    copyOnInheritThreadLocal.remove();
  }

  /**
   * Get the current thread's MDC as a map. This method is intended to be used
   * internally.
   */
  public Map<String, String> getPropertyMap() {
    lastOperation.set(READ_OPERATION);
    return copyOnInheritThreadLocal.get();
  }

  /**
   * Return a copy of the current thread's context map. Returned value may be
   * null.
   */
  public Map getCopyOfContextMap() {
    HashMap<String, String> hashMap = copyOnInheritThreadLocal.get();
    if (hashMap == null) {
      return null;
    } else {
      return new HashMap<String, String>(hashMap);
    }
  }

  /**
   * Returns the keys in the MDC as a {@link Set}. The returned value can be
   * null.
   */
  public Set<String> getKeys() {
    lastOperation.set(READ_OPERATION);
    HashMap<String, String> hashMap = copyOnInheritThreadLocal.get();

    if (hashMap != null) {
      return hashMap.keySet();
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public void setContextMap(Map contextMap) {
    HashMap<String, String> oldMap = copyOnInheritThreadLocal.get();

    HashMap<String, String> newMap = new HashMap<String, String>();
    newMap.putAll(contextMap);

    // the newMap replaces the old one for serialisation's sake
    copyOnInheritThreadLocal.set(newMap);

    // hints for the garbage collector
    if (oldMap != null) {
      oldMap.clear();
      oldMap = null;
    }
  }
}
