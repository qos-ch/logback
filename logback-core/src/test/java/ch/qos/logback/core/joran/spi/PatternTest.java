/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.joran.spi;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test pattern manipulation code.
 * 
 * @author Ceki Gulcu
 */
public class PatternTest {

  @Test
  public void test1() {
    Pattern p = new Pattern("a");
    assertEquals(1, p.size());
    assertEquals("a", p.peekLast());
    assertEquals("a", p.get(0));
  }

  @Test
  public void testSuffix() {
    Pattern p = new Pattern("a/");
    assertEquals(1, p.size());
    assertEquals("a", p.peekLast());
    assertEquals("a", p.get(0));
  }
  
  @Test
  public void test2() {
    Pattern p = new Pattern("a/b");
    assertEquals(2, p.size());
    assertEquals("b", p.peekLast());
    assertEquals("a", p.get(0));
    assertEquals("b", p.get(1));
  }

  @Test
  public void test3() {
    Pattern p = new Pattern("a123/b1234/cvvsdf");
    assertEquals(3, p.size());
    assertEquals("a123", p.get(0));
    assertEquals("b1234", p.get(1));
    assertEquals("cvvsdf", p.get(2));
  }

  @Test
  public void test4() {
    Pattern p = new Pattern("/a123/b1234/cvvsdf");
    assertEquals(3, p.size());
    assertEquals("a123", p.get(0));
    assertEquals("b1234", p.get(1));
    assertEquals("cvvsdf", p.get(2));
  }

  @Test
  public void test5() {
    Pattern p = new Pattern("//a");
    assertEquals(1, p.size());
    assertEquals("a", p.get(0));
  }

  @Test
  public void test6() {
    Pattern p = new Pattern("//a//b");
    assertEquals(2, p.size());
    assertEquals("a", p.get(0));
    assertEquals("b", p.get(1));
  }

  
  // test tail matching
  @Test
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
      Pattern p = new Pattern("/A");
      Pattern rulePattern = new Pattern("*/a");
      assertEquals(1, p.getTailMatchLength(rulePattern));
    }
    
    {
      Pattern p = new Pattern("/a");
      Pattern rulePattern = new Pattern("*/A");
      assertEquals(1, p.getTailMatchLength(rulePattern));
    }
    
    
    {
      Pattern p = new Pattern("/a/b");
      Pattern rulePattern = new Pattern("*/b");
      assertEquals(1, p.getTailMatchLength(rulePattern));
    }
    
    {
      Pattern p = new Pattern("/a/B");
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
  @Test
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
      Pattern rulePattern = new Pattern("/A/*");
      assertEquals(1, p.getPrefixMatchLength(rulePattern));
    }
    
    {
      Pattern p = new Pattern("/A/b");
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
