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
package ch.qos.logback.classic.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CallerDataThrowableTest {

    static final int DEPTH = 8;

    // Stands in for a facade such as org.slf4j.MDC, which forwards to Inner.
    static class Outer {
        static CallerDataThrowable make(String... fqnsToShave) {
            return Inner.make(fqnsToShave);
        }
    }

    // Stands in for the adapter that creates the throwable.
    static class Inner {
        static CallerDataThrowable make(String... fqnsToShave) {
            return new CallerDataThrowable(fqnsToShave, DEPTH);
        }

        static CallerDataThrowable makeViaOuterCallback(String... fqnsToShave) {
            return OuterCallback.make(fqnsToShave);
        }
    }

    static class OuterCallback {
        static CallerDataThrowable make(String... fqnsToShave) {
            return Inner.make(fqnsToShave);
        }
    }

    @Test
    public void allDesignatedClassesAreShaved() {
        CallerDataThrowable cdt = Outer.make(Inner.class.getName(), Outer.class.getName());
        StackTraceElement[] stack = cdt.getStackTrace();
        Assertions.assertEquals(CallerDataThrowableTest.class.getName(), stack[0].getClassName());
        Assertions.assertEquals("allDesignatedClassesAreShaved", stack[0].getMethodName());
    }

    @Test
    public void shavingStopsAtFirstUndesignatedFrame() {
        // Only Outer is designated but Inner is on top, so nothing is shaved.
        CallerDataThrowable cdt = Outer.make(Outer.class.getName());
        StackTraceElement[] stack = cdt.getStackTrace();
        Assertions.assertEquals(Inner.class.getName(), stack[0].getClassName());
        Assertions.assertEquals(Outer.class.getName(), stack[1].getClassName());
    }

    @Test
    public void laterFramesOfDesignatedClassesAreKept() {
        // Stack: Inner.make, OuterCallback.make, Inner.makeViaOuterCallback, test method.
        CallerDataThrowable cdt = Inner.makeViaOuterCallback(Inner.class.getName());
        StackTraceElement[] stack = cdt.getStackTrace();
        Assertions.assertEquals(OuterCallback.class.getName(), stack[0].getClassName());
        Assertions.assertEquals(Inner.class.getName(), stack[1].getClassName());
        Assertions.assertEquals("makeViaOuterCallback", stack[1].getMethodName());
    }

    @Test
    public void stackIsCappedAtMaxDepth() {
        CallerDataThrowable cdt = Outer.make(Inner.class.getName(), Outer.class.getName());
        Assertions.assertTrue(cdt.getStackTrace().length <= DEPTH);
    }

    @Test
    public void ownFramesAreNeverKept() {
        CallerDataThrowable cdt = Outer.make();
        for (StackTraceElement frame : cdt.getStackTrace()) {
            Assertions.assertNotEquals(CallerDataThrowable.class.getName(), frame.getClassName());
        }
        Assertions.assertEquals(Inner.class.getName(), cdt.getStackTrace()[0].getClassName());
    }
}
