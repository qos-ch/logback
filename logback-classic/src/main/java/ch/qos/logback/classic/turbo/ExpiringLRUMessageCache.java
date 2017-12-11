/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 * <p>
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 * <p>
 * or (per the licensee's choosing)
 * <p>
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.turbo;

import ch.qos.logback.core.util.Duration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Clients of this class should only use the {@link #getMessageCountAndThenIncrement} method. Other methods inherited
 * via LinkedHashMap are not thread safe.<p>
 * Expiration of entries does not shrink the map.
 */
class ExpiringLRUMessageCache extends LinkedHashMap<String, ExpiringLRUMessageCache.Hit> {

    private static final long serialVersionUID = 1L;
    final int cacheSize;
    Duration expirationTime;

    private long artificialCurrentTime = -1;

    ExpiringLRUMessageCache(int cacheSize, Duration expirationTime) {
        super((int) (cacheSize * (4.0f / 3)), 0.75f, true);
        if (cacheSize < 1) {
            throw new IllegalArgumentException("Cache size cannot be smaller than 1");
        }
        if (expirationTime.getMilliseconds() < 1) {
            throw new IllegalArgumentException("Expiration time cannot be smaller than 1");
        }
        this.cacheSize = cacheSize;
        this.expirationTime = expirationTime;
    }

    int getMessageCountAndThenIncrement(String msg) {
        // don't insert null elements
        if (msg == null) {
            return 0;
        }

        Hit hit;
        // LinkedHashMap is not LinkedHashMap. See also LBCLASSIC-255
        synchronized (this) {
            hit = super.get(msg);
            if (hit == null || hit.lastAccessTime + expirationTime.getMilliseconds() < getCurrentTime()) {
                hit = new Hit();
            } else {
                hit = hit.getAndIncrement();
            }
            super.put(msg, hit);
        }
        return hit.count;
    }

    // called indirectly by get() or put() which are already supposed to be
    // called from within a synchronized block
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return (size() > cacheSize);
    }

    @Override
    synchronized public void clear() {
        super.clear();
    }

    long getCurrentTime() {
        // if time is forced return the time set by user
        if (artificialCurrentTime >= 0) {
            return artificialCurrentTime;
        } else {
            return System.currentTimeMillis();
        }
    }

    /**
     * Manipulate used time in Tests.
     */
    void setCurrentTime(long artificialCurrentTime) {
        this.artificialCurrentTime = artificialCurrentTime;
    }

    final class Hit {
        final int count;
        final long lastAccessTime = getCurrentTime();

        public Hit() {
            this(0);
        }

        public Hit(int count) {
            this.count = count;
        }

        Hit getAndIncrement() {
            return new Hit(count + 1);
        }
    }
}
