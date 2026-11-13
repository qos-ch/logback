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

public interface BidirectionalObjectToIdMap<V> {

    /**
     * Returns the id for the given value or null if the value is not mapped.
     */
    Integer getIdByValue(V value);


    /**
     * Inserts a new entry mapping an id to the given value.
     * Reuses an id freed by LRU eviction when the map is at capacity;
     * otherwise assigns the next unused id.
     * Returns the assigned id.
     */
    Integer insertNewEntryOnWrite(V value);


    /**
     * Inserts a new entry mapping the given id to the given value.
     *
     * <p>Any existing entry with the same value if removed from the valueToId map.</p>
     *
     * Returns the existing value mapped to the given id or null if there was no existing mapping.
     */
    V insertNewEntryOnRead(int id, V value);


    /**
     * Returns the value mapped to the given id or null if there is no such mapping.
     */
    public V getValueById(int id);
}
