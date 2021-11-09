/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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

import java.util.Collections;
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
 * <b><em>The MDC is managed on a per thread basis</em></b>. Note that a child thread
 * <b>does not</b> inherit the mapped diagnostic context of its parent.
 * <p/>
 * <p/>
 * For more information about MDC, please refer to the online manual at
 * http://logback.qos.ch/manual/mdc.html
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class LogbackMDCAdapter implements MDCAdapter {

    // The internal map is copied so as

    // We wish to avoid unnecessarily copying of the map. To ensure
    // efficient/timely copying, we have a variable keeping track of the last
    // operation. A copy is necessary on 'put' or 'remove' but only if the last
    // operation was a 'get'. Get operations never necessitate a copy nor
    // successive 'put/remove' operations, only a get followed by a 'put/remove'
    // requires copying the map.
    // See http://jira.qos.ch/browse/LOGBACK-620 for the original discussion.

    // We no longer use CopyOnInheritThreadLocal in order to solve LBCLASSIC-183
    // Initially the contents of the thread local in parent and child threads
    // reference the same map. However, as soon as a thread invokes the put()
    // method, the maps diverge as they should.
    final ThreadLocal<Map<String, String>> copyOnThreadLocal = new ThreadLocal<>();

    private static final int WRITE_OPERATION = 1;
    private static final int MAP_COPY_OPERATION = 2;

    // keeps track of the last operation performed
    final ThreadLocal<Integer> lastOperation = new ThreadLocal<>();

    private Integer getAndSetLastOperation(final int op) {
        final Integer lastOp = lastOperation.get();
        lastOperation.set(op);
        return lastOp;
    }

    private boolean wasLastOpReadOrNull(final Integer lastOp) {
        return lastOp == null || lastOp.intValue() == MAP_COPY_OPERATION;
    }

    private Map<String, String> duplicateAndInsertNewMap(final Map<String, String> oldMap) {
        final Map<String, String> newMap = Collections.synchronizedMap(new HashMap<String, String>());
        if (oldMap != null) {
            // we don't want the parent thread modifying oldMap while we are
            // iterating over it
            synchronized (oldMap) {
                newMap.putAll(oldMap);
            }
        }

        copyOnThreadLocal.set(newMap);
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
    @Override
    public void put(final String key, final String val) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }

        final Map<String, String> oldMap = copyOnThreadLocal.get();
        final Integer lastOp = getAndSetLastOperation(WRITE_OPERATION);

        if (wasLastOpReadOrNull(lastOp) || oldMap == null) {
            final Map<String, String> newMap = duplicateAndInsertNewMap(oldMap);
            newMap.put(key, val);
        } else {
            oldMap.put(key, val);
        }
    }

    /**
     * Remove the the context identified by the <code>key</code> parameter.
     * <p/>
     */
    @Override
    public void remove(final String key) {
        if (key == null) {
            return;
        }
        final Map<String, String> oldMap = copyOnThreadLocal.get();
        if (oldMap == null) {
            return;
        }

        final Integer lastOp = getAndSetLastOperation(WRITE_OPERATION);

        if (wasLastOpReadOrNull(lastOp)) {
            final Map<String, String> newMap = duplicateAndInsertNewMap(oldMap);
            newMap.remove(key);
        } else {
            oldMap.remove(key);
        }
    }

    /**
     * Clear all entries in the MDC.
     */
    @Override
    public void clear() {
        lastOperation.set(WRITE_OPERATION);
        copyOnThreadLocal.remove();
    }

    /**
     * Get the context identified by the <code>key</code> parameter.
     * <p/>
     */
    @Override
    public String get(final String key) {
        final Map<String, String> map = copyOnThreadLocal.get();
        if (map != null && key != null) {
            return map.get(key);
        }
        return null;
    }

    /**
     * Get the current thread's MDC as a map. This method is intended to be used
     * internally.
     */
    public Map<String, String> getPropertyMap() {
        lastOperation.set(MAP_COPY_OPERATION);
        return copyOnThreadLocal.get();
    }

    /**
     * Returns the keys in the MDC as a {@link Set}. The returned value can be
     * null.
     */
    public Set<String> getKeys() {
        final Map<String, String> map = getPropertyMap();

        if (map != null) {
            return map.keySet();
        }
        return null;
    }

    /**
     * Return a copy of the current thread's context map. Returned value may be
     * null.
     */
    @Override
    public Map<String, String> getCopyOfContextMap() {
        final Map<String, String> hashMap = copyOnThreadLocal.get();
        if (hashMap == null) {
            return null;
        }
        return new HashMap<>(hashMap);
    }

    @Override
    public void setContextMap(final Map<String, String> contextMap) {
        lastOperation.set(WRITE_OPERATION);

        final Map<String, String> newMap = Collections.synchronizedMap(new HashMap<String, String>());
        newMap.putAll(contextMap);

        // the newMap replaces the old one for serialisation's sake
        copyOnThreadLocal.set(newMap);
    }
}
