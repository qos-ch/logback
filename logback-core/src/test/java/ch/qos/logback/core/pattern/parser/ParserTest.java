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
package ch.qos.logback.core.pattern.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.pattern.FormatInfo;
import ch.qos.logback.core.spi.ScanException;
import ch.qos.logback.core.testUtil.StatusChecker;

public class ParserTest {

    String BARE = Token.BARE_COMPOSITE_KEYWORD_TOKEN.getValue().toString();
    Context context = new ContextBase();

    @Test
    public void testBasic() throws Exception {
        final Parser<Object> p = new Parser<>("hello");
        final Node t = p.parse();
        assertEquals(Node.LITERAL, t.getType());
        assertEquals("hello", t.getValue());
    }

    @Test
    public void testKeyword() throws Exception {

        {
            final Parser<Object> p = new Parser<>("hello%xyz");
            final Node t = p.parse();
            final Node witness = new Node(Node.LITERAL, "hello");
            witness.next = new SimpleKeywordNode("xyz");
            assertEquals(witness, t);
        }

        {
            final Parser<Object> p = new Parser<>("hello%xyz{x}");
            final Node t = p.parse();
            final Node witness = new Node(Node.LITERAL, "hello");
            final SimpleKeywordNode n = new SimpleKeywordNode("xyz");
            final List<String> optionList = new ArrayList<>();
            optionList.add("x");
            n.setOptions(optionList);
            witness.next = n;
            assertEquals(witness, t);
        }
    }

    @Test
    public void testComposite() throws Exception {
        {
            final Parser<Object> p = new Parser<>("hello%(%child)");
            final Node t = p.parse();

            final Node witness = new Node(Node.LITERAL, "hello");
            final CompositeNode composite = new CompositeNode(BARE);
            final Node child = new SimpleKeywordNode("child");
            composite.setChildNode(child);
            witness.next = composite;

            // System.out.println("w:" + witness);
            // System.out.println(t);

            assertEquals(witness, t);
        }

        // System.out.println("testRecursive part 2");
        {
            final Parser<Object> p = new Parser<>("hello%(%child )");
            final Node t = p.parse();

            final Node witness = new Node(Node.LITERAL, "hello");
            final CompositeNode composite = new CompositeNode(BARE);
            final Node child = new SimpleKeywordNode("child");
            composite.setChildNode(child);
            witness.next = composite;
            child.next = new Node(Node.LITERAL, " ");
            assertEquals(witness, t);
        }

        {
            final Parser<Object> p = new Parser<>("hello%(%child %h)");
            final Node t = p.parse();
            final Node witness = new Node(Node.LITERAL, "hello");
            final CompositeNode composite = new CompositeNode(BARE);
            final Node child = new SimpleKeywordNode("child");
            composite.setChildNode(child);
            child.next = new Node(Node.LITERAL, " ");
            child.next.next = new SimpleKeywordNode("h");
            witness.next = composite;
            assertEquals(witness, t);
        }

        {
            final Parser<Object> p = new Parser<>("hello%(%child %h) %m");
            final Node t = p.parse();
            final Node witness = new Node(Node.LITERAL, "hello");
            final CompositeNode composite = new CompositeNode(BARE);
            final Node child = new SimpleKeywordNode("child");
            composite.setChildNode(child);
            child.next = new Node(Node.LITERAL, " ");
            child.next.next = new SimpleKeywordNode("h");
            witness.next = composite;
            composite.next = new Node(Node.LITERAL, " ");
            composite.next.next = new SimpleKeywordNode("m");
            assertEquals(witness, t);
        }

        {
            final Parser<Object> p = new Parser<>("hello%( %child \\(%h\\) ) %m");
            final Node t = p.parse();
            final Node witness = new Node(Node.LITERAL, "hello");
            final CompositeNode composite = new CompositeNode(BARE);
            final Node child = new Node(Node.LITERAL, " ");
            composite.setChildNode(child);
            Node c = child;
            c = c.next = new SimpleKeywordNode("child");
            c = c.next = new Node(Node.LITERAL, " (");
            c = c.next = new SimpleKeywordNode("h");
            c = c.next = new Node(Node.LITERAL, ") ");
            witness.next = composite;
            composite.next = new Node(Node.LITERAL, " ");
            composite.next.next = new SimpleKeywordNode("m");
            assertEquals(witness, t);
        }

    }

    @Test
    public void testNested() throws Exception {
        {
            final Parser<Object> p = new Parser<>("%top %(%child%(%h))");
            final Node t = p.parse();
            final Node witness = new SimpleKeywordNode("top");
            Node w = witness.next = new Node(Node.LITERAL, " ");
            CompositeNode composite = new CompositeNode(BARE);
            w = w.next = composite;
            final Node child = new SimpleKeywordNode("child");
            composite.setChildNode(child);
            composite = new CompositeNode(BARE);
            child.next = composite;
            composite.setChildNode(new SimpleKeywordNode("h"));

            assertEquals(witness, t);
        }
    }

    @Test
    public void testFormattingInfo() throws Exception {
        {
            final Parser<Object> p = new Parser<>("%45x");
            final Node t = p.parse();
            final FormattingNode witness = new SimpleKeywordNode("x");
            witness.setFormatInfo(new FormatInfo(45, Integer.MAX_VALUE));
            assertEquals(witness, t);
        }
        {
            final Parser<Object> p = new Parser<>("%4.5x");
            final Node t = p.parse();
            final FormattingNode witness = new SimpleKeywordNode("x");
            witness.setFormatInfo(new FormatInfo(4, 5));
            assertEquals(witness, t);
        }

        {
            final Parser<Object> p = new Parser<>("%-4.5x");
            final Node t = p.parse();
            final FormattingNode witness = new SimpleKeywordNode("x");
            witness.setFormatInfo(new FormatInfo(4, 5, false, true));
            assertEquals(witness, t);
        }
        {
            final Parser<Object> p = new Parser<>("%-4.-5x");
            final Node t = p.parse();
            final FormattingNode witness = new SimpleKeywordNode("x");
            witness.setFormatInfo(new FormatInfo(4, 5, false, false));
            assertEquals(witness, t);
        }

        {
            final Parser<Object> p = new Parser<>("%-4.5x %12y");
            final Node t = p.parse();
            final FormattingNode witness = new SimpleKeywordNode("x");
            witness.setFormatInfo(new FormatInfo(4, 5, false, true));
            Node n = witness.next = new Node(Node.LITERAL, " ");
            n = n.next = new SimpleKeywordNode("y");
            ((FormattingNode) n).setFormatInfo(new FormatInfo(12, Integer.MAX_VALUE));
            assertEquals(witness, t);
        }
    }

    @Test
    public void testOptions0() throws Exception {
        final Parser<Object> p = new Parser<>("%45x{'test '}");
        final Node t = p.parse();
        final SimpleKeywordNode witness = new SimpleKeywordNode("x");
        witness.setFormatInfo(new FormatInfo(45, Integer.MAX_VALUE));
        final List<String> ol = new ArrayList<>();
        ol.add("test ");
        witness.setOptions(ol);
        assertEquals(witness, t);
    }

    @Test
    public void testOptions1() throws Exception {
        final Parser<Object> p = new Parser<>("%45x{a, b}");
        final Node t = p.parse();
        final SimpleKeywordNode witness = new SimpleKeywordNode("x");
        witness.setFormatInfo(new FormatInfo(45, Integer.MAX_VALUE));
        final List<String> ol = new ArrayList<>();
        ol.add("a");
        ol.add("b");
        witness.setOptions(ol);
        assertEquals(witness, t);
    }

    // see http://jira.qos.ch/browse/LBCORE-180
    @Test
    public void keywordGluedToLitteral() throws Exception {
        final Parser<Object> p = new Parser<>("%x{}a");
        final Node t = p.parse();
        final SimpleKeywordNode witness = new SimpleKeywordNode("x");
        witness.setOptions(new ArrayList<String>());
        witness.next = new Node(Node.LITERAL, "a");
        assertEquals(witness, t);
    }

    @Test
    public void testCompositeFormatting() throws Exception {
        final Parser<Object> p = new Parser<>("hello%5(XYZ)");
        final Node t = p.parse();

        final Node witness = new Node(Node.LITERAL, "hello");
        final CompositeNode composite = new CompositeNode(BARE);
        composite.setFormatInfo(new FormatInfo(5, Integer.MAX_VALUE));
        final Node child = new Node(Node.LITERAL, "XYZ");
        composite.setChildNode(child);
        witness.next = composite;

        assertEquals(witness, t);

    }

    @Test
    public void empty() {
        try {
            final Parser<Object> p = new Parser<>("");
            p.parse();
            fail("");
        } catch (final ScanException e) {

        }
    }

    @Test
    public void lbcore193() throws Exception {
        try {
            final Parser<Object> p = new Parser<>("hello%(abc");
            p.setContext(context);
            p.parse();
            fail("where the is exception?");
        } catch (final ScanException ise) {
            assertEquals("Expecting RIGHT_PARENTHESIS token but got null", ise.getMessage());
        }
        final StatusChecker sc = new StatusChecker(context);
        sc.assertContainsMatch("Expecting RIGHT_PARENTHESIS");
        sc.assertContainsMatch("See also " + Parser.MISSING_RIGHT_PARENTHESIS);
    }

}