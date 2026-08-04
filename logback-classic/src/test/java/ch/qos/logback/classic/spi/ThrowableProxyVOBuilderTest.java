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
package ch.qos.logback.classic.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThrowableProxyVOBuilderTest {

    @Test
    public void emptyBuildHasNullDefaults() {
        ThrowableProxyVO vo = ThrowableProxyVO.builder().build();

        assertNull(vo.getClassName());
        assertNull(vo.getMessage());
        assertNull(vo.getOverridingMessage());
        assertEquals(0, vo.getCommonFrames());
        assertNull(vo.getStackTraceElementProxyArray());
        assertNull(vo.getCause());
        assertNull(vo.getSuppressed());
        assertFalse(vo.isCyclic());
    }

    @Test
    public void setsFieldsOneByOne() {
        StackTraceElement ste = new StackTraceElement("com.example.Foo", "bar", "Foo.java", 42);
        StackTraceElementProxy[] steps = new StackTraceElementProxy[] { new StackTraceElementProxy(ste) };

        ThrowableProxyVO cause = ThrowableProxyVO.builder().setClassName("java.lang.Exception")
                .setMessage("root cause").build();

        ThrowableProxyVO suppressed = ThrowableProxyVO.builder()
                .setClassName("java.lang.IllegalStateException").setMessage("suppressed").build();

        ThrowableProxyVO vo = new ThrowableProxyVOBuilder().setClassName("java.lang.RuntimeException")
                .setMessage("boom").setOverridingMessage("overridden toString")
                .setStackTraceElementProxyArray(steps).setCommonFramesCount(1).setCause(cause)
                .setSuppressed(new IThrowableProxy[] { suppressed }).setCyclic(true).build();

        assertEquals("java.lang.RuntimeException", vo.getClassName());
        assertEquals("boom", vo.getMessage());
        assertEquals("overridden toString", vo.getOverridingMessage());
        assertEquals(1, vo.getCommonFrames());
        assertEquals(1, vo.getStackTraceElementProxyArray().length);
        assertEquals("com.example.Foo",
                vo.getStackTraceElementProxyArray()[0].getStackTraceElement().getClassName());
        assertSame(cause, vo.getCause());
        assertEquals(1, vo.getSuppressed().length);
        assertSame(suppressed, vo.getSuppressed()[0]);
        assertTrue(vo.isCyclic());
    }

    @Test
    public void buildFromIThrowableProxyStillWorks() {
        ThrowableProxyVO original = ThrowableProxyVO.builder().setClassName("java.lang.IllegalArgumentException")
                .setMessage("bad arg").setCommonFramesCount(2).setCyclic(false).build();

        ThrowableProxyVO copy = ThrowableProxyVO.build(original);

        assertEquals(original.getClassName(), copy.getClassName());
        assertEquals(original.getMessage(), copy.getMessage());
        assertEquals(original.getCommonFrames(), copy.getCommonFrames());
        assertEquals(original, copy);
    }
}
