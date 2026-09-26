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
        static CallerDataComputingException make(String... fqnsToShave) {
            return Inner.make(fqnsToShave);
        }
    }

    // Stands in for the adapter that creates the throwable.
    static class Inner {
        static CallerDataComputingException make(String... fqnsToShave) {
            return new CallerDataComputingException(fqnsToShave, DEPTH);
        }

        static CallerDataComputingException makeViaOuterCallback(String... fqnsToShave) {
            return OuterCallback.make(fqnsToShave);
        }
    }

    static class OuterCallback {
        static CallerDataComputingException make(String... fqnsToShave) {
            return Inner.make(fqnsToShave);
        }
    }

    @Test
    public void allDesignatedClassesAreShaved() {
        CallerDataComputingException cdt = Outer.make(Inner.class.getName(), Outer.class.getName());
        StackTraceElement[] stack = cdt.getStackTrace();
        Assertions.assertEquals(CallerDataThrowableTest.class.getName(), stack[0].getClassName());
        Assertions.assertEquals("allDesignatedClassesAreShaved", stack[0].getMethodName());
    }

    @Test
    public void shavingStopsAtFirstUndesignatedFrame() {
        // Only Outer is designated but Inner is on top, so nothing is shaved.
        CallerDataComputingException cdt = Outer.make(Outer.class.getName());
        StackTraceElement[] stack = cdt.getStackTrace();
        Assertions.assertEquals(Inner.class.getName(), stack[0].getClassName());
        Assertions.assertEquals(Outer.class.getName(), stack[1].getClassName());
    }

    @Test
    public void laterFramesOfDesignatedClassesAreKept() {
        // Stack: Inner.make, OuterCallback.make, Inner.makeViaOuterCallback, test method.
        CallerDataComputingException cdt = Inner.makeViaOuterCallback(Inner.class.getName());
        StackTraceElement[] stack = cdt.getStackTrace();
        Assertions.assertEquals(OuterCallback.class.getName(), stack[0].getClassName());
        Assertions.assertEquals(Inner.class.getName(), stack[1].getClassName());
        Assertions.assertEquals("makeViaOuterCallback", stack[1].getMethodName());
    }

    @Test
    public void stackIsCappedAtMaxDepth() {
        CallerDataComputingException cdt = Outer.make(Inner.class.getName(), Outer.class.getName());
        Assertions.assertTrue(cdt.getStackTrace().length <= DEPTH);
    }

    @Test
    public void toStringNamesTheFirstRemainingFrame() {
        CallerDataComputingException cdt = Outer.make(Inner.class.getName(), Outer.class.getName());
        Assertions.assertEquals(cdt.getStackTrace()[0].toString(), cdt.toString());
        Assertions.assertFalse(cdt.toString().startsWith(CallerDataComputingException.class.getName()));
    }

    @Test
    public void ownFramesAreNeverKept() {
        CallerDataComputingException cdt = Outer.make();
        for (StackTraceElement frame : cdt.getStackTrace()) {
            Assertions.assertNotEquals(CallerDataComputingException.class.getName(), frame.getClassName());
        }
        Assertions.assertEquals(Inner.class.getName(), cdt.getStackTrace()[0].getClassName());
    }
}
