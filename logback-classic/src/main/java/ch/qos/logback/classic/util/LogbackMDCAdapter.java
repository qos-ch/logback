/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareImpl;
import ch.qos.logback.core.util.BatchedFixedIntervalInvocationGate;
import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.InvocationGate;
import org.slf4j.MDC;
import org.slf4j.helpers.ThreadLocalMapOfStacks;
import org.slf4j.spi.MDCAdapter;

import java.util.*;

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

    static final int DEFAULT_LULL_IN_HOURS = 12;
    static final int DEFAULT_BATCH_SIZE = 10;
    static final int NULL_VALUE_CALLER_DATA_DEPTH = 8;
    // Frames of these classes are removed from the top of the caller data. MDC
    // forwards to this adapter, so its frames would otherwise hide the caller.
    static final String[] CALLER_DATA_FQNS_TO_SHAVE = { LogbackMDCAdapter.class.getName(), MDC.class.getName() };
    static final int MAX_NULL_VALUE_KEYS_IN_MSG = 16;

    static final String NULL_VALUE_ON_PUT_MSG = "Null value for MDC key [%1$s] is stored. "
            + "Null values are deprecated; use MDC.remove(\"%1$s\") instead.";
    static final String NULL_VALUES_ON_SET_CONTEXT_MAP_MSG = "Null values for MDC keys %s are stored. "
            + "Null values are deprecated; remove such entries instead.";
    static final String MORE_NULL_VALUE_KEYS_SUFFIX = " (and %d more)";
    static final String NULL_KEY_ON_SET_CONTEXT_MAP_MSG = "Null key in MDC context map is stored.";
    static final String NULL_KEY_AND_VALUES_ON_SET_CONTEXT_MAP_MSG = "Null key in MDC context map is stored. "
            + "Null values for MDC keys %s are also stored. Null values are deprecated; remove such entries instead.";

    private Context context;
    private volatile ContextAwareImpl contextAware;
    // Negative means the system clock. See getCurrentTime().
    long artificialTime = -1;
    private final BatchedFixedIntervalInvocationGate nullValueOnPutWarningGate = new BatchedFixedIntervalInvocationGate(
            DEFAULT_BATCH_SIZE, Duration.buildByHours(DEFAULT_LULL_IN_HOURS));

    private final BatchedFixedIntervalInvocationGate nullKeyOrValueOnSetContextMapGate = new BatchedFixedIntervalInvocationGate(
            DEFAULT_BATCH_SIZE, Duration.buildByHours(DEFAULT_LULL_IN_HOURS));


    public void setContext(Context aContext) {
        if(aContext == null)
            throw new IllegalArgumentException("Context argument cannot be null");

        boolean contextChanged = (this.context != null && this.context != aContext);

        this.context = aContext;
        contextAware = new ContextAwareImpl(aContext, this);
        if(contextChanged) {
            contextAware.addWarn("The context has been already set. Re-setting the context may have unexpected results.");
        }
    }

    void setCurrentTime(long time) {
        artificialTime = time;
    }

    long getCurrentTime() {
        // if time is forced return the time set by user
        if (artificialTime >= 0) {
            return artificialTime;
        } else {
            return System.currentTimeMillis();
        }
    }

    /**
     * Put a context value (the <code>val</code> parameter) as identified with the
     * <code>key</code> parameter into the current thread's context map.
     * <p/>
     * <p/>
     * A null <code>val</code> is still stored but is deprecated. Use
     * {@link #remove(String)} instead. When a context is set, a null
     * <code>val</code> causes a rate-limited warning status to be emitted.
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
        if (val == null) {
            warnOfNullValue(nullValueOnPutWarningGate, key);
        }
        Map<String, String> current = readWriteThreadLocalMap.get();
        if (current == null) {
            current = new HashMap<String, String>();
            readWriteThreadLocalMap.set(current);
        }

        current.put(key, val);
        nullifyReadOnlyThreadLocalMap();
    }

    private void warnOfNullValue(InvocationGate invocationGate, String key) {
        if(contextAware == null) return;

        if (invocationGate.isTooSoon(getCurrentTime())) {
            return;
        }

        contextAware.addWarn(String.format(NULL_VALUE_ON_PUT_MSG, key),
                new CallerDataThrowable(CALLER_DATA_FQNS_TO_SHAVE, NULL_VALUE_CALLER_DATA_DEPTH));
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
    @Override
    public Map<String, String> getCopyOfContextMap() {
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

    @Override
    public void setContextMap(Map<String, String> contextMap) {
        if (contextMap != null) {
            Map<String, String> copy = copyAndReportNulls(contextMap);
            readWriteThreadLocalMap.set(copy);
        } else {
            readWriteThreadLocalMap.set(null);
        }
        nullifyReadOnlyThreadLocalMap();
    }

    /**
     * Copies the map and records null keys and null values in the same pass.
     */
    private Map<String, String> copyAndReportNulls(Map<String, String> contextMap) {
        // same sizing as the HashMap(Map) constructor
        Map<String, String> copy = new HashMap<String, String>((int) (contextMap.size() / 0.75f) + 1);

        boolean hasNullKey = false;
        int nullValueCount = 0;
        List<String> nullValueKeys = null;
        for (Map.Entry<String, String> entry : contextMap.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            copy.put(k, v);
            if (k == null) {
                hasNullKey = true;
            } else if (v == null) {
                if (nullValueKeys == null) {
                    nullValueKeys = new ArrayList<String>();
                }
                if (nullValueCount < MAX_NULL_VALUE_KEYS_IN_MSG) {
                    nullValueKeys.add(k);
                }
                nullValueCount++;
            }
        }

        if (hasNullKey || nullValueCount > 0) {
            reportNullKeysAndValues(hasNullKey, nullValueCount, nullValueKeys);
        }
        return copy;
    }

    /**
     * Emits at most one status per call. A null key is
     * reported as an error, which also lists any keys with null values. An entry
     * with a null key is not reported as a null value.
     */
    private void reportNullKeysAndValues(boolean hasNullKey, int nullValueCount, List<String> nullValueKeys) {
        if (contextAware == null)
            return;

        if (nullKeyOrValueOnSetContextMapGate.isTooSoon(getCurrentTime())) {
            return;
        }

        String keysForDisplay = null;
        if (nullValueKeys != null) {
            keysForDisplay = nullValueKeys.toString();
            if (nullValueCount > MAX_NULL_VALUE_KEYS_IN_MSG) {
                keysForDisplay += String.format(MORE_NULL_VALUE_KEYS_SUFFIX, nullValueCount - MAX_NULL_VALUE_KEYS_IN_MSG);
            }
        }

        CallerDataThrowable callerData = new CallerDataThrowable(CALLER_DATA_FQNS_TO_SHAVE,
                NULL_VALUE_CALLER_DATA_DEPTH);
        if (hasNullKey) {
            String msg = (keysForDisplay == null) ? NULL_KEY_ON_SET_CONTEXT_MAP_MSG
                    : String.format(NULL_KEY_AND_VALUES_ON_SET_CONTEXT_MAP_MSG, keysForDisplay);
            contextAware.addError(msg, callerData);
        } else {
            contextAware.addWarn(String.format(NULL_VALUES_ON_SET_CONTEXT_MAP_MSG, keysForDisplay), callerData);
        }
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
