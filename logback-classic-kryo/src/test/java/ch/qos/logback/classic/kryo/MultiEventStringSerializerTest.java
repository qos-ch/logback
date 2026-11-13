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

public class MultiEventStringSerializerTest {

    private Kryo kryo;
    private MultiEventCompressedStringSerializer serializer;

    @BeforeEach
    public void setUp() {
        serializer = new MultiEventCompressedStringSerializer(256);
        kryo = new Kryo();
        kryo.register(String.class, serializer);
    }

    @Test
    public void roundTripSingleShortString() {
        assertThat(writeThenReadAll("hello")).containsExactly("hello");
    }

    @Test
    public void shortNewEntryIsUnmodified() {
        Output out = new Output(512, -1);
        serializer.write(kryo, out, "hello");
        out.flush();
        byte[] bytes = out.toBytes();
        // 2-byte SerializerBase header, then 'U'
        assertThat(bytes.length).isGreaterThan(2);
        assertThat(bytes[2]).isEqualTo(MultiEventCompressedStringSerializer.UNMODIFIED);
    }

    @Test
    public void longNewEntryIsCompressed() {
        String longVal = "y".repeat(300);
        Output out = new Output(2048, -1);
        serializer.write(kryo, out, longVal);
        out.flush();
        byte[] bytes = out.toBytes();
        assertThat(bytes[2]).isEqualTo(MultiEventCompressedStringSerializer.COMPRESSED);
        // 'C' + 4-byte chunk length, then raw DEFLATE — not gzip magic 1f 8b
        assertThat(bytes[7]).isNotEqualTo((byte) 0x1f);
    }

    @Test
    public void roundTripStreamOfRecords_retainsDeflateState() {
        String[] records = {
                "alpha-record-0001",
                "x".repeat(300),
                "short",
                "x".repeat(300) + "-again",
                "y".repeat(280)
        };
        assertThat(writeThenReadAll(records)).containsExactly(records);
    }

    @Test
    public void interleavedShortAndLong_roundTrip() {
        String[] records = {
                "tiny",
                "z".repeat(256),
                "",
                "z".repeat(256) + "b",
                "x"
        };
        assertThat(writeThenReadAll(records)).containsExactly(records);
    }

    @Test
    public void streamCompressionBetterThanIndependentDeflateMembers() {
        // Many similar long records: shared DEFLATE state should beat N independent compressors.
        String[] records = new String[40];
        for (int i = 0; i < records.length; i++) {
            records[i] = "common-prefix/host=app-server/path=/api/v1/items/".repeat(6) + i;
        }

        MultiEventCompressedStringSerializer streaming = new MultiEventCompressedStringSerializer(256);
        int streamingBytes = measureEncodedSize(streaming, records);

        int independentBytes = 0;
        for (String r : records) {
            MultiEventCompressedStringSerializer oneShot = new MultiEventCompressedStringSerializer(256);
            independentBytes += measureEncodedSize(oneShot, r);
        }

        assertThat(streamingBytes)
                .as("stateful stream should be smaller than independent compressors")
                .isLessThan(independentBytes);
    }

    @Test
    public void unicodeLongStringRoundTrip() {
        String[] records = {
                "short-ascii",
                "日本語メッセージ".repeat(40),
                "emoji 😀".repeat(80)
        };
        assertThat(writeThenReadAll(records)).containsExactly(records);
    }

    @Test
    public void multibyteJustUnderThresholdIsUnmodified() {
        // 85 CJK chars → 255 UTF-8 bytes, under the 256-byte threshold
        String justUnder = "中".repeat(85);
        assertThat(justUnder.getBytes(java.nio.charset.StandardCharsets.UTF_8).length).isEqualTo(255);

        Output out = new Output(1024, -1);
        serializer.write(kryo, out, justUnder);
        out.flush();
        byte[] bytes = out.toBytes();
        assertThat(bytes[2]).isEqualTo(MultiEventCompressedStringSerializer.UNMODIFIED);
        assertThat(writeThenReadAll(justUnder)).containsExactly(justUnder);
    }

    @Test
    public void cachedRepeatDoesNotBreakStreamState() {
        // First write is NEW (may enter deflate stream); later writes are CACHED.
        String[] records = { "same", "same", "same", "other", "same" };
        assertThat(writeThenReadAll(records)).containsExactly(records);
    }

    private String[] writeThenReadAll(String... values) {
        Output out = new Output(8192, -1);
        for (String v : values) {
            kryo.writeObject(out, v, serializer);
        }
        out.flush();

        Input in = new Input(out.toBytes());
        String[] restored = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            restored[i] = kryo.readObject(in, String.class, serializer);
        }
        return restored;
    }

    private static int measureEncodedSize(MultiEventCompressedStringSerializer ser, String... values) {
        Kryo k = new Kryo();
        Output out = new Output(8192, -1);
        for (String v : values) {
            k.writeObject(out, v, ser);
        }
        out.flush();
        return out.position();
    }
}
