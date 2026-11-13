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
 * CRC-16/CCITT-FALSE ({@code poly=0x1021}, {@code init=0xFFFF}, no xorout, unreflected).
 *
 * <p>Known vector: {@code "123456789"} → {@code 0x29B1}.
 *
 * <p>One payload byte is one table lookup, not an 8-step bit loop.
 */
public final class Crc16 {

    public static final int POLY = 0x1021;
    public static final int INIT = 0xFFFF;

    private static final int[] TABLE = new int[256];

    static {
        for (int i = 0; i < 256; i++) {
            int c = i << 8;
            for (int bit = 0; bit < 8; bit++) {
                if ((c & 0x8000) != 0) {
                    c = (c << 1) ^ POLY;
                } else {
                    c <<= 1;
                }
                c &= 0xFFFF;
            }
            TABLE[i] = c;
        }
    }

    private int crc = INIT;

    public void update(int b) {
        crc = ((crc << 8) ^ TABLE[((crc >>> 8) ^ (b & 0xFF)) & 0xFF]) & 0xFFFF;
    }

    public void update(byte[] data) {
        update(data, 0, data.length);
    }

    public void update(byte[] data, int off, int len) {
        int c = crc;
        int end = off + len;
        for (int i = off; i < end; i++) {
            c = ((c << 8) ^ TABLE[((c >>> 8) ^ (data[i] & 0xFF)) & 0xFF]) & 0xFFFF;
        }
        crc = c;
    }

    public int getValue() {
        return crc & 0xFFFF;
    }

    public static int of(byte[] data) {
        Crc16 crc = new Crc16();
        crc.update(data);
        return crc.getValue();
    }
}
