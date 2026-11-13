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

public class KryoCoreConstants {

    static public boolean NEW_ENTRY = true;
    static public boolean CACHED_ENTRY = false;

    /**
     * Sentinel id written on the wire (as 0 in the id field) when a value is not
     * registered in the id map (e.g. frequency-gated first sightings).
     */
    static public int UNREGISTERED_ID = -1;

    /**
     * Maximum id that may appear in a {@link SerializerBase} entry header (12-bit field).
     * Ids are assigned starting at 1; {@link #UNREGISTERED_ID} is encoded as 0.
     */
    static public final int MAX_SERIALIZER_ID = 0xFFF; // 4095

    /**
     * Live size of a {@link BidirectionalObjectToIdMap}: at least 1, at most
     * {@link #MAX_SERIALIZER_ID} so LRU cannot mint an id outside the 12-bit header.
     */
    public static int cappedSerializerCapacity(int capacity) {
        if (capacity < 1) {
            return 1;
        }
        return Math.min(capacity, MAX_SERIALIZER_ID);
    }

    /**
     * 2-byte entry header layout (big-endian short):
     * <pre>
     *   bits 15-3: 13-bit data
     *     bit 12 of data (bit 15 of short): 1 = NEW_ENTRY, 0 = CACHED_ENTRY
     *     bits 0-11 of data (bits 14-3 of short): id; 0 means UNREGISTERED_ID on NEW only
     *   bits 2-0:  3-bit CRC over the 13 data bits (poly x^3+x+1)
     * </pre>
     */
    static public final int ENTRY_HEADER_DATA_BITS = 13;
    static public final int ENTRY_HEADER_CRC_BITS = 3;
    static public final int ENTRY_HEADER_CRC_MASK = 0x7;
    static public final int ENTRY_HEADER_DATA_MASK = 0x1FFF;

    /** Within the 13-bit data word: NEW_ENTRY flag. */
    static public final int ENTRY_HEADER_NEW_FLAG = 1 << 12;

    /** Within the 13-bit data word: 12-bit id mask. */
    static public final int ENTRY_HEADER_ID_MASK = 0xFFF;

    /**
     * CRC-3 generator polynomial {@code x^3 + x + 1} (0b1011), used over the 13 data bits.
     */
    static public final int ENTRY_HEADER_CRC3_POLY = 0b1011;

    static public final String KRYO_UNSAFE_PROPERTY_KEY = "kryo.unsafe";
}
