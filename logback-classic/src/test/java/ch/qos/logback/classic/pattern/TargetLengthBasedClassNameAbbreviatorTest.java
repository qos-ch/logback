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
package ch.qos.logback.classic.pattern;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.qos.logback.classic.pattern.TargetLengthBasedClassNameAbbreviator;

public class TargetLengthBasedClassNameAbbreviatorTest {

    @Test
    public void testShortName() {
        {
            TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(100);
            StringBuilder sb = new StringBuilder();

            String name = "hello";
            abbreviator.abbreviate(name, sb);
            assertEquals(name, sb.toString());
        }
        {
            TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(100);
            StringBuilder sb = new StringBuilder();
            String name = "hello.world";
            abbreviator.abbreviate(name, sb);
            assertEquals(name, sb.toString());
        }
    }

    @Test
    public void testNoDot() {
        TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
        StringBuilder sb = new StringBuilder();
        String name = "hello";
        abbreviator.abbreviate(name, sb);
        assertEquals(name, sb.toString());
    }

    @Test
    public void testOneDot() {
        {
            TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
            StringBuilder sb = new StringBuilder();
            String name = "hello.world";
            abbreviator.abbreviate(name, sb);
            assertEquals("h.world", sb.toString());
        }

        {
            TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
            StringBuilder sb = new StringBuilder();
            String name = "h.world";
            abbreviator.abbreviate(name, sb);
            assertEquals("h.world", sb.toString());
        }

        {
            TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
            StringBuilder sb = new StringBuilder();
            String name = ".world";
            abbreviator.abbreviate(name, sb);
            assertEquals(".world", sb.toString());
        }
    }

    @Test
    public void testTwoDot() {
        {
            TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
            StringBuilder sb = new StringBuilder();
            String name = "com.logback.Foobar";
            abbreviator.abbreviate(name, sb);
            assertEquals("c.l.Foobar", sb.toString());
        }

        {
            TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
            StringBuilder sb = new StringBuilder();
            String name = "c.logback.Foobar";
            abbreviator.abbreviate(name, sb);
            assertEquals("c.l.Foobar", sb.toString());
        }

        {
            TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
            StringBuilder sb = new StringBuilder();
            String name = "c..Foobar";
            abbreviator.abbreviate(name, sb);
            assertEquals("c..Foobar", sb.toString());
        }
        {
            TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
            StringBuilder sb = new StringBuilder();
            String name = "..Foobar";
            abbreviator.abbreviate(name, sb);
            assertEquals("..Foobar", sb.toString());
        }
    }

    @Test
    public void test3Dot() {
        {
            TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
            StringBuilder sb = new StringBuilder();
            String name = "com.logback.xyz.Foobar";
            abbreviator.abbreviate(name, sb);
            assertEquals("c.l.x.Foobar", sb.toString());
        }
        {
            TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(13);
            StringBuilder sb = new StringBuilder();
            String name = "com.logback.xyz.Foobar";
            abbreviator.abbreviate(name, sb);
            assertEquals("c.l.x.Foobar", sb.toString());
        }
        {
            TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(14);
            StringBuilder sb = new StringBuilder();
            String name = "com.logback.xyz.Foobar";
            abbreviator.abbreviate(name, sb);
            assertEquals("c.l.xyz.Foobar", sb.toString());
        }

        {
            TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(15);
            StringBuilder sb = new StringBuilder();
            String name = "com.logback.alligator.Foobar";
            abbreviator.abbreviate(name, sb);
            assertEquals("c.l.a.Foobar", sb.toString());
        }
    }

    @Test
    public void testXDot() {
        {
            TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(21);
            StringBuilder sb = new StringBuilder();
            String name = "com.logback.wombat.alligator.Foobar";
            abbreviator.abbreviate(name, sb);
            assertEquals("c.l.w.a.Foobar", sb.toString());
        }

        {
            TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(22);
            StringBuilder sb = new StringBuilder();
            String name = "com.logback.wombat.alligator.Foobar";
            abbreviator.abbreviate(name, sb);
            assertEquals("c.l.w.alligator.Foobar", sb.toString());
        }

        {
            TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
            StringBuilder sb = new StringBuilder();
            String name = "com.logback.wombat.alligator.tomato.Foobar";
            abbreviator.abbreviate(name, sb);
            assertEquals("c.l.w.a.t.Foobar", sb.toString());
        }

        {
            TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(21);
            StringBuilder sb = new StringBuilder();
            String name = "com.logback.wombat.alligator.tomato.Foobar";
            abbreviator.abbreviate(name, sb);
            assertEquals("c.l.w.a.tomato.Foobar", sb.toString());
        }

        {
            TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(29);
            StringBuilder sb = new StringBuilder();
            String name = "com.logback.wombat.alligator.tomato.Foobar";
            abbreviator.abbreviate(name, sb);
            assertEquals("c.l.w.alligator.tomato.Foobar", sb.toString());
        }
    }
}
