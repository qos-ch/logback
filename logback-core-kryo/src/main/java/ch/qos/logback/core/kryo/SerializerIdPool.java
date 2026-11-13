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

/**
 * Assigns serializer ids in {@code 1..MAX_SERIALIZER_ID} and reuses ids freed by LRU
 * eviction. Recycle/allocate run only on NEW inserts, not cache hits.
 */
interface SerializerIdPool {

    Integer allocate();

    boolean hasFree();

    void recycle(Integer id);

    /**
     * The wire is taking {@code id} (read path). It must not be handed out again
     * until a later {@link #recycle}.
     */
    void unfree(Integer id);
}
