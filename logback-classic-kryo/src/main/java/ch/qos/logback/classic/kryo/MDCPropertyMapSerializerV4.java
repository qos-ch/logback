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
package ch.qos.logback.classic.kryo;

import com.esotericsoftware.kryo.kryo5.Serializer;

/**
 * V4 MDC map, used by {@link LoggingEventKryoVOSerializerV4}.
 *
 * <p>The whole-map cache is {@link BiMapType#IDENTITY}: a map is written by reference
 * only when it is the same instance ({@code ==}) as one already written. The MDC adapter
 * returns the same map instance as long as the MDC is not modified, so consecutive events
 * of a thread usually hit the cache without the cost of {@code equals}/{@code hashCode}.
 * Equal maps held by different instances are written in full.
 *
 * <p>Keys and values share one {@link BasicStringSerializer} with a
 * {@link BiMapType#DIRECT_MAP} cache of the same capacity.
 *
 * <p>The wire format is described in {@link MDCPropertyMapSerializer}. It must not change,
 * since V4 streams already written must remain readable.
 */
public class MDCPropertyMapSerializerV4 extends MDCPropertyMapSerializer {

    public MDCPropertyMapSerializerV4(int capacity) {
        super(BiMapType.IDENTITY, capacity);
    }

    @Override
    protected Serializer<String> getNewEntrySerializer(int capacity) {
        return new BasicStringSerializer(capacity);
    }
}
