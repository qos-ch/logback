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

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.KryoException;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.minlog.Log;

import static ch.qos.logback.core.kryo.KryoCoreConstants.ENTRY_HEADER_CRC3_POLY;
import static ch.qos.logback.core.kryo.KryoCoreConstants.ENTRY_HEADER_CRC_BITS;
import static ch.qos.logback.core.kryo.KryoCoreConstants.ENTRY_HEADER_CRC_MASK;
import static ch.qos.logback.core.kryo.KryoCoreConstants.ENTRY_HEADER_DATA_BITS;
import static ch.qos.logback.core.kryo.KryoCoreConstants.ENTRY_HEADER_DATA_MASK;
import static ch.qos.logback.core.kryo.KryoCoreConstants.ENTRY_HEADER_ID_MASK;
import static ch.qos.logback.core.kryo.KryoCoreConstants.ENTRY_HEADER_NEW_FLAG;
import static ch.qos.logback.core.kryo.KryoCoreConstants.MAX_SERIALIZER_ID;
import static ch.qos.logback.core.kryo.KryoCoreConstants.UNREGISTERED_ID;

/**
 * Base Kryo serializer with optional value↔id caching.
 *
 * <p>When a bimap is configured, each value is introduced with a 2-byte header packing
 * a 13-bit data word (NEW/CACHED flag + 12-bit id) and a 3-bit CRC over those data bits.
 * Ids must be in {@code 1..MAX_SERIALIZER_ID}, or {@link KryoCoreConstants#UNREGISTERED_ID}
 * for frequency-gated non-registrations. Cached hits write only that header; new values also
 * write the payload via {@link #writeNewEntry}.
 *
 * @see KryoCoreConstants#ENTRY_HEADER_NEW_FLAG
 */
public abstract class SerializerBase<V> extends Serializer<V> {

    public enum BiMapType {
        NONE,
        DIRECT_MAP,
        FREQUENCY_GATED,
        /** Cache hits use {@code ==}, not {@code equals}/{@code hashCode}. */
        IDENTITY
    }


    final int capacity;
    final BidirectionalObjectToIdMap<V> bimap;

    public SerializerBase(int capacity) {

        this(BiMapType.DIRECT_MAP, capacity);
        if(capacity < 1024) {
            System.out.println("WARNING: capacity less than 1024");
        }
    }

    public SerializerBase(BiMapType biMapType, int capacity) {
        this.capacity = capacity;

        switch (biMapType) {
            case NONE:
                this.bimap = null;
                break;
            case DIRECT_MAP:
                this.bimap = new BidirectionalObjectToIdMapDefault<>(capacity);
                break;
            case FREQUENCY_GATED:
                this.bimap = new BidirectionalObjectToIdMapDefault(capacity); //FrequencyGatedBidirectionalObjectToIdMap<>(capacity);
                break;
            case IDENTITY:
                this.bimap = new BidirectionalObjectToIdMapIdentity<>(capacity);
                break;
            default:
                throw new IllegalArgumentException("Unsupported BiMapType: " + biMapType);
        }
        Log.debug(this.getClass().getSimpleName() + " bimap:"+biMapType+" capacity: " + capacity);
    }


    @Override
    public void write(Kryo kryo, Output output, V value) {

        // no bimap, just write the value directly
        if (bimap == null) {
            writeNewEntry(kryo, output, value);
            return;
        }

        Integer existingId = this.bimap.getIdByValue(value);

        if (existingId == null) {
            final int newId = this.bimap.insertNewEntryOnWrite(value);
            writeEntryHeader(output, true, newId);
            //Log.debug("SerializerBase.write writing new value [" + value + "] with id: " + newId);
            writeNewEntry(kryo, output, value);
        } else {
            //Log.debug("SerializerBase.write writing existing entry [" + value + "] with id: " + existingId);
            writeEntryHeader(output, false, existingId);
        }

    }

    /**
     * Method called to write new entries.
     *
     * @param kryo
     * @param output
     * @param value
     */
    abstract protected void writeNewEntry(Kryo kryo, Output output, V value);

    @Override
    public V read(Kryo kryo, Input input, Class<? extends V> type) {

        if (bimap == null) {
            return readNewEntry(kryo, input);
        }

        int data13 = readEntryHeaderData(input);
        boolean isNewEntry = (data13 & ENTRY_HEADER_NEW_FLAG) != 0;
        int id = decodeHeaderId(data13);

        if (isNewEntry) {
            V deserialized = readNewEntry(kryo, input);
            if (id != UNREGISTERED_ID) {
                V existingVal = bimap.insertNewEntryOnRead(id, deserialized);
                if (existingVal != null) {
                    Log.warn("SerializerBase.read returned non-null existingVal for value id: " + id);
                }
            }
            Log.debug("NEW entry for id: " + id + ", value: " + deserialized);
            return deserialized;
        } else {
            V value = bimap.getValueById(id);
            Log.debug("Cached entry for id: " + id + ", value: " + value);
            return value;
        }
    }

    abstract protected V readNewEntry(Kryo kryo, Input input);

    /**
     * Writes a 2-byte entry header: 13-bit data (NEW flag + id) and a 3-bit CRC over that data.
     * {@link KryoCoreConstants#UNREGISTERED_ID} is stored as id 0 (valid only with NEW).
     */
    static void writeEntryHeader(Output output, boolean newEntry, int id) {
        int idBits;
        if (id == UNREGISTERED_ID) {
            if (!newEntry) {
                throw new IllegalArgumentException("UNREGISTERED_ID is only valid for NEW_ENTRY");
            }
            idBits = 0;
        } else {
            if (id < 1 || id > MAX_SERIALIZER_ID) {
                throw new IllegalStateException(
                        "Serializer id out of range: " + id + " (allowed 1.." + MAX_SERIALIZER_ID + ")");
            }
            idBits = id;
        }
        int data13 = (newEntry ? ENTRY_HEADER_NEW_FLAG : 0) | (idBits & ENTRY_HEADER_ID_MASK);
        int crc = crc3(data13);
        int packed = (data13 << ENTRY_HEADER_CRC_BITS) | crc;
        output.writeShort(packed);
    }

    /**
     * Reads a 2-byte entry header, verifies the CRC, and returns the 13-bit data word.
     *
     * @throws KryoException if the CRC does not match
     */
    static int readEntryHeaderData(Input input) {
        int packed = input.readShort() & 0xFFFF;
        int data13 = (packed >>> ENTRY_HEADER_CRC_BITS) & ENTRY_HEADER_DATA_MASK;
        int crc = packed & ENTRY_HEADER_CRC_MASK;
        int expected = crc3(data13);
        if (crc != expected) {
            throw new KryoException(
                    "SerializerBase entry header CRC mismatch: got " + crc + ", expected " + expected
                            + " (data=0x" + Integer.toHexString(data13) + ")");
        }
        return data13;
    }

    /**
     * Decodes the id from a verified 13-bit data word.
     * Id bits {@code 0} on a NEW entry mean {@link KryoCoreConstants#UNREGISTERED_ID}.
     */
    static int decodeHeaderId(int data13) {
        int idBits = data13 & ENTRY_HEADER_ID_MASK;
        boolean isNew = (data13 & ENTRY_HEADER_NEW_FLAG) != 0;
        if (isNew && idBits == 0) {
            return UNREGISTERED_ID;
        }
        return idBits;
    }

    /**
     * CRC-3 over the low {@link KryoCoreConstants#ENTRY_HEADER_DATA_BITS} of {@code data},
     * polynomial {@link KryoCoreConstants#ENTRY_HEADER_CRC3_POLY} ({@code x^3 + x + 1}).
     *
     * <p>MSB-first residual: remainder of {@code (data << 3)} modulo the generator.
     */
    static int crc3(int data) {
        int v = (data & ENTRY_HEADER_DATA_MASK) << ENTRY_HEADER_CRC_BITS;
        for (int i = ENTRY_HEADER_DATA_BITS + ENTRY_HEADER_CRC_BITS - 1; i >= ENTRY_HEADER_CRC_BITS; i--) {
            if (((v >> i) & 1) != 0) {
                v ^= ENTRY_HEADER_CRC3_POLY << (i - ENTRY_HEADER_CRC_BITS);
            }
        }
        return v & ENTRY_HEADER_CRC_MASK;
    }
}
