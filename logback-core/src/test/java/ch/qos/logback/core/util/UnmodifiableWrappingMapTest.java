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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class UnmodifiableWrappingMapTest {

    @Test
    public void getDelegatesToBackingMap() {
        Map<String, String> source = new HashMap<>();
        source.put("k0", "v0");
        source.put("k1", "v1");

        Map<String, String> map = new UnmodifiableWrappingMap<>(source);
        assertEquals("v0", map.get("k0"));
        assertEquals("v1", map.get("k1"));
        assertNull(map.get("missing"));
        assertEquals(2, map.size());
    }

    @Test
    public void viewsBackingMapWithoutCopying() {
        Map<String, String> source = new HashMap<>();
        source.put("k0", "v0");
        Map<String, String> map = new UnmodifiableWrappingMap<>(source);

        source.put("k0", "changed");
        source.put("k1", "v1");

        assertEquals("changed", map.get("k0"));
        assertEquals("v1", map.get("k1"));
        assertEquals(2, map.size());
    }

    @Test
    public void entrySetIteratesKeyValuePairs() {
        Map<String, Integer> source = new HashMap<>();
        source.put("k0", 0);
        source.put("k1", 1);
        Map<String, Integer> map = new UnmodifiableWrappingMap<>(source);

        Map<String, Integer> fromEntries = new HashMap<>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            fromEntries.put(entry.getKey(), entry.getValue());
        }
        assertEquals(source, fromEntries);
    }

    @Test
    public void mutationsAreRejected() {
        Map<String, String> map = new UnmodifiableWrappingMap<>(Map.of("k", "v"));
        assertThrows(UnsupportedOperationException.class, () -> map.put("k2", "v2"));
        assertThrows(UnsupportedOperationException.class, () -> map.remove("k"));
        assertThrows(UnsupportedOperationException.class, map::clear);
        assertThrows(UnsupportedOperationException.class, () -> map.entrySet().add(null));
        assertThrows(UnsupportedOperationException.class, () -> map.entrySet().iterator().remove());
        assertThrows(UnsupportedOperationException.class, () -> map.entrySet().iterator().next().setValue("x"));
    }

    @Test
    public void equalsHashMapWithSameEntries() {
        Map<Integer, String> source = new HashMap<>();
        source.put(0, "v0");
        source.put(1, "v1");
        Map<Integer, String> map = new UnmodifiableWrappingMap<>(source);
        assertEquals(source, map);
        assertEquals(map, source);
        assertEquals(source.hashCode(), map.hashCode());
    }

    @Test
    public void emptyMap() {
        Map<String, String> map = new UnmodifiableWrappingMap<>(new HashMap<>());
        assertTrue(map.isEmpty());
        assertFalse(map.entrySet().iterator().hasNext());
        assertNull(map.get("k"));
    }
}
