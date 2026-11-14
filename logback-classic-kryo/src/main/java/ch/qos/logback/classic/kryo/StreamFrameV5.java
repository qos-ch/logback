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

import ch.qos.logback.core.kryo.Crc3;

import static ch.qos.logback.classic.kryo.KryoClassicConstants.STREAM_HEADER_SIZE;
import static ch.qos.logback.classic.kryo.KryoClassicConstants.STREAM_MAGIC;

/**
 * V5 record framing. EVENT/CLOSE prefixes are 1 type/crc byte plus an unsigned Kryo
 * varint length (typically 2 bytes total). Epoch {@code STWD} headers stay 6 bytes.
 *
 * <pre>
 *   byte0:  type (5) | crc3 (3)
 *   then:   unsigned varint length
 *   then:   payload[length]
 * </pre>
 *
 * CRC-3 covers type (5 bits), the length varint bytes, and a small payload window:
 * all bytes if {@code length <= 8}, otherwise first and last {@link #CRC_WINDOW} bytes.
 * The middle is not covered; the check is for torn writes (process exit / power fail)
 * where a later session is appended after a truncated EVENT.
 */
public final class StreamFrameV5 {

    public static final int TYPE_EVENT = 1;
    public static final int TYPE_CLOSE = 2;

    public static final int TYPE_BITS = 5;
    public static final int CRC_BITS = 3;
    public static final int TYPE_MASK = 0x1F;
    public static final int CRC_MASK = 0x7;

    /** 1 type/crc byte + at most 3 varint bytes (covers {@link KryoEncoder#MAX_BUFFER_SIZE}). */
    public static final int MAX_PREFIX = 4;

    /** Bytes hashed at each end of a payload longer than {@code 2 * CRC_WINDOW}. */
    public static final int CRC_WINDOW = 4;

    private StreamFrameV5() {
    }

    public static boolean isStreamHeader(byte[] rec) {
        return rec != null
                && rec.length >= STREAM_HEADER_SIZE
                && rec[0] == STREAM_MAGIC[0]
                && rec[1] == STREAM_MAGIC[1]
                && rec[2] == STREAM_MAGIC[2]
                && rec[3] == STREAM_MAGIC[3];
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

    public static int pack(int type, int crc3) {
        return ((type & TYPE_MASK) << CRC_BITS) | (crc3 & CRC_MASK);
    }

    public static int typeOf(int packedByte) {
        return (packedByte >>> CRC_BITS) & TYPE_MASK;
    }

    public static int crcOf(int packedByte) {
        return packedByte & CRC_MASK;
    }

    public static int unsignedVarIntSize(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("varint length must be >= 0: " + value);
        }
        if (value >>> 7 == 0) {
            return 1;
        }
        if (value >>> 14 == 0) {
            return 2;
        }
        if (value >>> 21 == 0) {
            return 3;
        }
        if (value >>> 28 == 0) {
            return 4;
        }
        return 5;
    }

    /** Kryo unsigned varint into {@code dest[off..]}. Returns bytes written. */
    public static int writeUnsignedVarInt(byte[] dest, int off, int value) {
        int i = off;
        while ((value & ~0x7F) != 0) {
            dest[i++] = (byte) ((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        dest[i++] = (byte) value;
        return i - off;
    }

    public static byte[] encodeUnsignedVarInt(int value) {
        byte[] tmp = new byte[5];
        int n = writeUnsignedVarInt(tmp, 0, value);
        byte[] out = new byte[n];
        System.arraycopy(tmp, 0, out, 0, n);
        return out;
    }

    /**
     * Writes the EVENT prefix immediately before {@code dest[payloadOff]}. Payload bytes
     * must already occupy {@code dest[payloadOff .. payloadOff + payloadLen)}.
     *
     * @return prefix length in bytes (2–4)
     */
    public static int writeEventPrefix(byte[] dest, int payloadOff, int payloadLen) {
        if (payloadLen < 0 || payloadLen > KryoEncoder.MAX_BUFFER_SIZE) {
            throw new IllegalArgumentException("payload too large: " + payloadLen);
        }
        int varintLen = unsignedVarIntSize(payloadLen);
        int prefixLen = 1 + varintLen;
        if (payloadOff < prefixLen) {
            throw new IllegalArgumentException("payloadOff " + payloadOff + " < prefix " + prefixLen);
        }
        int headerOff = payloadOff - prefixLen;
        writeUnsignedVarInt(dest, headerOff + 1, payloadLen);
        int crc = computeCrc(TYPE_EVENT, dest, headerOff + 1, varintLen, dest, payloadOff, payloadLen);
        dest[headerOff] = (byte) pack(TYPE_EVENT, crc);
        return prefixLen;
    }

    public static byte[] wrapEvent(byte[] payload) {
        if (payload == null) {
            throw new IllegalArgumentException("payload cannot be null");
        }
        int varintLen = unsignedVarIntSize(payload.length);
        byte[] out = new byte[1 + varintLen + payload.length];
        writeUnsignedVarInt(out, 1, payload.length);
        int crc = computeCrc(TYPE_EVENT, out, 1, varintLen, payload, 0, payload.length);
        out[0] = (byte) pack(TYPE_EVENT, crc);
        System.arraycopy(payload, 0, out, 1 + varintLen, payload.length);
        return out;
    }

    public static byte[] closeCode() {
        byte[] out = new byte[2];
        writeUnsignedVarInt(out, 1, 0);
        out[0] = (byte) pack(TYPE_CLOSE, computeCrc(TYPE_CLOSE, out, 1, 1, out, 0, 0));
        return out;
    }

    public static int computeCrc(int type, byte[] lenBytes, byte[] payload, int off, int len) {
        return computeCrc(type, lenBytes, 0, lenBytes.length, payload, off, len);
    }

    public static int computeCrc(int type, byte[] lenBuf, int lenOff, int lenLen,
                                 byte[] payload, int off, int len) {
        Crc3 crc = new Crc3();
        crc.updateBits(type, TYPE_BITS);
        crc.update(lenBuf, lenOff, lenLen);
        if (len <= 0) {
            return crc.getValue();
        }
        int window = CRC_WINDOW;
        if (len <= window * 2) {
            crc.update(payload, off, len);
        } else {
            crc.update(payload, off, window);
            crc.update(payload, off + len - window, window);
        }
        return crc.getValue();
    }

    public static boolean crcMatches(int packedByte, byte[] lenBytes, byte[] payload, int off, int len) {
        int type = typeOf(packedByte);
        int expected = computeCrc(type, lenBytes, payload, off, len);
        return crcOf(packedByte) == expected;
    }
}
