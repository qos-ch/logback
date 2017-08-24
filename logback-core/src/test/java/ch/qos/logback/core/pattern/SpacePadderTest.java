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
package ch.qos.logback.core.pattern;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SpacePadderTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void smoke() {
        {
            StringBuilder buf = new StringBuilder();
            String s = "a";
            SpacePadder.leftPad(buf, s, 4);
            assertEquals("   a", buf.toString());
        }
        {
            StringBuilder buf = new StringBuilder();
            String s = "a";
            SpacePadder.rightPad(buf, s, 4);
            assertEquals("a   ", buf.toString());
        }
    }

    @Test
    public void nullString() {
        String s = null;
        {
            StringBuilder buf = new StringBuilder();
            SpacePadder.leftPad(buf, s, 2);
            assertEquals("  ", buf.toString());
        }
        {
            StringBuilder buf = new StringBuilder();
            SpacePadder.rightPad(buf, s, 2);
            assertEquals("  ", buf.toString());
        }
    }

    @Test
    public void longString() {
        {
            StringBuilder buf = new StringBuilder();
            String s = "abc";
            SpacePadder.leftPad(buf, s, 2);
            assertEquals(s, buf.toString());
        }

        {
            StringBuilder buf = new StringBuilder();
            String s = "abc";
            SpacePadder.rightPad(buf, s, 2);
            assertEquals(s, buf.toString());
        }
    }

    @Test
    public void lengthyPad() {
        {
            StringBuilder buf = new StringBuilder();
            String s = "abc";
            SpacePadder.leftPad(buf, s, 33);
            assertEquals("                              abc", buf.toString());
        }
        {
            StringBuilder buf = new StringBuilder();
            String s = "abc";
            SpacePadder.rightPad(buf, s, 33);
            assertEquals("abc                              ", buf.toString());
        }

    }

}
