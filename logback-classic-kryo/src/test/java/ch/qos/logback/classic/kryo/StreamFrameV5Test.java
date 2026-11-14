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

import com.esotericsoftware.kryo.kryo5.io.Output;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class StreamFrameV5Test {

    @Test
    public void wrapEventTypicalPrefixIsTwoBytes() {
        byte[] payload = "hello-event".getBytes(StandardCharsets.UTF_8);
        byte[] framed = StreamFrameV5.wrapEvent(payload);
        assertThat(payload.length).isLessThan(128);
        assertThat(framed).hasSize(2 + payload.length);
        assertThat(StreamFrameV5.typeOf(framed[0] & 0xFF)).isEqualTo(StreamFrameV5.TYPE_EVENT);
        assertThat(framed[0] & 0xFF).isBetween(0x08, 0x0F);
        assertThat(StreamFrameV5.crcMatches(framed[0] & 0xFF,
                new byte[] {framed[1]}, payload, 0, payload.length)).isTrue();
    }

    @Test
    public void varintMatchesKryo() {
        int[] values = {0, 1, 127, 128, 129, 16383, 16384, 65536};
        byte[] buf = new byte[8];
        for (int v : values) {
            int n = StreamFrameV5.writeUnsignedVarInt(buf, 0, v);
            byte[] ours = java.util.Arrays.copyOf(buf, n);
            Output out = new Output(8);
            out.writeVarInt(v, true);
            out.flush();
            byte[] kryo = java.util.Arrays.copyOf(out.getBuffer(), out.position());
            assertThat(ours).as("value " + v).isEqualTo(kryo);
            assertThat(StreamFrameV5.unsignedVarIntSize(v)).isEqualTo(n);
            assertThat(StreamFrameV5.encodeUnsignedVarInt(v)).isEqualTo(kryo);
        }
    }

    @Test
    public void length127IsOneVarintByteLength128IsTwo() {
        byte[] small = StreamFrameV5.wrapEvent(new byte[127]);
        assertThat(small).hasSize(2 + 127);
        byte[] large = StreamFrameV5.wrapEvent(new byte[128]);
        assertThat(large).hasSize(3 + 128);
    }

    @Test
    public void crcDetectsPayloadFlipInWindow() {
        byte[] payload = new byte[] {1, 2, 3, 4};
        byte[] framed = StreamFrameV5.wrapEvent(payload);
        byte[] lenBytes = java.util.Arrays.copyOfRange(framed, 1, framed.length - payload.length);
        payload[0] ^= 0x01;
        assertThat(StreamFrameV5.crcMatches(framed[0] & 0xFF, lenBytes, payload, 0, payload.length))
                .isFalse();
    }

    @Test
    public void crcCoversFirstAndLastWindowNotMiddle() {
        byte[] payload = new byte[20];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) (i + 1);
        }
        byte[] framed = StreamFrameV5.wrapEvent(payload);
        byte[] lenBytes = java.util.Arrays.copyOfRange(framed, 1, framed.length - payload.length);

        payload[0] ^= 0x01;
        assertThat(StreamFrameV5.crcMatches(framed[0] & 0xFF, lenBytes, payload, 0, payload.length)).isFalse();
        payload[0] ^= 0x01;

        payload[payload.length - 1] ^= 0x01;
        assertThat(StreamFrameV5.crcMatches(framed[0] & 0xFF, lenBytes, payload, 0, payload.length)).isFalse();
        payload[payload.length - 1] ^= 0x01;

        payload[10] ^= 0x01;
        assertThat(StreamFrameV5.crcMatches(framed[0] & 0xFF, lenBytes, payload, 0, payload.length)).isTrue();
    }

    @Test
    public void closeCodeIsTwoBytesAndNotHeader() {
        byte[] close = StreamFrameV5.closeCode();
        assertThat(close).hasSize(2);
        assertThat(StreamFrameV5.typeOf(close[0] & 0xFF)).isEqualTo(StreamFrameV5.TYPE_CLOSE);
        assertThat(close[0] & 0xFF).isBetween(0x10, 0x17);
        assertThat(close[0]).isNotEqualTo((byte) 'S');
        assertThat(StreamFrameV5.isStreamHeader(close)).isFalse();
        assertThat(StreamFrameV5.crcMatches(close[0] & 0xFF, new byte[] {close[1]}, new byte[0], 0, 0))
                .isTrue();
    }

    @Test
    public void writeEventPrefixMatchesWrapEvent() {
        byte[] payload = "hello-event".getBytes(StandardCharsets.UTF_8);
        byte[] buf = new byte[StreamFrameV5.MAX_PREFIX + payload.length];
        System.arraycopy(payload, 0, buf, StreamFrameV5.MAX_PREFIX, payload.length);
        int prefixLen = StreamFrameV5.writeEventPrefix(buf, StreamFrameV5.MAX_PREFIX, payload.length);
        byte[] wrapped = StreamFrameV5.wrapEvent(payload);
        byte[] inplace = java.util.Arrays.copyOfRange(buf, StreamFrameV5.MAX_PREFIX - prefixLen,
                StreamFrameV5.MAX_PREFIX + payload.length);
        assertThat(inplace).isEqualTo(wrapped);
    }

    @Test
    public void wrapEventRejectsNull() {
        assertThatThrownBy(() -> StreamFrameV5.wrapEvent(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void streamHeaderDetection() {
        byte[] header = { 'S', 'T', 'W', 'D', 5, 1 };
        assertThat(StreamFrameV5.isStreamHeader(header)).isTrue();
        assertThat(StreamFrameV5.isStreamHeader(new byte[] { 'S', 'T', 'W', 'D', 5 })).isFalse();
        assertThat(StreamFrameV5.isStreamHeader(null)).isFalse();
    }

    @Test
    public void indexOfMagic() {
        byte[] buf = { 0, 1, 'S', 'T', 'W', 'D', 5, 1 };
        assertThat(StreamFrameV5.indexOfMagic(buf, 0, buf.length)).isEqualTo(2);
        assertThat(StreamFrameV5.indexOfMagic(buf, 3, buf.length)).isEqualTo(-1);
    }

    @Test
    public void countOccurrences() {
        byte[] buf = { 'S', 'T', 'W', 'D', 0, 'S', 'T', 'W', 'D' };
        assertThat(StreamFrameV5.countOccurrences(buf, KryoClassicConstants.STREAM_MAGIC)).isEqualTo(2);
        assertThat(StreamFrameV5.countOccurrences(new byte[0], KryoClassicConstants.STREAM_MAGIC)).isEqualTo(0);
    }
}
