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

import java.util.ArrayDeque;
import java.util.HashSet;

import static ch.qos.logback.core.kryo.KryoCoreConstants.MAX_SERIALIZER_ID;

/**
 * {@link SerializerIdPool} that keeps both an {@link ArrayDeque} (LIFO allocate/recycle)
 * and a {@link HashSet} (O(1) {@link #unfree} without scanning the deque).
 *
 * <p>After {@code unfree}, the deque may still hold that id. {@link #allocate} polls until
 * {@code freeSet.remove} succeeds; the loop is bounded by deque size because each iteration
 * {@code pollLast}s.
 */
final class DualSerializerIdPool implements SerializerIdPool {

    /** LIFO of recycled ids; may contain stale entries that {@link #unfree} already dropped. */
    private final ArrayDeque<Integer> freeArrayDeque = new ArrayDeque<>();

    /**
     * Ids that may be assigned again. Subset of (or equal to) {@link #freeArrayDeque}.
     * {@link #unfree} removes here only, so a read-path reuse does not walk the deque.
     */
    private final HashSet<Integer> freeSet = new HashSet<>();

    private int nextId = 1;

    @Override
    public Integer allocate() {
        Integer id;
        while ((id = freeArrayDeque.pollLast()) != null) {
            if (freeSet.remove(id)) {
                return id;
            }
        }
        if (nextId > MAX_SERIALIZER_ID) {
            throw new IllegalStateException(
                    "Serializer id exhausted (allowed 1.." + MAX_SERIALIZER_ID + ")");
        }
        return BoxedIds.of(nextId++);
    }

    @Override
    public boolean hasFree() {
        return !freeSet.isEmpty();
    }

    @Override
    public void recycle(Integer id) {
        if (id == null) {
            return;
        }
        if (freeSet.add(id)) {
            freeArrayDeque.addLast(id);
        }
    }

    @Override
    public void unfree(Integer id) {
        if (id != null) {
            freeSet.remove(id);
        }
    }
}
