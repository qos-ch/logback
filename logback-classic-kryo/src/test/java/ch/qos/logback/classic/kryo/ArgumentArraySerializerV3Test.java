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
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArgumentArraySerializerV3Test {

    private Kryo kryo;
    private ArgumentArraySerializerV3 serializer;

    @BeforeEach
    public void setUp() {
        serializer = new ArgumentArraySerializerV3(256);
        kryo = new Kryo();
        kryo.register(Object[].class, serializer);
    }

    @Test
    public void roundTripAllElementTypes() {
        Object[] original = new Object[] {
                "hello",
                null,
                42,
                99L,
                (short) 7,
                1.5f,
                2.5d,
                new Object() {
                    @Override
                    public String toString() {
                        return "stringified";
                    }
                }
        };

        Object[] restored = roundTrip(original);

        assertThat(restored).hasSize(original.length);
        assertThat(restored[0]).isEqualTo("hello");
        assertThat(restored[1]).isNull();
        assertThat(restored[2]).isEqualTo(42).isInstanceOf(Integer.class);
        assertThat(restored[3]).isEqualTo(99L).isInstanceOf(Long.class);
        assertThat(restored[4]).isEqualTo((short) 7).isInstanceOf(Short.class);
        assertThat(restored[5]).isEqualTo(1.5f);
        assertThat(restored[6]).isEqualTo(2.5d);
        assertThat(restored[7]).isEqualTo("stringified").isInstanceOf(String.class);
    }

    @Test
    public void emptyArray() {
        Object[] restored = roundTrip(new Object[0]);
        assertThat(restored).isEmpty();
    }

    @Test
    public void longStringArgsRoundTripWithMultiEventStringSerializer() {
        MultiEventCompressedStringSerializer strings = new MultiEventCompressedStringSerializer(256);
        ArgumentArraySerializerV3 ser = new ArgumentArraySerializerV3(strings);
        String longArg = "y".repeat(300);
        Object[] original = new Object[] { "short", longArg, longArg };
        Kryo k = new Kryo();
        Output out = new Output(2048, -1);
        k.writeObject(out, original, ser);
        out.flush();
        Object[] restored = k.readObject(new Input(out.toBytes()), Object[].class, ser);
        assertThat(restored).containsExactly(original);
    }

    @Test
    public void repeatedStringsShareCache() {
        Object[] thrice = new Object[] { "x", "x", "x" };
        Object[] once = new Object[] { "x" };

        int thriceLen = write(new ArgumentArraySerializerV3(256), thrice).length;
        int onceLen = write(new ArgumentArraySerializerV3(256), once).length;

        assertThat(thriceLen).isLessThan(onceLen * 3);
    }

    private Object[] roundTrip(Object[] original) {
        byte[] bytes = write(serializer, original);
        return kryo.readObject(new Input(bytes), Object[].class, serializer);
    }

    private static byte[] write(ArgumentArraySerializerV3 ser, Object[] array) {
        Kryo k = new Kryo();
        Output out = new Output(512, -1);
        k.writeObject(out, array, ser);
        out.flush();
        return out.toBytes();
    }
}
