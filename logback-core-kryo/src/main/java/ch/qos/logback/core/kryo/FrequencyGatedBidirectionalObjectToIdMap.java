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

import java.util.LinkedHashMap;
import java.util.Map;

import static ch.qos.logback.core.kryo.KryoCoreConstants.UNREGISTERED_ID;
import static ch.qos.logback.core.kryo.KryoCoreConstants.cappedSerializerCapacity;

/**
 * A {@link BidirectionalObjectToIdMap} that only assigns stable ids to values after they have
 * been seen often enough on the write path.
 *
 * <h2>Motivation</h2>
 * <p>A plain {@link BidirectionalObjectToIdMapDefault} caches every distinct value on first
 * sight. For high-cardinality fields (log messages, MDC values, exception text) that creates a
 * large cache of one-off entries and little compression benefit: the stream still pays for a
 * {@code NEW_ENTRY} header plus a full payload, and the map fills with values that never recur.
 *
 * <p>This implementation <em>gates</em> registration by frequency. A value is not entered into
 * the id map until it has been written more than {@link #MIN_HITS} times. Until then, writers
 * emit a full payload under a sentinel id ({@link KryoCoreConstants#UNREGISTERED_ID}); only
 * on later occurrences is a real id assigned so subsequent writes can emit a compact
 * {@code CACHED_ENTRY} reference.
 *
 * <h2>Write-path protocol</h2>
 * <p>Used by {@link SerializerBase#write} as follows:
 * <ol>
 *   <li>{@link #getIdByValue(Object)} — if a real id exists, emit {@code CACHED_ENTRY} + id
 *       (no payload).</li>
 *   <li>Otherwise {@link #insertNewEntryOnWrite(Object)}:
 *     <ul>
 *       <li>Increments the per-value access count.</li>
 *       <li>If {@code count <= MIN_HITS}: returns {@link KryoCoreConstants#UNREGISTERED_ID}
 *           ({@code -1}) and does <strong>not</strong> store the value in the id map.</li>
 *       <li>If {@code count > MIN_HITS}: assigns the next id, stores the mapping, returns that
 *           id.</li>
 *     </ul>
 *   </li>
 *   <li>In both unregistered and newly registered cases, {@code SerializerBase} still writes
 *       {@code NEW_ENTRY} + the returned id + the full payload via {@code writeNewEntry}.</li>
 * </ol>
 *
 * <p>Thus the first sighting of a value always carries a full payload with id {@code -1}. The
 * second sighting (with default {@code MIN_HITS = 1}) carries a full payload again, but with a
 * real id that is stored for later {@code CACHED_ENTRY} hits.
 *
 * <h2>Read-path protocol</h2>
 * <p>{@link SerializerBase#read} reads the entry flag and id, then:
 * <ul>
 *   <li>For {@code NEW_ENTRY}: deserializes the payload; if the id is not
 *       {@link KryoCoreConstants#UNREGISTERED_ID}, registers it via
 *       {@link #insertNewEntryOnRead(int, Object)}; unregistered ids are not stored.</li>
 *   <li>For {@code CACHED_ENTRY}: looks up the value with {@link #getValueById(int)} (must already
 *       have been registered by a prior {@code NEW_ENTRY} with a real id).</li>
 * </ul>
 *
 * <p>Read-side registration is not frequency-gated: the stream is authoritative. Gating applies
 * only when assigning ids on write.
 *
 * <h2>State</h2>
 * <ul>
 *   <li><strong>Id map</strong> — a {@link BidirectionalObjectToIdMapDefault} of capacity
 *       {@code capacity}, access-order LRU eviction when full.</li>
 *   <li><strong>Access counts</strong> — a separate access-order {@link LinkedHashMap} used only
 *       on the write path to decide when a value graduates to a real id. Initial capacity
 *       {@link #ACCESS_COUNT_CACHE_SIZE}.</li>
 *   <li><strong>Ids</strong> — assigned by the underlying map when a value passes the
 *       frequency gate; evicted ids are reused.</li>
 * </ul>
 *
 * <h2>Thread safety</h2>
 * <p>{@link #recordAccess(Object)} is synchronized. Other methods are not; a given map instance
 * is intended for single-threaded use by one serializer (typical Kryo usage).
 *
 * @param <V> value type (must have correct {@code equals}/{@code hashCode} for map keys)
 * @see BidirectionalObjectToIdMapDefault
 * @see SerializerBase.BiMapType#FREQUENCY_GATED
 * @see KryoCoreConstants#UNREGISTERED_ID
 */
public class FrequencyGatedBidirectionalObjectToIdMap<V> implements BidirectionalObjectToIdMap<V> {

    final int capacity;

    /**
     * Minimum number of write-path sightings before a value is assigned a stable id.
     * With the default of {@code 1}, the second write of the same value receives a real id.
     */
    static int MIN_HITS = 1;

    /** Initial capacity of the write-side access-count map. */
    static final int ACCESS_COUNT_CACHE_SIZE = 512;

    private final BidirectionalObjectToIdMapDefault<V> bidirectionalObjectToIdMap;
    private final Map<V, Integer> accessCount;

    /**
     * @param capacity maximum number of value↔id mappings retained in the underlying id map
     *                 (access-order LRU eviction via {@link BidirectionalObjectToIdMapDefault})
     */
    public FrequencyGatedBidirectionalObjectToIdMap(int capacity) {
        this.capacity = cappedSerializerCapacity(capacity);
        bidirectionalObjectToIdMap = new BidirectionalObjectToIdMapDefault<>(this.capacity);
        accessCount = new LinkedHashMap<>(ACCESS_COUNT_CACHE_SIZE, 0.75f, true);
    }

    /**
     * Records one write-path access for {@code val} and returns the new count.
     *
     * @param val value being written (may be used as a map key)
     * @return access count for {@code val} after this call (at least 1)
     */
    public synchronized int recordAccess(V val) {
        return accessCount.merge(val, 1, Integer::sum);
    }

    /**
     * Returns the stable id for {@code value} if it has already passed the frequency gate and is
     * still in the id map; otherwise {@code null}.
     */
    @Override
    public Integer getIdByValue(V value) {
            return bidirectionalObjectToIdMap.getIdByValue(value);
    }

    /**
     * Called on the write path when {@link #getIdByValue(Object)} returned {@code null}.
     *
     * <p>Increments the access count for {@code value}. If the count is still
     * {@code <= MIN_HITS}, returns {@link KryoCoreConstants#UNREGISTERED_ID} without storing
     * a mapping. Once the count exceeds {@link #MIN_HITS}, assigns the next id, stores the
     * mapping, and returns that id.
     *
     * <p>In both cases the caller is expected to write a full payload (the id alone is not
     * enough for the reader until a real id has been seen).
     *
     * @param value value being serialized
     * @return a newly assigned positive id, or {@link KryoCoreConstants#UNREGISTERED_ID}
     *         if the value has not yet graduated
     */
    @Override
    public Integer insertNewEntryOnWrite(V value) {
        int count = recordAccess(value);
        if (count > MIN_HITS) {
            return bidirectionalObjectToIdMap.insertNewEntryOnWrite(value);
        }
        return UNREGISTERED_ID;
    }

    /**
     * Registers a mapping on the read path when a {@code NEW_ENTRY} carries a real id.
     * Not frequency-gated; delegates to the underlying id map.
     *
     * @param id    id from the stream (must not be {@link KryoCoreConstants#UNREGISTERED_ID}
     *              when called from {@link SerializerBase#read})
     * @param value deserialized value
     * @return previous value for {@code id}, or {@code null} if none
     */
    @Override
    public V insertNewEntryOnRead(int id, V value) {
        return bidirectionalObjectToIdMap.insertNewEntryOnRead(id, value);
    }

    /**
     * Looks up a value by id on the read path for {@code CACHED_ENTRY} records.
     *
     * @return the value, or {@code null} if the id is unknown or was LRU-evicted
     */
    @Override
    public V getValueById(int id) {
        return bidirectionalObjectToIdMap.getValueById(id);
    }

    /** Underlying value↔id map (real ids only). */
    public BidirectionalObjectToIdMapDefault<V> getBidirectionalObjectToIdMap() {
        return bidirectionalObjectToIdMap;
    }

    /**
     * Write-side access counts used for frequency gating.
     * Not used for id assignment after a value has graduated.
     */
    public Map<V, Integer> getAccessCountMap() {
        return accessCount;
    }
}
