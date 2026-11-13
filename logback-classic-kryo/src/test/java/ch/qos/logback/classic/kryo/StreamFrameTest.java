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

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static ch.qos.logback.classic.kryo.KryoClassicConstants.STREAM_HEADER_SIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class StreamFrameTest {

    @Test
    public void wrapEventRoundTrip() {
        byte[] payload = "hello-event".getBytes(StandardCharsets.UTF_8);
        byte[] framed = StreamFrame.wrapEvent(payload);

        assertThat(framed).hasSize(STREAM_HEADER_SIZE + payload.length);
        assertThat(StreamFrame.isEvent(framed)).isTrue();
        assertThat(StreamFrame.isClose(framed)).isFalse();
        assertThat(StreamFrame.isStreamHeader(framed)).isFalse();
        assertThat(StreamFrame.lengthOf(framed)).isEqualTo(payload.length);
        assertThat(StreamFrame.crcMatches(framed, payload)).isTrue();
    }

    @Test
    public void crcDetectsPayloadFlip() {
        byte[] payload = new byte[] { 1, 2, 3, 4 };
        byte[] framed = StreamFrame.wrapEvent(payload);
        payload[0] ^= 0x01;
        assertThat(StreamFrame.crcMatches(framed, payload)).isFalse();
    }

    @Test
    public void crcCoversFirstAndLastWindowNotMiddle() {
        byte[] payload = new byte[20];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) (i + 1);
        }
        byte[] framed = StreamFrame.wrapEvent(payload);

        payload[0] ^= 0x01;
        assertThat(StreamFrame.crcMatches(framed, payload)).isFalse();
        payload[0] ^= 0x01;

        payload[payload.length - 1] ^= 0x01;
        assertThat(StreamFrame.crcMatches(framed, payload)).isFalse();
        payload[payload.length - 1] ^= 0x01;

        payload[10] ^= 0x01;
        assertThat(StreamFrame.crcMatches(framed, payload)).isTrue();
    }

    @Test
    public void crcEmptyPayloadIsTypeAndLengthOnly() {
        byte[] framed = StreamFrame.wrapEvent(new byte[0]);
        assertThat(StreamFrame.lengthOf(framed)).isEqualTo(0);
        assertThat(StreamFrame.crcMatches(framed, new byte[0])).isTrue();
    }

    @Test
    public void crc16CanBeDisabled() {
        boolean previous = StreamFrame.crc16Enabled;
        StreamFrame.crc16Enabled = false;
        try {
            byte[] payload = new byte[] { 1, 2, 3, 4 };
            byte[] framed = StreamFrame.wrapEvent(payload);
            assertThat(StreamFrame.crcOf(framed)).isEqualTo(0);
            payload[0] ^= 0x01;
            assertThat(StreamFrame.crcMatches(framed, payload)).isTrue();
        } finally {
            StreamFrame.crc16Enabled = previous;
        }
    }

    @Test
    public void closeCodeIsNotEventOrHeader() {
        byte[] close = StreamFrame.closeCode();
        assertThat(close).hasSize(STREAM_HEADER_SIZE);
        assertThat(StreamFrame.isClose(close)).isTrue();
        assertThat(StreamFrame.isEvent(close)).isFalse();
        assertThat(StreamFrame.isStreamHeader(close)).isFalse();
        assertThat(close[0]).isNotEqualTo((byte) 'S');
    }

    @Test
    public void streamHeaderDetection() {
        byte[] header = { 'S', 'T', 'W', 'D', 4, 1 };
        assertThat(StreamFrame.isStreamHeader(header)).isTrue();
        assertThat(StreamFrame.isEvent(header)).isFalse();
        assertThat(StreamFrame.isClose(header)).isFalse();
    }

    @Test
    public void writeEventPrefixMatchesWrapEvent() {
        byte[] payload = "hello-event".getBytes(StandardCharsets.UTF_8);
        byte[] inplace = new byte[STREAM_HEADER_SIZE + payload.length + 8];
        System.arraycopy(payload, 0, inplace, STREAM_HEADER_SIZE, payload.length);
        StreamFrame.writeEventPrefix(inplace, 0, payload.length);

        byte[] wrapped = StreamFrame.wrapEvent(payload);
        assertThat(java.util.Arrays.copyOf(inplace, wrapped.length)).isEqualTo(wrapped);
        byte[] payloadOnly = new byte[payload.length];
        System.arraycopy(inplace, STREAM_HEADER_SIZE, payloadOnly, 0, payload.length);
        assertThat(StreamFrame.crcMatches(inplace, payloadOnly)).isTrue();
    }

    @Test
    public void wrapEventRejectsNull() {
        assertThatThrownBy(() -> StreamFrame.wrapEvent(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void indexOfMagic() {
        byte[] buf = { 0, 1, 'S', 'T', 'W', 'D', 4, 1 };
        assertThat(StreamFrame.indexOfMagic(buf, 0, buf.length)).isEqualTo(2);
        assertThat(StreamFrame.indexOfMagic(buf, 3, buf.length)).isEqualTo(-1);
    }
}
