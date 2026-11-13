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
package ch.qos.logback.core.kryo;

/**
 * CRC-3, polynomial {@code x^3 + x + 1} ({@code 0b1011}), init 0, MSB-first, no xorout.
 *
 * <p>Same remainder as a bit-at-a-time LFSR. Bytes use a 256-entry table plus an 8-entry
 * “shift 8 zero bits” table so {@code crc(C, B) = ZERO8[C] xor TABLE[B]}.
 */
public final class Crc3 {

    public static final int POLY = 0b1011;
    public static final int MASK = 0b111;

    /** Remainder of byte {@code i} starting from crc 0. */
    private static final int[] TABLE = new int[256];

    /** Remainder after 8 zero bits starting from crc {@code c}. */
    private static final int[] ZERO8 = new int[8];

    static {
        for (int b = 0; b < 256; b++) {
            TABLE[b] = crcByteSlow(0, b);
        }
        for (int c = 0; c < 8; c++) {
            ZERO8[c] = crcByteSlow(c, 0);
        }
    }

    private int crc;

    static int crcByteSlow(int crc, int b) {
        int c = crc & MASK;
        for (int i = 7; i >= 0; i--) {
            c = step(c, (b >>> i) & 1);
        }
        return c;
    }

    static int step(int crc, int bit) {
        int mix = ((crc >>> 2) ^ (bit & 1)) & 1;
        crc = (crc << 1) & MASK;
        if (mix != 0) {
            crc ^= 0b011;
        }
        return crc;
    }

    public void updateBit(int bit) {
        crc = step(crc, bit);
    }

    /** {@code nbits} low bits of {@code value}, MSB first. */
    public void updateBits(int value, int nbits) {
        for (int i = nbits - 1; i >= 0; i--) {
            updateBit((value >>> i) & 1);
        }
    }

    public void update(int b) {
        crc = tableByte(crc, b);
    }

    /** Remainder after one data byte from state {@code crc}. */
    static int tableByte(int crc, int b) {
        return ZERO8[crc & MASK] ^ TABLE[b & 0xFF];
    }

    public void update(byte[] data, int off, int len) {
        int c = crc;
        int end = off + len;
        for (int i = off; i < end; i++) {
            c = ZERO8[c] ^ TABLE[data[i] & 0xFF];
        }
        crc = c;
    }

    public int getValue() {
        return crc & MASK;
    }

    public void reset() {
        crc = 0;
    }
}
