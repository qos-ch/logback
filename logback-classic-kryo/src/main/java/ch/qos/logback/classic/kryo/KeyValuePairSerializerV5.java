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

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import org.slf4j.event.KeyValuePair;

/**
 * V5 serializer for a single {@link KeyValuePair}.
 *
 * <p>Keys use {@link BasicStringSerializer} (short repeating names, id cache).
 * String values use the caller-supplied codec, typically the same
 * {@link MultiEventCompressedStringSerializer} as the event message so there is one deflate stream.
 */
public class KeyValuePairSerializerV5 extends Serializer<KeyValuePair> {

    private final Serializer<String> keySerializer;
    private final Serializer<String> valueStringSerializer;

    public KeyValuePairSerializerV5(int stringCapacity) {
        this(new BasicStringSerializer(stringCapacity), new MultiEventCompressedStringSerializer(stringCapacity));
    }

    public KeyValuePairSerializerV5(Serializer<String> keySerializer,
                                    Serializer<String> valueStringSerializer) {
        if (keySerializer == null || valueStringSerializer == null) {
            throw new IllegalArgumentException("key and value string serializers cannot be null");
        }
        this.keySerializer = keySerializer;
        this.valueStringSerializer = valueStringSerializer;
        setAcceptsNull(false);
    }

    @Override
    public void write(Kryo kryo, Output output, KeyValuePair keyValuePair) {
        kryo.writeObject(output, keyValuePair.key, keySerializer);
        SafeObjectCodec.write(kryo, output, valueStringSerializer, keyValuePair.value);
    }

    @Override
    public KeyValuePair read(Kryo kryo, Input input, Class<? extends KeyValuePair> type) {
        String key = kryo.readObject(input, String.class, keySerializer);
        Object value = SafeObjectCodec.read(kryo, input, valueStringSerializer);
        return new KeyValuePair(key, value);
    }
}
