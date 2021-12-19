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
package ch.qos.logback.classic.turbo;

import java.util.LinkedHashMap;
import java.util.Map;

class LRUMessageCache {

    private static final long serialVersionUID = 1L;
    final int cacheSize;
    private final InternalHashMap cache = new InternalHashMap();
    private final long cacheResetWindow;
    private long lastReset;

    LRUMessageCache(int cacheSize) {
        this(cacheSize, Long.MAX_VALUE);
    }

    LRUMessageCache(int cacheSize, long cacheResetWindow) {
        if (cacheSize < 1) {
            throw new IllegalArgumentException("Cache size cannot be smaller than 1");
        }
        this.cacheSize = cacheSize;
        this.cacheResetWindow = cacheResetWindow;
        this.lastReset = System.currentTimeMillis();
    }

    int getMessageCountAndThenIncrement(String msg) {
        // don't insert null elements
        if (msg == null) {
            return 0;
        }

        int i;
        // LinkedHashMap is not LinkedHashMap. See also LBCLASSIC-255
        synchronized (this) {
            if (cacheResetWindow != Long.MAX_VALUE && lastReset + cacheResetWindow < System.currentTimeMillis()) {
                cache.clear();
                lastReset = System.currentTimeMillis();
            }
            // new message will get 0, existing one will get its value inc by 1
            i = cache.getOrDefault(msg, -1) + 1;
            cache.put(msg, i);
        }
        return i;
    }

    synchronized public void clear() {
        cache.clear();
    }

    // Thread safety is provided due to the fact that this can only be called from the thread safe
    // getMessageCountAndThenIncrement method, no way to access the internal implementation
    private class InternalHashMap extends LinkedHashMap<String, Integer> {

        public InternalHashMap() {
            super((int) (cacheSize * (4.0f / 3)), 0.75f, true);
        }

        protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
            return (size() > cacheSize);
        }

        @Override
        synchronized public void clear() {
            super.clear();
        }
    }
}
