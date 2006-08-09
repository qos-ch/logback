/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.pattern;

import junit.framework.TestCase;

import ch.qos.logback.classic.pattern.ClassNameAbbreviator;

public class ClassNameAbbreviatorTest extends TestCase {

  public ClassNameAbbreviatorTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testShortName() {
    {
      ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(100);
      String name = "hello";
      assertEquals(name, abbreviator.abbreviate(name));
    }
    {
      ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(100);
      String name = "hello.world";
      assertEquals(name, abbreviator.abbreviate(name));
    }
  }

  public void testNoDot() {
    ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(1);
    String name = "hello";
    assertEquals(name, abbreviator.abbreviate(name));
  }

  public void testOneDot() {
    {
      ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(1);
      String name = "hello.world";
      assertEquals("h.world", abbreviator.abbreviate(name));
    }

    {
      ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(1);
      String name = "h.world";
      assertEquals("h.world", abbreviator.abbreviate(name));
    }

    {
      ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(1);
      String name = ".world";
      assertEquals(".world", abbreviator.abbreviate(name));
    }
  }

  public void testTwoDot() {
    {
      ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(1);
      String name = "com.logback.Foobar";
      assertEquals("c.l.Foobar", abbreviator.abbreviate(name));
    }

    {
      ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(1);
      String name = "c.logback.Foobar";
      assertEquals("c.l.Foobar", abbreviator.abbreviate(name));
    }

    {
      ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(1);
      String name = "c..Foobar";
      assertEquals("c..Foobar", abbreviator.abbreviate(name));
    }
    {
      ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(1);
      String name = "..Foobar";
      assertEquals("..Foobar", abbreviator.abbreviate(name));
    }
  }

  public void test3Dot() {
    {
      ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(1);
      String name = "com.logback.xyz.Foobar";
      assertEquals("c.l.x.Foobar", abbreviator.abbreviate(name));
    }
    {
      ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(13);
      String name = "com.logback.xyz.Foobar";
      assertEquals("c.l.x.Foobar", abbreviator.abbreviate(name));
    }
    {
      ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(14);
      String name = "com.logback.xyz.Foobar";
      assertEquals("c.l.xyz.Foobar", abbreviator.abbreviate(name));
    }
    
    {
      ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(15);
      String name = "com.logback.alligator.Foobar";
      assertEquals("c.l.a.Foobar", abbreviator.abbreviate(name));
    }
  }
  
  public void testXDot() {
    {
      ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(21);
      String name = "com.logback.wombat.alligator.Foobar";
      assertEquals("c.l.w.a.Foobar", abbreviator.abbreviate(name));
    }
    
    {
      ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(22);
      String name = "com.logback.wombat.alligator.Foobar";
      assertEquals("c.l.w.alligator.Foobar", abbreviator.abbreviate(name));
    }
    
    {
      ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(1);
      String name = "com.logback.wombat.alligator.tomato.Foobar";
      assertEquals("c.l.w.a.t.Foobar", abbreviator.abbreviate(name));
    }
    
    {
      ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(21);
      String name = "com.logback.wombat.alligator.tomato.Foobar";
      assertEquals("c.l.w.a.tomato.Foobar", abbreviator.abbreviate(name));
    }
    
    {
      ClassNameAbbreviator abbreviator = new ClassNameAbbreviator(29);
      String name = "com.logback.wombat.alligator.tomato.Foobar";
      assertEquals("c.l.w.alligator.tomato.Foobar", abbreviator.abbreviate(name));
    }
  }
}
