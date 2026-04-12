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
package ch.qos.logback.classic.scoped;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ScopedMDCTest {

    @Test
    public void unboundReturnsNullAndEmptyMap() {
        assertNull(ScopedMDC.get("any"));
        assertTrue(ScopedMDC.getPropertyMap().isEmpty());
    }

    @Test
    public void putAndGetWithinScope() {
        ScopedMDC.put("key1", "value1").run(() -> {
            assertEquals("value1", ScopedMDC.get("key1"));
            assertNull(ScopedMDC.get("missing"));
        });
    }

    @Test
    public void getPropertyMapReturnsAllEntries() {
        ScopedMDC.put("a", "1").put("b", "2").run(() -> {
            Map<String, String> map = ScopedMDC.getPropertyMap();
            assertEquals(2, map.size());
            assertEquals("1", map.get("a"));
            assertEquals("2", map.get("b"));
        });
    }

    @Test
    public void nestedScopeInheritsParentValues() {
        ScopedMDC.put("parent", "pval").run(() -> {
            ScopedMDC.put("child", "cval").run(() -> {
                assertEquals("pval", ScopedMDC.get("parent"));
                assertEquals("cval", ScopedMDC.get("child"));
            });
        });
    }

    @Test
    public void nestedScopeCanOverrideParentValue() {
        ScopedMDC.put("key", "original").run(() -> {
            assertEquals("original", ScopedMDC.get("key"));

            ScopedMDC.put("key", "overridden").run(() -> {
                assertEquals("overridden", ScopedMDC.get("key"));
            });

            assertEquals("original", ScopedMDC.get("key"));
        });
    }

    @Test
    public void parentScopeUnaffectedAfterNestedScopeExits() {
        ScopedMDC.put("parent", "pval").run(() -> {
            ScopedMDC.put("child", "cval").run(() -> {
                // child scope active
            });
            assertNull(ScopedMDC.get("child"));
            assertEquals("pval", ScopedMDC.get("parent"));
        });
    }

    @Test
    public void putChainingOnBinding() {
        ScopedMDC.put("a", "1")
                 .put("b", "2")
                 .put("c", "3")
                 .run(() -> {
                     assertEquals("1", ScopedMDC.get("a"));
                     assertEquals("2", ScopedMDC.get("b"));
                     assertEquals("3", ScopedMDC.get("c"));
                 });
    }

    @Test
    public void putAllMergesWithCurrentScope() {
        ScopedMDC.put("existing", "val").run(() -> {
            ScopedMDC.putAll(Map.of("new1", "v1", "new2", "v2")).run(() -> {
                assertEquals("val", ScopedMDC.get("existing"));
                assertEquals("v1", ScopedMDC.get("new1"));
                assertEquals("v2", ScopedMDC.get("new2"));
            });
        });
    }

    @Test
    public void callReturnsValue() throws Exception {
        String result = ScopedMDC.put("key", "value").call(() -> {
            assertEquals("value", ScopedMDC.get("key"));
            return "result";
        });
        assertEquals("result", result);
    }

    @Test
    public void callPropagatesException() {
        assertThrows(IllegalStateException.class, () ->
            ScopedMDC.put("key", "value").call(() -> {
                throw new IllegalStateException("test");
            })
        );
    }

    @Test
    public void scopedValuesNotVisibleOutsideScope() {
        ScopedMDC.put("key", "value").run(() -> {
            assertEquals("value", ScopedMDC.get("key"));
        });
        assertNull(ScopedMDC.get("key"));
        assertTrue(ScopedMDC.getPropertyMap().isEmpty());
    }

    @Test
    public void propertyMapIsUnmodifiable() {
        ScopedMDC.put("key", "value").run(() -> {
            Map<String, String> map = ScopedMDC.getPropertyMap();
            assertThrows(UnsupportedOperationException.class, () -> map.put("new", "val"));
        });
    }

    @Test
    public void childThreadInheritsScopedValuesViaStructuredTaskScope() throws Exception {
        AtomicReference<String> captured = new AtomicReference<>();

        ScopedMDC.put("requestId", "abc-123").run(() -> {
            try (var scope = StructuredTaskScope.open()) {
                scope.fork(() -> {
                    captured.set(ScopedMDC.get("requestId"));
                    return null;
                });
                scope.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertEquals("abc-123", captured.get());
    }
}
