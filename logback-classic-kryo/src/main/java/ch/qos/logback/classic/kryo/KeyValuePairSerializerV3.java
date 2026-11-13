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

import ch.qos.logback.core.kryo.SerializerBase;
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import org.slf4j.event.KeyValuePair;

/**
 * V3 serializer for a single {@link KeyValuePair}.
 *
 * <p>Keys and string values share a {@link SingleItemCompressingStringSerializer} (typically stream-scoped).
 * Values are encoded via {@link SafeObjectCodec} after {@link SafeObjectTransformer}
 * (1-byte type tag + compact payload; never {@code writeClassAndObject}).
 *
 * <p>Whole pairs are not cached ({@link BiMapType#NONE}).
 *
 * @see SafeObjectCodec
 */
public class KeyValuePairSerializerV3 extends SerializerBase<KeyValuePair> {

    private final SingleItemCompressingStringSerializer stringSerializer;

    public KeyValuePairSerializerV3() {
        this(1024);
    }

    public KeyValuePairSerializerV3(int stringCapacity) {
        this(new SingleItemCompressingStringSerializer(stringCapacity));
    }

    /**
     * @param stringSerializer shared cache for keys and string values (must not be null)
     */
    public KeyValuePairSerializerV3(SingleItemCompressingStringSerializer stringSerializer) {
        super(BiMapType.NONE, 0);
        if (stringSerializer == null) {
            throw new IllegalArgumentException("stringSerializer cannot be null");
        }
        this.stringSerializer = stringSerializer;
        setAcceptsNull(false);
    }

    public SingleItemCompressingStringSerializer getStringSerializer() {
        return stringSerializer;
    }

    @Override
    protected void writeNewEntry(Kryo kryo, Output output, KeyValuePair keyValuePair) {
        kryo.writeObject(output, keyValuePair.key, stringSerializer);
        SafeObjectCodec.write(kryo, output, stringSerializer, keyValuePair.value);
    }

    @Override
    protected KeyValuePair readNewEntry(Kryo kryo, Input input) {
        String key = kryo.readObject(input, String.class, stringSerializer);
        Object value = SafeObjectCodec.read(kryo, input, stringSerializer);
        return new KeyValuePair(key, value);
    }
}
