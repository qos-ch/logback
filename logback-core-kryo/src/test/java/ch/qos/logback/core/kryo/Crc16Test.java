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

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class Crc16Test {

    @Test
    public void ccittFalseKnownVector() {
        assertThat(Crc16.of("123456789".getBytes(StandardCharsets.US_ASCII))).isEqualTo(0x29B1);
    }

    @Test
    public void emptyIsInitValue() {
        assertThat(new Crc16().getValue()).isEqualTo(Crc16.INIT);
    }

    @Test
    public void tableMatchesBitLoop() {
        byte[][] samples = {
                new byte[0],
                new byte[] {0},
                new byte[] {(byte) 0xFF},
                "123456789".getBytes(StandardCharsets.US_ASCII),
                "hello-event".getBytes(StandardCharsets.UTF_8),
                new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, (byte) 0x1E, (byte) 0x80, (byte) 0xAB}
        };
        for (byte[] sample : samples) {
            assertThat(Crc16.of(sample)).isEqualTo(bitLoop(sample));
        }
    }

    @Test
    public void incrementalMatchesBulk() {
        byte[] data = "payload-with-more-than-one-byte".getBytes(StandardCharsets.US_ASCII);
        Crc16 incremental = new Crc16();
        for (byte b : data) {
            incremental.update(b);
        }
        assertThat(incremental.getValue()).isEqualTo(Crc16.of(data));
    }

    /** Original per-bit CCITT-FALSE step, used as a witness for the table. */
    private static int bitLoop(byte[] data) {
        int crc = Crc16.INIT;
        for (byte value : data) {
            crc ^= (value & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ Crc16.POLY;
                } else {
                    crc <<= 1;
                }
                crc &= 0xFFFF;
            }
        }
        return crc;
    }
}
