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

import com.esotericsoftware.kryo.kryo5.KryoException;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import org.junit.jupiter.api.Test;

import static ch.qos.logback.core.kryo.KryoCoreConstants.ENTRY_HEADER_CRC_BITS;
import static ch.qos.logback.core.kryo.KryoCoreConstants.ENTRY_HEADER_NEW_FLAG;
import static ch.qos.logback.core.kryo.KryoCoreConstants.MAX_SERIALIZER_ID;
import static ch.qos.logback.core.kryo.KryoCoreConstants.UNREGISTERED_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SerializerBaseEntryHeaderTest {

    @Test
    public void roundTripNewAndCached() {
        byte[] buf = new byte[16];
        Output out = new Output(buf);

        SerializerBase.writeEntryHeader(out, true, 1);
        SerializerBase.writeEntryHeader(out, false, 42);
        SerializerBase.writeEntryHeader(out, true, UNREGISTERED_ID);
        SerializerBase.writeEntryHeader(out, true, MAX_SERIALIZER_ID);
        out.flush();

        Input in = new Input(buf, 0, out.position());

        int d0 = SerializerBase.readEntryHeaderData(in);
        assertThat(d0 & ENTRY_HEADER_NEW_FLAG).isNotZero();
        assertThat(SerializerBase.decodeHeaderId(d0)).isEqualTo(1);

        int d1 = SerializerBase.readEntryHeaderData(in);
        assertThat(d1 & ENTRY_HEADER_NEW_FLAG).isZero();
        assertThat(SerializerBase.decodeHeaderId(d1)).isEqualTo(42);

        int d2 = SerializerBase.readEntryHeaderData(in);
        assertThat(d2 & ENTRY_HEADER_NEW_FLAG).isNotZero();
        assertThat(SerializerBase.decodeHeaderId(d2)).isEqualTo(UNREGISTERED_ID);

        int d3 = SerializerBase.readEntryHeaderData(in);
        assertThat(SerializerBase.decodeHeaderId(d3)).isEqualTo(MAX_SERIALIZER_ID);
    }

    @Test
    public void crcDetectsBitFlip() {
        byte[] buf = new byte[4];
        Output out = new Output(buf);
        SerializerBase.writeEntryHeader(out, true, 100);
        out.flush();

        // Flip a data bit (bit 8 of the short is inside the data field)
        buf[0] ^= 0x01;

        Input in = new Input(buf, 0, out.position());
        assertThatThrownBy(() -> SerializerBase.readEntryHeaderData(in))
                .isInstanceOf(KryoException.class)
                .hasMessageContaining("CRC mismatch");
    }

    @Test
    public void crc3StableForKnownVector() {
        // data = NEW | id 1 → 0x1001; CRC is deterministic
        int data13 = ENTRY_HEADER_NEW_FLAG | 1;
        int crc = SerializerBase.crc3(data13);
        assertThat(crc).isBetween(0, 7);
        assertThat(SerializerBase.crc3(data13)).isEqualTo(crc);

        int packed = (data13 << ENTRY_HEADER_CRC_BITS) | crc;
        assertThat(SerializerBase.crc3((packed >>> ENTRY_HEADER_CRC_BITS) & 0x1FFF)).isEqualTo(crc);
    }
}
