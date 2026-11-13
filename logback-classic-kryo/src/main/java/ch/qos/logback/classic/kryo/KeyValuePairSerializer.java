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

import ch.qos.logback.core.kryo.SerializerBase;
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import org.slf4j.event.KeyValuePair;

public class KeyValuePairSerializer extends SerializerBase<KeyValuePair> {


    public KeyValuePairSerializer() {
        super(BiMapType.NONE, 0);
    }

    @Override
    protected void writeNewEntry(Kryo kryo, Output output, KeyValuePair keyValuePair) {
        output.writeString(keyValuePair.key);
        Object nonNullAllowedObject = SafeObjectTransformer.asAllowedNonNullObject(keyValuePair.value);
        kryo.writeClassAndObject(output, nonNullAllowedObject);
    }


    @Override
    protected KeyValuePair readNewEntry(Kryo kryo, Input input) {
        String key = input.readString();
        //System.out.println("Deserializing KeyValuePair with key: " + key);
        Object value = kryo.readClassAndObject(input);
        // a null value is written as NULL_PROXY
        if (SafeObjectTransformer.NULL_PROXY.equals(value)) {
            value = null;
        }
        return new KeyValuePair(key, value);
    }
}
