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
 * V5 MDC map: whole-map cache is {@link BiMapType#IDENTITY}. Keys stay on {@link BasicStringSerializer}.
 * Values use the caller-supplied codec, the same
 * {@link MultiEventCompressedStringSerializer} as message, string arguments, and
 * KVP string values.
 */
public class MDCPropertyMapSerializerV5 extends MDCPropertyMapSerializer {

    public MDCPropertyMapSerializerV5(Serializer<String> valueSerializer) {
        this(1024, valueSerializer);
    }

    public MDCPropertyMapSerializerV5(int capacity, Serializer<String> valueSerializer) {
        super(BiMapType.IDENTITY, capacity, new BasicStringSerializer(capacity), requireValueSerializer(valueSerializer));
    }

    private static Serializer<String> requireValueSerializer(Serializer<String> valueSerializer) {
        if (valueSerializer == null) {
            throw new IllegalArgumentException("valueSerializer cannot be null");
        }
        return valueSerializer;
    }
}
