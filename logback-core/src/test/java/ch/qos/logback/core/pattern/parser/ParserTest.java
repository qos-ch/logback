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
package ch.qos.logback.core.pattern.parser;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.qos.logback.core.pattern.FormatInfo;

public class ParserTest {

  @Test
  public void testBasic() throws Exception {
    Parser p = new Parser("hello");
    Node t = p.parse();
    assertEquals(Node.LITERAL, t.getType());
    assertEquals("hello", t.getValue());
  }

  @Test
  public void testKeyword() throws Exception {

    {
      Parser p = new Parser("hello%xyz");
      Node t = p.parse();
      Node witness = new Node(Node.LITERAL, "hello");
      witness.next = new KeywordNode("xyz");
      assertEquals(witness, t);
    }

    {
      Parser p = new Parser("hello%xyz{x}");
      Node t = p.parse();
      Node witness = new Node(Node.LITERAL, "hello");
      KeywordNode n = new KeywordNode("xyz");
      List<String> optionList = new ArrayList<String>();
      optionList.add("x");
      n.setOptions(optionList);
      witness.next = n;
      assertEquals(witness, t);
    }
  }

  @Test
  public void testComposite() throws Exception {
    {
      Parser p = new Parser("hello%(%child)");
      Node t = p.parse();

      Node witness = new Node(Node.LITERAL, "hello");
      CompositeNode composite = new CompositeNode();
      Node child = new KeywordNode("child");
      composite.setChildNode(child);
      witness.next = composite;

      // System.out.println("w:" + witness);
      // System.out.println(t);

      assertEquals(witness, t);
    }

    // System.out.println("testRecursive part 2");
    {
      Parser p = new Parser("hello%(%child )");
      Node t = p.parse();

      Node witness = new Node(Node.LITERAL, "hello");
      CompositeNode composite = new CompositeNode();
      Node child = new KeywordNode("child");
      composite.setChildNode(child);
      witness.next = composite;
      child.next = new Node(Node.LITERAL, " ");
      assertEquals(witness, t);
    }

    {
      Parser p = new Parser("hello%(%child %h)");
      Node t = p.parse();
      Node witness = new Node(Node.LITERAL, "hello");
      CompositeNode composite = new CompositeNode();
      Node child = new KeywordNode("child");
      composite.setChildNode(child);
      child.next = new Node(Node.LITERAL, " ");
      child.next.next = new KeywordNode("h");
      witness.next = composite;
      assertEquals(witness, t);
    }

    {
      Parser p = new Parser("hello%(%child %h) %m");
      Node t = p.parse();
      Node witness = new Node(Node.LITERAL, "hello");
      CompositeNode composite = new CompositeNode();
      Node child = new KeywordNode("child");
      composite.setChildNode(child);
      child.next = new Node(Node.LITERAL, " ");
      child.next.next = new KeywordNode("h");
      witness.next = composite;
      composite.next = new Node(Node.LITERAL, " ");
      composite.next.next = new KeywordNode("m");
      assertEquals(witness, t);
    }

    {
      Parser p = new Parser("hello%( %child \\(%h\\) ) %m");
      Node t = p.parse();
      Node witness = new Node(Node.LITERAL, "hello");
      CompositeNode composite = new CompositeNode();
      Node child = new Node(Node.LITERAL, " ");
      composite.setChildNode(child);
      Node c = child;
      c = c.next = new KeywordNode("child");
      c = c.next = new Node(Node.LITERAL, " (");
      c = c.next = new KeywordNode("h");
      c = c.next = new Node(Node.LITERAL, ") ");
      witness.next = composite;
      composite.next = new Node(Node.LITERAL, " ");
      composite.next.next = new KeywordNode("m");
      assertEquals(witness, t);

    }
  }
  
  @Test
  public void testNested() throws Exception {
    {
      Parser p = new Parser("%top %(%child%(%h))");
      Node t = p.parse();
      Node witness = new KeywordNode("top");
      Node w = witness.next = new Node(Node.LITERAL, " ");
      CompositeNode composite = new CompositeNode();
      w = w.next = composite;
      Node child = new KeywordNode("child");
      composite.setChildNode(child);
      composite = new CompositeNode();
      child.next = composite;
      composite.setChildNode(new KeywordNode("h"));

      assertEquals(witness, t);
    }
  }

  @Test
  public void testFormattingInfo() throws Exception {
    {
      Parser p = new Parser("%45x");
      Node t = p.parse();
      FormattingNode witness = new KeywordNode("x");
      witness.setFormatInfo(new FormatInfo(45, Integer.MAX_VALUE));
      assertEquals(witness, t);
    }
    {
      Parser p = new Parser("%4.5x");
      Node t = p.parse();
      FormattingNode witness = new KeywordNode("x");
      witness.setFormatInfo(new FormatInfo(4, 5));
      assertEquals(witness, t);
    }

    {
      Parser p = new Parser("%-4.5x");
      Node t = p.parse();
      FormattingNode witness = new KeywordNode("x");
      witness.setFormatInfo(new FormatInfo(4, 5, false, true));
      assertEquals(witness, t);
    }
    {
      Parser p = new Parser("%-4.-5x");
      Node t = p.parse();
      FormattingNode witness = new KeywordNode("x");
      witness.setFormatInfo(new FormatInfo(4, 5, false, false));
      assertEquals(witness, t);
    }

    {
      Parser p = new Parser("%-4.5x %12y");
      Node t = p.parse();
      FormattingNode witness = new KeywordNode("x");
      witness.setFormatInfo(new FormatInfo(4, 5, false, true));
      Node n = witness.next = new Node(Node.LITERAL, " ");
      n = n.next = new KeywordNode("y");
      ((FormattingNode) n).setFormatInfo(new FormatInfo(12, Integer.MAX_VALUE));
      assertEquals(witness, t);
    }
  }

  @Test
  public void testOptions() throws Exception {
    {
      Parser p = new Parser("%45x{'test '}");
      Node t = p.parse();
      KeywordNode witness = new KeywordNode("x");
      witness.setFormatInfo(new FormatInfo(45, Integer.MAX_VALUE));
      List<String> ol = new ArrayList<String>();
      ol.add("test ");
      witness.setOptions(ol);
      assertEquals(witness, t);
    }

    {
      Parser p = new Parser("%45x{a, b}");
      Node t = p.parse();
      KeywordNode witness = new KeywordNode("x");
      witness.setFormatInfo(new FormatInfo(45, Integer.MAX_VALUE));
      List<String> ol = new ArrayList<String>();
      ol.add("a");
      ol.add("b");
      witness.setOptions(ol);
      assertEquals(witness, t);
    }
  }

  @Test
  public void testCompositeFormatting() throws Exception {

    {
      Parser p = new Parser("hello%5(XYZ)");
      Node t = p.parse();

      Node witness = new Node(Node.LITERAL, "hello");
      CompositeNode composite = new CompositeNode();
      composite.setFormatInfo(new FormatInfo(5, Integer.MAX_VALUE));
      Node child = new Node(Node.LITERAL, "XYZ");
      composite.setChildNode(child);
      witness.next = composite;

      assertEquals(witness, t);
    }
  }
}