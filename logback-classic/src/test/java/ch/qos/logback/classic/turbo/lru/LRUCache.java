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
package ch.qos.logback.classic.turbo.lru;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An lru cache based on Java's LinkedHashMap.
 * 
 * @author Ceki Gulcu
 *
 * @param <K>
 * @param <V>
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = -6592964689843698200L;

    final int cacheSize;

    public LRUCache(int cacheSize) {
        super((int) (cacheSize * (4.0f / 3)), 0.75f, true);
        if (cacheSize < 1) {
            throw new IllegalArgumentException("Cache size cannnot be smaller than 1");
        }
        this.cacheSize = cacheSize;
    }

    protected boolean removeEldestEntry(Map.Entry eldest) {
        return (size() > cacheSize);
    }

    List<K> keyList() {
        ArrayList<K> al = new ArrayList<K>();
        al.addAll(keySet());
        return al;
    }
}
