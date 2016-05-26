/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.control;

import junit.framework.TestCase;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.control.ControlLogger;
import ch.qos.logback.classic.control.ControlLoggerContext;

/**
 * This class is for testing ControlLoggerContext which is a control class for testing HLoggerContext.
 */
public class CLCTest extends TestCase {
    ControlLoggerContext clc;

    protected void setUp() throws Exception {
        clc = new ControlLoggerContext();
    }

    public void test1() {
        ControlLogger x = clc.getLogger("x");
        assertEquals("x", x.getName());
        assertEquals(clc.getRootLogger(), x.parent);

        ControlLogger abc = clc.getLogger("a.b.c");
        assertEquals("a.b.c", abc.getName());
        assertEquals(Level.DEBUG, abc.getEffectiveLevel());
    }

    public void testCreation() {
        ControlLogger xyz = clc.getLogger("x.y.z");
        assertEquals("x.y.z", xyz.getName());
        assertEquals("x.y", xyz.parent.getName());
        assertEquals("x", xyz.parent.parent.getName());
        assertEquals("root", xyz.parent.parent.parent.getName());

        ControlLogger xyz_ = clc.exists("x.y.z");
        assertEquals("x.y.z", xyz_.getName());

    }
}