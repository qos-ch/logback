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
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.helpers.ThreadLocalMapOfStacks;
import org.slf4j.spi.MDCAdapter;

/**
 * A <em>Mapped Diagnostic Context</em>, or MDC in short, is an instrument for
 * distinguishing interleaved log output from different sources. Log output is
 * typically interleaved when a server handles multiple clients
 * near-simultaneously.
 * <p/>
 * <b><em>The MDC is managed on a per thread basis</em></b>. Note that a child
 * thread <b>does not</b> inherit the mapped diagnostic context of its parent.
 * <p/>
 * <p/>
 * For more information about MDC, please refer to the online manual at
 * http://logback.qos.ch/manual/mdc.html
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class LogbackMDCAdapterSimple implements MDCAdapter {


    // We wish to avoid unnecessarily copying of the map. To ensure
    // efficient/timely copying, we have a variable keeping track of the previous
    // operation. A copy is necessary on 'put' or 'remove' but only if the last
    // operation was a 'get'. Get operations never necessitate a copy nor
    // successive 'put/remove' operations, only a get followed by a 'put/remove'
    // requires copying the map.
    // See http://jira.qos.ch/browse/LOGBACK-620 for the original discussion.

    // We no longer use CopyOnInheritThreadLocal in order to solve LBCLASSIC-183
    // Initially the contents of the thread local in parent and child threads
    // reference the same map. However, as soon as a thread invokes the put()
    // method, the maps diverge as they should.
//    final ThreadLocal<Map<String, String>> modifiableMap = new ThreadLocal<Map<String, String>>();

    final ThreadLocal<Map<String, String>> threadLocalUnmodifiableMap = new ThreadLocal<Map<String, String>>();

//    private static final int WRITE_OPERATION = 1;
//    private static final int MAP_COPY_OPERATION = 2;

    // keeps track of the previous operation performed
//    final ThreadLocal<Integer> previousOperation = new ThreadLocal<Integer>();

    private final ThreadLocalMapOfStacks threadLocalMapOfDeques = new ThreadLocalMapOfStacks();
    
//    private Integer getAndSetPreviousOperation(int op) {
//        Integer penultimateOp = previousOperation.get();
//        previousOperation.set(op);
//        return penultimateOp;
//    }

//    private boolean wasPreviousOpReadOrNull(Integer lastOp) {
//        return lastOp == null || lastOp.intValue() == MAP_COPY_OPERATION;
//    }

//    private Map<String, String> duplicateAndInsertNewMap(Map<String, String> oldMap) {
//        Map<String, String> newMap = duplicateOldMap(oldMap);
//        modifiableMap.set(newMap);
//        return newMap;
//    }

    private Map<String, String> duplicateMap(Map<String, String> oldMap) {
        if(oldMap != null)
            return new HashMap<>(oldMap);
        else
            return new HashMap<>();
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

        Map<String, String> oldMap = threadLocalUnmodifiableMap.get();
        Map<String, String> newMap = duplicateMap(oldMap);
        newMap.put(key, val);
        makeUnmodifiableAndThreadLocalSet(newMap);
    }

    private void makeUnmodifiableAndThreadLocalSet(Map<String, String> aMap) {
        Map<String, String> unmodifiable = Collections.unmodifiableMap(aMap);
        threadLocalUnmodifiableMap.set(unmodifiable);
    }

    /**
     * Remove the context identified by the <code>key</code> parameter.
     * <p/>
     */
    public void remove(String key) {
        if (key == null) {
            return;
        }
        Map<String, String> oldMap = threadLocalUnmodifiableMap.get();
        if (oldMap == null)
            return;

        Map<String, String> newMap = duplicateMap(oldMap);
        newMap.remove(key);
        makeUnmodifiableAndThreadLocalSet(newMap);
    }

    /**
     * Clear all entries in the MDC.
     */
    public void clear() {
        threadLocalUnmodifiableMap.remove();
    }

    /**
     * Get the context identified by the <code>key</code> parameter.
     * <p/>
     */
    public String get(String key) {
        final Map<String, String> map = threadLocalUnmodifiableMap.get();
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
        return threadLocalUnmodifiableMap.get();
    }

    /**
     * Returns the keys in the MDC as a {@link Set}. The returned value can be null.
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
    public Map<String, String> getCopyOfContextMap() {
        Map<String, String> hashMap = threadLocalUnmodifiableMap.get();
        return duplicateMap(hashMap);
    }

    /**
     * Set the MDC map to the map passed as parameter.
     *
     * @param contextMap the new map
     */
    public void setContextMap(Map<String, String> contextMap) {
        duplicateMap(contextMap);
    }
    
    @Override
    public void pushByKey(String key, String value) {
        threadLocalMapOfDeques.pushByKey(key, value);
    }

    @Override
    public String popByKey(String key) {
        return threadLocalMapOfDeques.popByKey(key);    
     }

    @Override
    public Deque<String> getCopyOfDequeByKey(String key) {
        return threadLocalMapOfDeques.getCopyOfDequeByKey(key);
    }
    @Override
    public void clearDequeByKey(String key) {
        threadLocalMapOfDeques.clearDequeByKey(key);
    }
}
