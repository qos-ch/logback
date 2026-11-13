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
import com.esotericsoftware.kryo.kryo5.KryoException;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

/**
 * Compact wire encoding for objects reduced by {@link SafeObjectTransformer}.
 *
 * <p>Applies {@link SafeObjectTransformer#asAllowedNonNullObject(Object)}, then writes a
 * 1-byte type tag and a type-specific payload (never {@code writeClassAndObject}).
 *
 * <pre>
 *   tag (1 byte) + payload
 *     0 NULL    — no payload (null or {@link SafeObjectTransformer#NULL_PROXY})
 *     1 STRING  — {@link SingleItemCompressingStringSerializer}
 *     2 INTEGER — writeVarInt (zig-zag)
 *     3 LONG    — writeVarLong (zig-zag)
 *     4 SHORT   — writeShort
 *     5 FLOAT   — writeFloat
 *     6 DOUBLE  — writeDouble
 * </pre>
 *
 * <p>Used for KVP values and message argument arrays.
 */
public final class SafeObjectCodec {

    public static final byte TAG_NULL = 0;
    public static final byte TAG_STRING = 1;
    public static final byte TAG_INTEGER = 2;
    public static final byte TAG_LONG = 3;
    public static final byte TAG_SHORT = 4;
    public static final byte TAG_FLOAT = 5;
    public static final byte TAG_DOUBLE = 6;

    private SafeObjectCodec() {
    }

    /**
     * Encodes {@code rawValue} after {@link SafeObjectTransformer#asAllowedNonNullObject(Object)}.
     * {@link SafeObjectTransformer#NULL_PROXY} is written as {@link #TAG_NULL} and restored as
     * {@code null} on read.
     */
    public static void write(Kryo kryo, Output output, Serializer<String> stringSerializer, Object rawValue) {
        Object value = SafeObjectTransformer.asAllowedNonNullObject(rawValue);

        if (value == SafeObjectTransformer.NULL_PROXY) {
            output.writeByte(TAG_NULL);
            return;
        }
        if (value instanceof String) {
            output.writeByte(TAG_STRING);
            kryo.writeObject(output, (String) value, stringSerializer);
            return;
        }
        if (value instanceof Integer) {
            output.writeByte(TAG_INTEGER);
            output.writeVarInt((Integer) value, false);
            return;
        }
        if (value instanceof Long) {
            output.writeByte(TAG_LONG);
            output.writeVarLong((Long) value, false);
            return;
        }
        if (value instanceof Short) {
            output.writeByte(TAG_SHORT);
            output.writeShort((Short) value);
            return;
        }
        if (value instanceof Float) {
            output.writeByte(TAG_FLOAT);
            output.writeFloat((Float) value);
            return;
        }
        if (value instanceof Double) {
            output.writeByte(TAG_DOUBLE);
            output.writeDouble((Double) value);
            return;
        }
        throw new KryoException("Unexpected value type after SafeObjectTransformer: "
                + value.getClass().getName());
    }

    /**
     * Inverse of {@link #write(Kryo, Output, Serializer, Object)}.
     */
    public static Object read(Kryo kryo, Input input, Serializer<String> stringSerializer) {
        byte tag = input.readByte();
        switch (tag) {
            case TAG_NULL:
                return null;
            case TAG_STRING:
                return kryo.readObject(input, String.class, stringSerializer);
            case TAG_INTEGER:
                return input.readVarInt(false);
            case TAG_LONG:
                return input.readVarLong(false);
            case TAG_SHORT:
                return input.readShort();
            case TAG_FLOAT:
                return input.readFloat();
            case TAG_DOUBLE:
                return input.readDouble();
            default:
                throw new KryoException("Unknown SafeObjectCodec value tag: " + tag);
        }
    }
}
