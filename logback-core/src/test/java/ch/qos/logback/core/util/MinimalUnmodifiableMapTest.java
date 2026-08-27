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
}
