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

/**
 * V5 serializer for message argument arrays ({@code Object[]}).
 *
 * <p>Does not use Kryo's default polymorphic array encoding. Each element is reduced by
 * {@link SafeObjectTransformer} and written via {@link SafeObjectCodec} (1-byte type tag +
 * compact payload). String arguments share a stream-scoped string serializer:
 * {@link LoggingEventKryoVOSerializerV5} passes its {@link MultiEventCompressedStringSerializer},
 * the {@code int} constructor uses a {@link SingleItemCompressingStringSerializer}.
 *
 * <p>Wire format:
 * <pre>
 *   varInt(length)
 *   length × (SafeObjectCodec element)
 * </pre>
 *
 * <p>Null arrays are not handled here; callers should use {@code writeObjectOrNull} /
 * {@code readObjectOrNull}.
 *
 * @see SafeObjectCodec
 * @see LoggingEventKryoVO#safeObjectArray(Object[])
 */
public class ArgumentArraySerializerV5 extends Serializer<Object[]> {

    private final Serializer<String> stringSerializer;

    public ArgumentArraySerializerV5() {
        this(1024);
    }

    /**
     * @param stringCapacity capacity of the string cache for string arguments
     */
    public ArgumentArraySerializerV5(int stringCapacity) {
        this(new SingleItemCompressingStringSerializer(stringCapacity));
    }

    /**
     * @param stringSerializer shared cache for string arguments (must not be null)
     */
    public ArgumentArraySerializerV5(Serializer<String> stringSerializer) {
        if (stringSerializer == null) {
            throw new IllegalArgumentException("stringSerializer cannot be null");
        }
        this.stringSerializer = stringSerializer;
        setAcceptsNull(false);
    }

    public Serializer<String> getStringSerializer() {
        return stringSerializer;
    }

    @Override
    public void write(Kryo kryo, Output output, Object[] arguments) {
        output.writeVarInt(arguments.length, true);
        for (Object arg : arguments) {
            SafeObjectCodec.write(kryo, output, stringSerializer, arg);
        }
    }

    @Override
    public Object[] read(Kryo kryo, Input input, Class<? extends Object[]> type) {
        int length = input.readVarInt(true);
        Object[] arguments = new Object[length];
        for (int i = 0; i < length; i++) {
            arguments[i] = SafeObjectCodec.read(kryo, input, stringSerializer);
        }
        return arguments;
    }
}
