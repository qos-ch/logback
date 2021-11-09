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

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class TargetLengthBasedClassNameAbbreviatorTest {

    @Test
    public void testShortName() {
        {
            final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(100);
            final String name = "hello";
            assertEquals(name, abbreviator.abbreviate(name));
        }
        {
            final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(100);
            final String name = "hello.world";
            assertEquals(name, abbreviator.abbreviate(name));
        }
    }

    @Test
    public void testNoDot() {
        final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
        final String name = "hello";
        assertEquals(name, abbreviator.abbreviate(name));
    }

    @Test
    public void testOneDot() {
        {
            final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
            final String name = "hello.world";
            assertEquals("h.world", abbreviator.abbreviate(name));
        }

        {
            final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
            final String name = "h.world";
            assertEquals("h.world", abbreviator.abbreviate(name));
        }

        {
            final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
            final String name = ".world";
            assertEquals(".world", abbreviator.abbreviate(name));
        }
    }

    @Test
    public void testTwoDot() {
        {
            final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
            final String name = "com.logback.Foobar";
            assertEquals("c.l.Foobar", abbreviator.abbreviate(name));
        }

        {
            final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
            final String name = "c.logback.Foobar";
            assertEquals("c.l.Foobar", abbreviator.abbreviate(name));
        }

        {
            final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
            final String name = "c..Foobar";
            assertEquals("c..Foobar", abbreviator.abbreviate(name));
        }
        {
            final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
            final String name = "..Foobar";
            assertEquals("..Foobar", abbreviator.abbreviate(name));
        }
    }

    @Test
    public void test3Dot() {
        {
            final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
            final String name = "com.logback.xyz.Foobar";
            assertEquals("c.l.x.Foobar", abbreviator.abbreviate(name));
        }
        {
            final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(13);
            final String name = "com.logback.xyz.Foobar";
            assertEquals("c.l.x.Foobar", abbreviator.abbreviate(name));
        }
        {
            final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(14);
            final String name = "com.logback.xyz.Foobar";
            assertEquals("c.l.xyz.Foobar", abbreviator.abbreviate(name));
        }

        {
            final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(15);
            final String name = "com.logback.alligator.Foobar";
            assertEquals("c.l.a.Foobar", abbreviator.abbreviate(name));
        }
    }

    @Test
    public void testXDot() {
        {
            final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(21);
            final String name = "com.logback.wombat.alligator.Foobar";
            assertEquals("c.l.w.a.Foobar", abbreviator.abbreviate(name));
        }

        {
            final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(22);
            final String name = "com.logback.wombat.alligator.Foobar";
            assertEquals("c.l.w.alligator.Foobar", abbreviator.abbreviate(name));
        }

        {
            final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(1);
            final String name = "com.logback.wombat.alligator.tomato.Foobar";
            assertEquals("c.l.w.a.t.Foobar", abbreviator.abbreviate(name));
        }

        {
            final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(21);
            final String name = "com.logback.wombat.alligator.tomato.Foobar";
            assertEquals("c.l.w.a.tomato.Foobar", abbreviator.abbreviate(name));
        }

        {
            final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(29);
            final String name = "com.logback.wombat.alligator.tomato.Foobar";
            assertEquals("c.l.w.alligator.tomato.Foobar", abbreviator.abbreviate(name));
        }
    }
}
