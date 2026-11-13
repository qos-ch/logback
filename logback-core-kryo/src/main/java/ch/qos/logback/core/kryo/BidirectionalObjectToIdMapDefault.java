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
 * Value↔id map with access-order LRU eviction at {@code capacity}.
 *
 * <p>{@link #getIdByValue} and {@link #getValueById} count as uses, so entries that
 * keep being looked up stay; the least-recently-used mapping is dropped when a new
 * one would exceed capacity. Evicted ids are reused for later inserts. {@code idToValue}
 * is a plain {@link HashMap}; LRU order lives only in {@code valueToId}.
 */
public class BidirectionalObjectToIdMapDefault<V> implements BidirectionalObjectToIdMap<V> {

    static final boolean ACCESS_ORDER = true;

    final int capacity;

    private final Map<Integer, V> idToValue;
    private final LinkedHashMap<V, Integer> valueToId;
    private final SerializerIdPool idPool = new DualSerializerIdPool();

    public BidirectionalObjectToIdMapDefault(int capacity) {
        this.capacity = cappedSerializerCapacity(capacity);
        this.idToValue = new HashMap<>(this.capacity);
        this.valueToId = new LinkedHashMap<>(this.capacity, 0.75f, ACCESS_ORDER) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<V, Integer> eldest) {
                if (size() > BidirectionalObjectToIdMapDefault.this.capacity) {
                    recycle(eldest.getValue());
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public Integer getIdByValue(V value) {
        return valueToId.get(value);
    }

    @Override
    public Integer insertNewEntryOnWrite(V value) {
        ensureIdAvailable();
        Integer boxed = idPool.allocate();
        V existingValue = idToValue.put(boxed, value);
        if (existingValue != null) {
            valueToId.remove(existingValue);
        }
        valueToId.put(value, boxed);
        return boxed;
    }

    @Override
    public V insertNewEntryOnRead(int id, V value) {
        Integer boxed = BoxedIds.of(id);
        idPool.unfree(boxed);
        V existingValue = idToValue.put(boxed, value);
        if (existingValue != null) {
            valueToId.remove(existingValue);
        }
        Integer previousId = valueToId.put(value, boxed);
        if (previousId != null && previousId.intValue() != id) {
            recycle(previousId);
        }
        return existingValue;
    }

    @Override
    public V getValueById(int id) {
        V value = idToValue.get(BoxedIds.of(id));
        if (value != null) {
            valueToId.get(value);
        }
        return value;
    }

    /**
     * Makes a free id available before {@link SerializerIdPool#allocate()} on the write path.
     *
     * <p>Ids are assigned <em>before</em> {@code valueToId.put}, so LRU
     * {@code removeEldestEntry} cannot supply the id for this insert. If the pool has
     * nothing free and the map is already at {@link #capacity}, this drops the
     * least-recently-used mapping and {@link #recycle(Integer) recycles} its id.
     * {@code allocate()} then reuses that id instead of minting {@code nextId}, which
     * would eventually exceed the 12-bit header.
     *
     * <p>No-op when a recycled id is already in the pool, or when the map is below
     * capacity (a new id can be minted).
     */
    private void ensureIdAvailable() {
        if (!idPool.hasFree() && valueToId.size() >= capacity && !valueToId.isEmpty()) {
            Map.Entry<V, Integer> eldest = valueToId.entrySet().iterator().next();
            valueToId.remove(eldest.getKey());
            recycle(eldest.getValue());
        }
    }

    private void recycle(Integer id) {
        idToValue.remove(id);
        idPool.recycle(id);
    }

    Map<Integer, V> getIdToValueMap() {
        return idToValue;
    }

    Map<V, Integer> getValueToIdMap() {
        return valueToId;
    }
}
