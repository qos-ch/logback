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

import ch.qos.logback.core.kryo.Crc16;

import java.util.Arrays;

import static ch.qos.logback.classic.kryo.KryoClassicConstants.STREAM_HEADER_SIZE;
import static ch.qos.logback.classic.kryo.KryoClassicConstants.STREAM_MAGIC;

/**
 * V4 record framing. Every record is {@value #FRAME_HEADER_SIZE} bytes, then an optional payload.
 *
 * <pre>
 *   HEADER  — {@code STWD} + version + flags (no payload)
 *   EVENT   — {@link #TYPE_EVENT} + uint24 length + CRC-16 + payload
 *   CLOSE   — {@link #CLOSE_CODE} (no payload)
 * </pre>
 *
 * CRC-16/CCITT-FALSE covers type + 24-bit length and a small payload window:
 * the whole payload if {@code length <= 8}, otherwise the first and last
 * {@link #CRC_WINDOW} bytes. The middle is not covered; the check is for torn
 * writes (process exit / power fail) where a later session is appended after
 * a truncated EVENT.
 */
public final class StreamFrame {

    public static final int FRAME_HEADER_SIZE = STREAM_HEADER_SIZE;

    /** Bytes hashed at each end of a payload longer than {@code 2 * CRC_WINDOW}. */
    public static final int CRC_WINDOW = 4;

    /** ASCII RS. Must not equal {@code 'S'} so HEADER stays unique on a 6-byte boundary. */
    public static final byte TYPE_EVENT = 0x1E;

    /** ASCII CAN + {@code CLOSE}. */
    public static final byte[] CLOSE_CODE = new byte[] { 0x18, 'C', 'L', 'O', 'S', 'E' };

    public static final int MAX_PAYLOAD_LENGTH = 0xFF_FF_FF;

    /**
     * Set to {@code false} to skip CRC-16 compute (encode) and verify (decode)
     * for performance experiments. Frame layout is unchanged; CRC bytes are written as 0.
     * JVM: {@code -Dlogback.kryo.frame.crc16=false}. Default is on.
     */
    public static final String CRC16_PROPERTY_KEY = "logback.kryo.frame.crc16";

    public static volatile boolean crc16Enabled =
            !"false".equalsIgnoreCase(System.getProperty(CRC16_PROPERTY_KEY, "true"));

    private StreamFrame() {
    }

    public static boolean isStreamHeader(byte[] rec) {
        return rec != null
                && rec.length >= STREAM_HEADER_SIZE
                && rec[0] == STREAM_MAGIC[0]
                && rec[1] == STREAM_MAGIC[1]
                && rec[2] == STREAM_MAGIC[2]
                && rec[3] == STREAM_MAGIC[3];
    }

    public static boolean isClose(byte[] rec) {
        return rec != null && rec.length >= FRAME_HEADER_SIZE
                && rec[0] == CLOSE_CODE[0]
                && rec[1] == CLOSE_CODE[1]
                && rec[2] == CLOSE_CODE[2]
                && rec[3] == CLOSE_CODE[3]
                && rec[4] == CLOSE_CODE[4]
                && rec[5] == CLOSE_CODE[5];
    }

    public static boolean isEvent(byte[] rec) {
        return rec != null && rec.length >= FRAME_HEADER_SIZE && rec[0] == TYPE_EVENT;
    }

    public static int lengthOf(byte[] rec) {
        return ((rec[1] & 0xFF) << 16) | ((rec[2] & 0xFF) << 8) | (rec[3] & 0xFF);
    }

    public static int crcOf(byte[] rec) {
        return ((rec[4] & 0xFF) << 8) | (rec[5] & 0xFF);
    }

    public static int computeCrc(byte type, int length, byte[] payload) {
        return computeCrc(type, length, payload, 0);
    }

    public static int computeCrc(byte type, int length, byte[] payload, int off) {
        if (!crc16Enabled) {
            return 0;
        }
        Crc16 crc = new Crc16();
        crc.update(type);
        crc.update(length >>> 16);
        crc.update(length >>> 8);
        crc.update(length);
        if (length <= 0) {
            return crc.getValue();
        }
        if (length <= CRC_WINDOW * 2) {
            crc.update(payload, off, length);
        } else {
            crc.update(payload, off, CRC_WINDOW);
            crc.update(payload, off + length - CRC_WINDOW, CRC_WINDOW);
        }
        return crc.getValue();
    }

    public static boolean crcMatches(byte[] rec, byte[] payload) {
        if (!crc16Enabled) {
            return true;
        }
        return crcOf(rec) == computeCrc(rec[0], lengthOf(rec), payload);
    }

    /**
     * Writes the 6-byte EVENT prefix at {@code dest[headerOff]}. Payload bytes must
     * already occupy {@code dest[headerOff + FRAME_HEADER_SIZE .. + payloadLen)}.
     */
    public static void writeEventPrefix(byte[] dest, int headerOff, int payloadLen) {
        if (payloadLen < 0 || payloadLen > MAX_PAYLOAD_LENGTH) {
            throw new IllegalArgumentException("payload too large: " + payloadLen);
        }
        dest[headerOff] = TYPE_EVENT;
        dest[headerOff + 1] = (byte) (payloadLen >>> 16);
        dest[headerOff + 2] = (byte) (payloadLen >>> 8);
        dest[headerOff + 3] = (byte) payloadLen;
        if (!crc16Enabled) {
            dest[headerOff + 4] = 0;
            dest[headerOff + 5] = 0;
            return;
        }
        int crc = computeCrc(TYPE_EVENT, payloadLen, dest, headerOff + FRAME_HEADER_SIZE);
        dest[headerOff + 4] = (byte) (crc >>> 8);
        dest[headerOff + 5] = (byte) crc;
    }

    public static byte[] wrapEvent(byte[] payload) {
        if (payload == null) {
            throw new IllegalArgumentException("payload cannot be null");
        }
        byte[] out = new byte[FRAME_HEADER_SIZE + payload.length];
        System.arraycopy(payload, 0, out, FRAME_HEADER_SIZE, payload.length);
        writeEventPrefix(out, 0, payload.length);
        return out;
    }

    public static byte[] concat(byte[]... parts) {
        int n = 0;
        for (byte[] p : parts) {
            n += p.length;
        }
        byte[] out = new byte[n];
        int o = 0;
        for (byte[] p : parts) {
            System.arraycopy(p, 0, out, o, p.length);
            o += p.length;
        }
        return out;
    }

    public static int indexOfMagic(byte[] buf, int from, int to) {
        int last = to - STREAM_MAGIC.length;
        for (int i = from; i <= last; i++) {
            if (buf[i] == STREAM_MAGIC[0]
                    && buf[i + 1] == STREAM_MAGIC[1]
                    && buf[i + 2] == STREAM_MAGIC[2]
                    && buf[i + 3] == STREAM_MAGIC[3]) {
                return i;
            }
        }
        return -1;
    }

    public static int countOccurrences(byte[] haystack, byte[] needle) {
        if (needle.length == 0 || haystack.length < needle.length) {
            return 0;
        }
        int count = 0;
        outer:
        for (int i = 0; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            count++;
        }
        return count;
    }

    public static byte[] closeCode() {
        return Arrays.copyOf(CLOSE_CODE, CLOSE_CODE.length);
    }
}
