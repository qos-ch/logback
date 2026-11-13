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

public class KeyValuePairCollectionSerializerV4 extends CollectionSerializer<ArrayList<KeyValuePair>> {

    private final KeyValuePairSerializerV4 keyValuePairSerializer;

    public KeyValuePairCollectionSerializerV4(int stringCapacity) {
        this(new KeyValuePairSerializerV4(stringCapacity));
    }

    public KeyValuePairCollectionSerializerV4(KeyValuePairSerializerV4 keyValuePairSerializer) {
        super();
        if (keyValuePairSerializer == null) {
            throw new IllegalArgumentException("KeyValuePairSerializer cannot be null");
        }
        this.keyValuePairSerializer = keyValuePairSerializer;
        setElementClass(KeyValuePair.class, keyValuePairSerializer);
        setElementsCanBeNull(true);
    }

    public KeyValuePairSerializerV4 getElementSerializerV4() {
        return keyValuePairSerializer;
    }
}
