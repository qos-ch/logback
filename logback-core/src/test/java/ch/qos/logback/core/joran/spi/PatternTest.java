/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.joran.spi;

import ch.qos.logback.core.joran.spi.Pattern;
import junit.framework.TestCase;

/**
 * Test pattern manipulation code.
 * 
 * @author Ceki Gulcu
 */
public class PatternTest extends TestCase {
  /**
   * Constructor for PatternTestCase.
   * 
   * @param name
   */
  public PatternTest(String name) {
    super(name);
  }

  /*
   * @see TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
  }

  /*
   * @see TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void test1() {
    Pattern p = new Pattern("a");
    assertEquals(1, p.size());
    assertEquals("a", p.peekLast());
    assertEquals("a", p.get(0));
  }

  public void test2() {
    Pattern p = new Pattern("a/b");
    assertEquals(2, p.size());
    assertEquals("b", p.peekLast());
    assertEquals("a", p.get(0));
    assertEquals("b", p.get(1));
  }

  public void test3() {
    Pattern p = new Pattern("a123/b1234/cvvsdf");
    assertEquals(3, p.size());
    assertEquals("a123", p.get(0));
    assertEquals("b1234", p.get(1));
    assertEquals("cvvsdf", p.get(2));
  }

  public void test4() {
    Pattern p = new Pattern("/a123/b1234/cvvsdf");
    assertEquals(3, p.size());
    assertEquals("a123", p.get(0));
    assertEquals("b1234", p.get(1));
    assertEquals("cvvsdf", p.get(2));
  }

  public void test5() {
    Pattern p = new Pattern("//a");
    assertEquals(1, p.size());
    assertEquals("a", p.get(0));
  }

  public void test6() {
    Pattern p = new Pattern("//a//b");
    assertEquals(2, p.size());
    assertEquals("a", p.get(0));
    assertEquals("b", p.get(1));
  }

  
  // test tail matching
  public void testTailMatch() {
    {
      Pattern p = new Pattern("/a/b");
      Pattern rulePattern = new Pattern("*");
      assertEquals(0, p.getTailMatchLength(rulePattern));
    }

    {
      Pattern p = new Pattern("/a");
      Pattern rulePattern = new Pattern("*/a");
      assertEquals(1, p.getTailMatchLength(rulePattern));
    }
    
    {
      Pattern p = new Pattern("/a/b");
      Pattern rulePattern = new Pattern("*/b");
      assertEquals(1, p.getTailMatchLength(rulePattern));
    }
    
    
    {
      Pattern p = new Pattern("/a/b/c");
      Pattern rulePattern = new Pattern("*/b/c");
      assertEquals(2, p.getTailMatchLength(rulePattern));
    }
  }
  
  // test prefix matching
  public void testPrefixMatch() {
    {
      Pattern p = new Pattern("/a/b");
      Pattern rulePattern = new Pattern("/x/*");
      assertEquals(0, p.getPrefixMatchLength(rulePattern));
    }

    {
      Pattern p = new Pattern("/a");
      Pattern rulePattern = new Pattern("/x/*");
      assertEquals(0, p.getPrefixMatchLength(rulePattern));
    }

    {
      Pattern p = new Pattern("/a/b");
      Pattern rulePattern = new Pattern("/a/*");
      assertEquals(1, p.getPrefixMatchLength(rulePattern));
    }
    
    {
      Pattern p = new Pattern("/a/b");
      Pattern rulePattern = new Pattern("/a/b/*");
      assertEquals(2, p.getPrefixMatchLength(rulePattern));
    }
    
    {
      Pattern p = new Pattern("/a/b");
      Pattern rulePattern = new Pattern("/*");
      assertEquals(0, p.getPrefixMatchLength(rulePattern));
    }
  }

}
