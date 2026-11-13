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
package ch.qos.logback.core.kryo;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static ch.qos.logback.core.kryo.KryoCoreConstants.cappedSerializerCapacity;

/**
 * Value↔id map keyed by object identity ({@code ==} / {@link System#identityHashCode}),
 * with access-order LRU eviction at {@code capacity}.
 *
 * <p>Equal-but-distinct instances are different keys. {@link #getIdByValue} and
 * {@link #getValueById} count as uses. Evicted ids are reused for later inserts.
 * {@code idToValue} is a plain {@link HashMap}; LRU order lives only in {@code valueToId}.
 *
 * @see SerializerBase.BiMapType#IDENTITY
 */
public class BidirectionalObjectToIdMapIdentity<V> implements BidirectionalObjectToIdMap<V> {

    static final boolean ACCESS_ORDER = true;

    final int capacity;

    private final Map<Integer, V> idToValue;
    private final LinkedHashMap<IdentityKey<V>, Integer> valueToId;
    /** Reused for lookups so {@link #getIdByValue} / {@link #getValueById} do not allocate. */
    private final IdentityKey<V> lookupKey = new IdentityKey<>(null);
    private final SerializerIdPool idPool = new DualSerializerIdPool();

    public BidirectionalObjectToIdMapIdentity(int capacity) {
        this.capacity = cappedSerializerCapacity(capacity);
        this.idToValue = new HashMap<>(this.capacity);
        this.valueToId = new LinkedHashMap<>(this.capacity, 0.75f, ACCESS_ORDER) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<IdentityKey<V>, Integer> eldest) {
                if (size() > BidirectionalObjectToIdMapIdentity.this.capacity) {
                    recycle(eldest.getValue());
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public Integer getIdByValue(V value) {
        lookupKey.ref = value;
        Integer id = valueToId.get(lookupKey);
        lookupKey.ref = null;
        return id;
    }

    @Override
    public Integer insertNewEntryOnWrite(V value) {
        ensureIdAvailable();
        Integer boxed = idPool.allocate();
        V existingValue = idToValue.put(boxed, value);
        if (existingValue != null) {
            valueToId.remove(IdentityKey.of(existingValue));
        }
        valueToId.put(IdentityKey.of(value), boxed);
        return boxed;
    }

    @Override
    public V insertNewEntryOnRead(int id, V value) {
        Integer boxed = BoxedIds.of(id);
        idPool.unfree(boxed);
        V existingValue = idToValue.put(boxed, value);
        if (existingValue != null) {
            valueToId.remove(IdentityKey.of(existingValue));
        }
        Integer previousId = valueToId.put(IdentityKey.of(value), boxed);
        if (previousId != null && previousId.intValue() != id) {
            recycle(previousId);
        }
        return existingValue;
    }

    @Override
    public V getValueById(int id) {
        V value = idToValue.get(BoxedIds.of(id));
        if (value != null) {
            lookupKey.ref = value;
            valueToId.get(lookupKey);
            lookupKey.ref = null;
        }
        return value;
    }

    private void ensureIdAvailable() {
        if (!idPool.hasFree() && valueToId.size() >= capacity && !valueToId.isEmpty()) {
            Map.Entry<IdentityKey<V>, Integer> eldest = valueToId.entrySet().iterator().next();
            valueToId.remove(eldest.getKey());
            recycle(eldest.getValue());
        }
    }

    private void recycle(Integer id) {
        idToValue.remove(id);
        idPool.recycle(id);
    }

    static final class IdentityKey<V> {
        V ref;

        static <V> IdentityKey<V> of(V ref) {
            return new IdentityKey<>(ref);
        }

        IdentityKey(V ref) {
            this.ref = ref;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(ref);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof IdentityKey && ((IdentityKey<?>) o).ref == ref;
        }
    }
}
