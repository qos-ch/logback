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

public class BasicStringSerializer  extends SerializerBase<String> {

    public BasicStringSerializer(int capacity) {
        super(capacity);
    }

    public BasicStringSerializer(SerializerBase.BiMapType biMapType, int capacity) {
        super(biMapType, capacity);
    }

    @Override
    protected void writeNewEntry(Kryo kryo, com.esotericsoftware.kryo.kryo5.io.Output output, String value) {
        output.writeString(value);
    }

    @Override
    protected String readNewEntry(Kryo kryo, com.esotericsoftware.kryo.kryo5.io.Input input) {
        return input.readString();
    }
}
