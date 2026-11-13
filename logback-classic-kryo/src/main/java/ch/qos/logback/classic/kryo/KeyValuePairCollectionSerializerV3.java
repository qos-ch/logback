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

import com.esotericsoftware.kryo.kryo5.serializers.CollectionSerializer;
import org.slf4j.event.KeyValuePair;

import java.util.ArrayList;

/**
 * Collection serializer for {@code ArrayList<KeyValuePair>} used by stream V3.
 *
 * <p>All pairs share one {@link KeyValuePairSerializerV3} (and thus one
 * {@link SingleItemCompressingStringSerializer} for keys and string values) for the lifetime of this
 * instance / Kryo stream.
 */
public class KeyValuePairCollectionSerializerV3 extends CollectionSerializer<ArrayList<KeyValuePair>> {

    private final KeyValuePairSerializerV3 KeyValuePairSerializer;


    /**
     * @param stringCapacity capacity of the shared string cache for KVP keys and string values
     */
    public KeyValuePairCollectionSerializerV3(int stringCapacity) {
        this(new KeyValuePairSerializerV3(stringCapacity), false);
    }


    public KeyValuePairCollectionSerializerV3(KeyValuePairSerializerV3 KeyValuePairSerializer) {
        this(KeyValuePairSerializer, false);
    }

    public KeyValuePairCollectionSerializerV3(KeyValuePairSerializerV3 KeyValuePairSerializer, boolean elementsCanBeNull) {
        super();
        if (KeyValuePairSerializer == null) {
            throw new IllegalArgumentException("KeyValuePairSerializer cannot be null");
        }
        this.KeyValuePairSerializer = KeyValuePairSerializer;
        setElementClass(KeyValuePair.class, KeyValuePairSerializer);
        setElementsCanBeNull(elementsCanBeNull);
    }

    public KeyValuePairSerializerV3 getElementSerializerV3() {
        return KeyValuePairSerializer;
    }
}
