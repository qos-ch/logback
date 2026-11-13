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

import static ch.qos.logback.core.kryo.KryoCoreConstants.MAX_SERIALIZER_ID;

/**
 * {@link SerializerIdPool} backed only by an {@link ArrayDeque} used as a LIFO stack.
 * Recycle/allocate are O(1); {@link #unfree} is O(n) because it must scan the deque.
 */
final class DequeSerializerIdPool implements SerializerIdPool {

    private final ArrayDeque<Integer> free = new ArrayDeque<>();
    private int nextId = 1;

    @Override
    public Integer allocate() {
        Integer id = free.pollLast();
        if (id != null) {
            return id;
        }
        if (nextId > MAX_SERIALIZER_ID) {
            throw new IllegalStateException(
                    "Serializer id exhausted (allowed 1.." + MAX_SERIALIZER_ID + ")");
        }
        return BoxedIds.of(nextId++);
    }

    @Override
    public boolean hasFree() {
        return !free.isEmpty();
    }

    @Override
    public void recycle(Integer id) {
        if (id == null) {
            return;
        }
        if (!free.contains(id)) {
            free.addLast(id);
        }
    }

    @Override
    public void unfree(Integer id) {
        if (id != null) {
            free.remove(id);
        }
    }
}
