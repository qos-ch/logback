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
package ch.qos.logback.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class MinimalUnmodifiableMapTest {

    @Test
    public void getReturnsCopiedValues() {
        Map<String, String> source = new HashMap<>();
        source.put("k0", "v0");
        source.put("k1", "v1");

        Map<String, String> map = new MinimalUnmodifiableMap(source);
        assertEquals("v0", map.get("k0"));
        assertEquals("v1", map.get("k1"));
        assertNull(map.get("missing"));
        assertEquals(2, map.size());
    }

    @Test
    public void snapshotIsIndependentOfSource() {
        Map<String, String> source = new HashMap<>();
        source.put("k0", "v0");
        Map<String, String> map = new MinimalUnmodifiableMap(source);

        source.put("k0", "changed");
        source.put("k1", "v1");

        assertEquals("v0", map.get("k0"));
        assertNull(map.get("k1"));
        assertEquals(1, map.size());
    }

    @Test
    public void entrySetIteratesKeyValuePairs() {
        Map<String, String> source = new HashMap<>();
        source.put("k0", "v0");
        source.put("k1", "v1");
        Map<String, String> map = new MinimalUnmodifiableMap(source);

        Map<String, String> fromEntries = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            fromEntries.put(entry.getKey(), entry.getValue());
        }
        assertEquals(source, fromEntries);
    }

    @Test
    public void mutationsAreRejected() {
        Map<String, String> map = new MinimalUnmodifiableMap(Map.of("k", "v"));
        assertThrows(UnsupportedOperationException.class, () -> map.put("k2", "v2"));
        assertThrows(UnsupportedOperationException.class, () -> map.remove("k"));
        assertThrows(UnsupportedOperationException.class, map::clear);
        assertThrows(UnsupportedOperationException.class, () -> map.entrySet().add(null));
        assertThrows(UnsupportedOperationException.class, () -> map.entrySet().iterator().remove());
        assertThrows(UnsupportedOperationException.class, () -> map.entrySet().iterator().next().setValue("x"));
    }

    @Test
    public void equalsHashMapWithSameEntries() {
        Map<String, String> source = new HashMap<>();
        source.put("k0", "v0");
        source.put("k1", "v1");
        Map<String, String> map = new MinimalUnmodifiableMap(source);
        assertEquals(source, map);
        assertEquals(map, source);
        assertEquals(source.hashCode(), map.hashCode());
    }

    @Test
    public void emptyMap() {
        Map<String, String> map = MinimalUnmodifiableMap.emptyMap();
        assertTrue(map.isEmpty());
        assertFalse(map.entrySet().iterator().hasNext());
        assertNull(map.get("k"));
        assertSame(MinimalUnmodifiableMap.emptyMap(), map);
    }

    @Test
    public void serializationRoundTrip() throws Exception {
        Map<String, String> source = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            source.put("k" + i, "v" + i);
        }
        Map<?, ?> copy = (Map<?, ?>) deserialize(serialize(new MinimalUnmodifiableMap(source)));
        assertEquals(source, copy);
    }

    @Test
    public void oversizedSerializedCountIsNotPreallocated() throws Exception {
        // 1 entry on the wire, but the declared count claims Integer.MAX_VALUE - 8
        byte[] payload = withSerializedCount(Integer.MAX_VALUE - 8);
        assertThrows(IOException.class, () -> deserialize(payload));
    }

    @Test
    public void negativeSerializedCountIsRejected() throws Exception {
        byte[] payload = withSerializedCount(-1);
        assertThrows(InvalidObjectException.class, () -> deserialize(payload));
    }

    private static byte[] withSerializedCount(int count) throws IOException {
        Map<String, String> source = new HashMap<>();
        source.put("k", "v");
        byte[] bytes = serialize(new MinimalUnmodifiableMap(source));
        // the count written by writeObject is the int 1 immediately followed by TC_STRING
        for (int i = 0; i + 4 < bytes.length; i++) {
            if (bytes[i] == 0 && bytes[i + 1] == 0 && bytes[i + 2] == 0 && bytes[i + 3] == 1 && bytes[i + 4] == 0x74) {
                bytes[i] = (byte) (count >>> 24);
                bytes[i + 1] = (byte) (count >>> 16);
                bytes[i + 2] = (byte) (count >>> 8);
                bytes[i + 3] = (byte) count;
                return bytes;
            }
        }
        throw new IllegalStateException("serialized count not found");
    }

    private static byte[] serialize(Object o) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(o);
        }
        return bos.toByteArray();
    }

    private static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return ois.readObject();
        }
    }
}
