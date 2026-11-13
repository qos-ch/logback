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

import java.util.HashSet;
import java.util.Iterator;

import static ch.qos.logback.core.kryo.KryoCoreConstants.MAX_SERIALIZER_ID;

/**
 * {@link SerializerIdPool} backed only by a {@link HashSet}. Recycle, allocate, and
 * {@link #unfree} are all O(1); which free id is returned is unspecified.
 */
final class SetSerializerIdPool implements SerializerIdPool {

    private final HashSet<Integer> free = new HashSet<>();
    private int nextId = 1;

    @Override
    public Integer allocate() {
        if (!free.isEmpty()) {
            Iterator<Integer> it = free.iterator();
            Integer id = it.next();
            it.remove();
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
        if (id != null) {
            free.add(id);
        }
    }

    @Override
    public void unfree(Integer id) {
        if (id != null) {
            free.remove(id);
        }
    }
}
