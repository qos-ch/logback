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
package ch.qos.logback.classic.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
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
public final class LogbackMDCAdapter implements MDCAdapter {

  // We wish to avoid unnecessarily copying of the map. To ensure
  // efficient/timely copying, we have a variable keeping track of the last
  // operation. A copy is necessary on 'put' or 'remove' but only if the last
  // operation was a 'get'. Get operations never necessitate a copy nor
  // successive 'put/remove' operations, only a get followed by a 'put/remove'
  // requires copying the map.
  // See http://jira.qos.ch/browse/LBCLASSIC-254 for the original discussion.

  // We no longer use CopyOnInheritThreadLocal in order to solve LBCLASSIC-183
  // Initially the contents of the thread local in parent and child threads
  // reference the same map. However, as soon as a thread invokes the put()
  // method, the maps diverge as they should.
  final InheritableThreadLocal<Map<String, String>> copyOnInheritThreadLocal = new InheritableThreadLocal<Map<String, String>>();

  private static final int WRITE_OPERATION = 1;
  private static final int READ_OPERATION = 2;

  // keeps track of the last operation performed
  final ThreadLocal<Integer> lastOperation = new ThreadLocal<Integer>();

  private Integer getAndSetLastOperation(int op) {
    Integer lastOp = lastOperation.get();
    lastOperation.set(op);
    return lastOp;
  }

  private boolean wasLastOpReadOrNull(Integer lastOp) {
    return lastOp == null || lastOp.intValue() == READ_OPERATION;
  }

  private Map<String, String> duplicateAndInsertNewMap(Map<String, String> oldMap) {
    Map<String, String> newMap = Collections.synchronizedMap(new HashMap<String, String>());
    if (oldMap != null) {
        // we don't want the parent thread modifying oldMap while we are
        // iterating over it
        synchronized (oldMap) {
          newMap.putAll(oldMap);
        }
    }

    copyOnInheritThreadLocal.set(newMap);
    return newMap;
  }

  /**
   * Put a context value (the <code>val</code> parameter) as identified with the
   * <code>key</code> parameter into the current thread's context map. Note that
   * contrary to log4j, the <code>val</code> parameter can be null.
   * <p/>
   * <p/>
   * If the current thread does not have a context map it is created as a side
   * effect of this call.
   *
   * @throws IllegalArgumentException in case the "key" parameter is null
   */
  public void put(String key, String val) throws IllegalArgumentException {
    if (key == null) {
      throw new IllegalArgumentException("key cannot be null");
    }

    Map<String, String> oldMap = copyOnInheritThreadLocal.get();
    Integer lastOp = getAndSetLastOperation(WRITE_OPERATION);

    if (wasLastOpReadOrNull(lastOp) || oldMap == null) {
      Map<String, String> newMap = duplicateAndInsertNewMap(oldMap);
      newMap.put(key, val);
    } else {
      oldMap.put(key, val);
    }
  }

  /**
   * Remove the the context identified by the <code>key</code> parameter.
   * <p/>
   */
  public void remove(String key) {
    if (key == null) {
      return;
    }
    Map<String, String> oldMap = copyOnInheritThreadLocal.get();
    if (oldMap == null) return;

    Integer lastOp = getAndSetLastOperation(WRITE_OPERATION);

    if (wasLastOpReadOrNull(lastOp)) {
      Map<String, String> newMap = duplicateAndInsertNewMap(oldMap);
      newMap.remove(key);
    } else {
      oldMap.remove(key);
    }
  }


  /**
   * Clear all entries in the MDC.
   */
  public void clear() {
    lastOperation.set(WRITE_OPERATION);
    copyOnInheritThreadLocal.remove();
  }

  /**
   * Get the context identified by the <code>key</code> parameter.
   * <p/>
   */
  public String get(String key) {
    Map<String, String> map = getPropertyMap();
    if ((map != null) && (key != null)) {
      return map.get(key);
    } else {
      return null;
    }
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
   * Returns the keys in the MDC as a {@link Set}. The returned value can be
   * null.
   */
  public Set<String> getKeys() {
    Map<String, String> map = getPropertyMap();

    if (map != null) {
      return map.keySet();
    } else {
      return null;
    }
  }

  /**
   * Return a copy of the current thread's context map. Returned value may be
   * null.
   */
  public Map getCopyOfContextMap() {
    lastOperation.set(READ_OPERATION);
    Map<String, String> hashMap = copyOnInheritThreadLocal.get();
    if (hashMap == null) {
      return null;
    } else {
      return new HashMap<String, String>(hashMap);
    }
  }

  @SuppressWarnings("unchecked")
  public void setContextMap(Map contextMap) {
    lastOperation.set(WRITE_OPERATION);

    Map<String, String> newMap = Collections.synchronizedMap(new HashMap<String, String>());
    newMap.putAll(contextMap);

    // the newMap replaces the old one for serialisation's sake
    copyOnInheritThreadLocal.set(newMap);
  }
}
