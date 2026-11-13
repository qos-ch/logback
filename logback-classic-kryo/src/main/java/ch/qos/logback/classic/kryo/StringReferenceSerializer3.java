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


import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.ReferenceResolver;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

/**
 * Custom Kryo 5 serializer for java.lang.String that uses ReferenceResolver
 * to emit compact reference ids for repeated String instances.
 */
public class StringReferenceSerializer3 extends Serializer<String> {

    public StringReferenceSerializer3() {
        setAcceptsNull(true);
    }


    @Override
    public void write(Kryo kryo, Output output, String obj) {
        // null flag
        if (obj == null) {
            output.writeBoolean(true);
            return;
        } else {
            output.writeBoolean(false);
        }

        ReferenceResolver referenceResolver = kryo.getReferenceResolver();
        int writtenId = referenceResolver.getWrittenId(obj); // -1 if not written yet

        System.out.println("Writing String: \"" + obj + "\", writtenId=" + writtenId);

        // already written: write reference id + 1 (non-zero marker)
        if (writtenId != -1) {
            // write reference id + 1 (non-zero marker)
            output.writeVarInt(writtenId + 1, true);
            return;
        }

        // first occurrence: marker 0, then register and write string bytes
        output.writeVarInt(0, true);

        int id = referenceResolver.addWrittenObject(obj);
        System.out.println("------------------");
        System.out.println("Registering new String with id=" + id);
        output.writeInt(id);
        output.writeString(obj);
    }

    @Override
    public String read(Kryo kryo, Input input, Class<? extends String> type) {
        // null flag
        boolean isNull = input.readBoolean();
        if (isNull) {
            System.out.println("Read null String");
            return null;
        }

        int marker = input.readVarInt(true);
        if (marker != 0) {
            // existing instance reference id was marker - 1
            ReferenceResolver refs = kryo.getReferenceResolver();
            Object existing = refs.getReadObject(String.class, marker - 1);
            //noinspection RedundantCast
            return (String) existing;
        }

        // first-time string: read, register and return
        ReferenceResolver refs = kryo.getReferenceResolver();
        int id = refs.nextReadId(String.class);
        String s = input.readString();
        refs.setReadObject(id, s);
        return s;
    }
}

