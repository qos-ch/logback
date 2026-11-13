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

public class SingleItemCompressingStringSerializerTest {

    private Kryo kryo;
    private SingleItemCompressingStringSerializer serializer;

    @BeforeEach
    public void setUp() {
        serializer = new SingleItemCompressingStringSerializer(256);
        kryo = new Kryo();
    }

    @Test
    public void roundTripShortAndLong() {
        assertThat(writeThenRead("hello")).isEqualTo("hello");
        String longVal = "y".repeat(400);
        assertThat(writeThenRead(longVal)).isEqualTo(longVal);
        assertThat(writeThenRead("日本語".repeat(100))).isEqualTo("日本語".repeat(100));
    }

    @Test
    public void longNewEntryIsCompressedWhenEffective() {
        Output out = new Output(2048, -1);
        serializer.write(kryo, out, "y".repeat(400));
        out.flush();
        byte[] bytes = out.toBytes();
        assertThat(bytes[2]).isEqualTo(SingleItemCompressingStringSerializer.COMPRESSED);
    }

    private String writeThenRead(String value) {
        Output out = new Output(4096, -1);
        serializer.write(kryo, out, value);
        out.flush();
        SingleItemCompressingStringSerializer reader = new SingleItemCompressingStringSerializer(256);
        return reader.read(kryo, new Input(out.toBytes()), String.class);
    }
}
