/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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

import org.slf4j.helpers.ThreadLocalMapOfStacks;
import org.slf4j.spi.MDCAdapter;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
 * @author Michael Franz
 */
public class LogbackMDCAdapter implements MDCAdapter  {


    // BEWARE: Keys or values placed in a ThreadLocal should not be of a type/class
    // not included in the JDK. See also https://jira.qos.ch/browse/LOGBACK-450

    final ThreadLocal<Map<String, String>> readWriteThreadLocalMap = new ThreadLocal<Map<String, String>>();
    final ThreadLocal<Map<String, String>> readOnlyThreadLocalMap = new ThreadLocal<Map<String, String>>();
    private final ThreadLocalMapOfStacks threadLocalMapOfDeques = new ThreadLocalMapOfStacks();

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
        Map<String, String> current = readWriteThreadLocalMap.get();
        if (current == null) {
            current = new HashMap<String, String>();
            readWriteThreadLocalMap.set(current);
        }

        current.put(key, val);
        nullifyReadOnlyThreadLocalMap();
    }

    /**
     * Get the context identified by the <code>key</code> parameter.
     * <p/>
     * <p/>
     * This method has no side effects.
     */
    @Override
    public String get(String key) {
        Map<String, String> hashMap = readWriteThreadLocalMap.get();

        if ((hashMap != null) && (key != null)) {
            return hashMap.get(key);
        } else {
            return null;
        }
    }

    /**
     * <p>Remove the context identified by the <code>key</code> parameter.
     * <p/>
     */
    @Override
    public void remove(String key) {
        if (key == null) {
            return;
        }

        Map<String, String> current = readWriteThreadLocalMap.get();
        if (current != null) {
            current.remove(key);
            nullifyReadOnlyThreadLocalMap();
        }
    }

    private void nullifyReadOnlyThreadLocalMap() {
        readOnlyThreadLocalMap.set(null);
    }

    /**
     * Clear all entries in the MDC.
     */
    @Override
    public void clear() {
        readWriteThreadLocalMap.set(null);
        nullifyReadOnlyThreadLocalMap();
    }

    /**
     * <p>Get the current thread's MDC as a map. This method is intended to be used
     * internally.</p>
     *
     * The returned map is unmodifiable (since version 1.3.2/1.4.2).
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getPropertyMap() {
        Map<String, String> readOnlyMap = readOnlyThreadLocalMap.get();
        if (readOnlyMap == null) {
            Map<String, String> current = readWriteThreadLocalMap.get();
            if (current != null) {
                final Map<String, String> tempMap = new HashMap<String, String>(current);
                readOnlyMap = Collections.unmodifiableMap(tempMap);
                readOnlyThreadLocalMap.set(readOnlyMap);
            }
        }
        return readOnlyMap;
    }

    /**
     * Return a copy of the current thread's context map. Returned value may be
     * null.
     */
    public Map getCopyOfContextMap() {
        Map<String, String> readOnlyMap = getPropertyMap();
        if (readOnlyMap == null) {
            return null;
        } else {
            return new HashMap<String, String>(readOnlyMap);
        }
    }

    /**
     * Returns the keys in the MDC as a {@link Set}. The returned value can be
     * null.
     */
    public Set<String> getKeys() {
        Map<String, String> readOnlyMap = getPropertyMap();

        if (readOnlyMap != null) {
            return readOnlyMap.keySet();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public void setContextMap(Map contextMap) {
        if (contextMap != null) {
            readWriteThreadLocalMap.set(new HashMap<String, String>(contextMap));
        } else {
            readWriteThreadLocalMap.set(null);
        }
        nullifyReadOnlyThreadLocalMap();
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
