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

import static org.assertj.core.api.Assertions.assertThat;

public class Crc3Test {

    @Test
    public void emptyIsZero() {
        assertThat(new Crc3().getValue()).isEqualTo(0);
    }

    @Test
    public void bitsThenBytesAreStable() {
        Crc3 a = new Crc3();
        a.updateBits(1, 5);
        a.update(0);
        int first = a.getValue();
        assertThat(first).isBetween(0, 7);

        Crc3 b = new Crc3();
        b.updateBits(1, 5);
        b.update(0);
        assertThat(b.getValue()).isEqualTo(first);
    }

    @Test
    public void tableMatchesBitLoop() {
        for (int c = 0; c < 8; c++) {
            for (int b = 0; b < 256; b++) {
                assertThat(Crc3.tableByte(c, b))
                        .as("crc=%d byte=%d", c, b)
                        .isEqualTo(Crc3.crcByteSlow(c, b));
            }
        }
    }

    @Test
    public void tableStreamMatchesBitStream() {
        byte[] data = {0, 1, (byte) 0xFF, 7, 8, 9, 10, 64, 127, (byte) 0x80, (byte) 0xAB};
        Crc3 table = new Crc3();
        table.updateBits(1, 5);
        table.update(data, 0, data.length);

        Crc3 slow = new Crc3();
        slow.updateBits(1, 5);
        for (byte b : data) {
            int c = slow.getValue();
            for (int i = 7; i >= 0; i--) {
                slow.updateBit((b >>> i) & 1);
            }
            assertThat(slow.getValue()).isEqualTo(Crc3.crcByteSlow(c, b & 0xFF));
        }
        assertThat(table.getValue()).isEqualTo(slow.getValue());
    }

    @Test
    public void payloadFlipChangesCrc() {
        byte[] payload = {1, 2, 3, 4, 5, 6, 7, 8};
        Crc3 good = new Crc3();
        good.updateBits(1, 5);
        good.update(payload.length);
        good.update(payload, 0, payload.length);

        payload[0] ^= 1;
        Crc3 bad = new Crc3();
        bad.updateBits(1, 5);
        bad.update(payload.length);
        bad.update(payload, 0, payload.length);

        assertThat(bad.getValue()).isNotEqualTo(good.getValue());
    }
}
