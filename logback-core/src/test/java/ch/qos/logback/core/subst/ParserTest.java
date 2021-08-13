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
package ch.qos.logback.core.subst;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;

import ch.qos.logback.core.spi.ScanException;

/**
 * Created with IntelliJ IDEA. User: ceki Date: 05.08.12 Time: 00:15 To change
 * this template use File | Settings | File Templates.
 */
public class ParserTest {

	@Test
	public void literal() throws ScanException {
		Tokenizer tokenizer = new Tokenizer("abc");
		Parser parser = new Parser(tokenizer.tokenize());
		Node node = parser.parse();
		Node witness = new Node(Node.Type.LITERAL, "abc");
		assertEquals(witness, node);
	}

	@Test
	public void literalWithAccolade0() throws ScanException {
		Tokenizer tokenizer = new Tokenizer("{}");
		Parser parser = new Parser(tokenizer.tokenize());
		Node node = parser.parse();
		Node witness = new Node(Node.Type.LITERAL, "{");
		witness.next = new Node(Node.Type.LITERAL, "}");
		assertEquals(witness, node);
	}

	@Test
	public void literalWithAccolade1() throws ScanException {
		Tokenizer tokenizer = new Tokenizer("%x{a}");
		Parser parser = new Parser(tokenizer.tokenize());
		Node node = parser.parse();
		Node witness = new Node(Node.Type.LITERAL, "%x");
		Node t = witness.next = new Node(Node.Type.LITERAL, "{");
		t.next = new Node(Node.Type.LITERAL, "a");
		t = t.next;
		t.next = new Node(Node.Type.LITERAL, "}");
		assertEquals(witness, node);
	}

	@Test
	public void literalWithTwoAccolades() throws ScanException {
		Tokenizer tokenizer = new Tokenizer("%x{y} %a{b} c");

		Parser parser = new Parser(tokenizer.tokenize());
		Node node = parser.parse();
		Node witness = new Node(Node.Type.LITERAL, "%x");

		Node t = witness.next = new Node(Node.Type.LITERAL, "{");
		t.next = new Node(Node.Type.LITERAL, "y");
		t = t.next;

		t.next = new Node(Node.Type.LITERAL, "}");
		t = t.next;

		t.next = new Node(Node.Type.LITERAL, " %a");
		t = t.next;

		t.next = new Node(Node.Type.LITERAL, "{");
		t = t.next;

		t.next = new Node(Node.Type.LITERAL, "b");
		t = t.next;

		t.next = new Node(Node.Type.LITERAL, "}");
		t = t.next;

		t.next = new Node(Node.Type.LITERAL, " c");

		node.dump();
		System.out.println("");
		assertEquals(witness, node);
	}

	@Test
	public void variable() throws ScanException {
		Tokenizer tokenizer = new Tokenizer("${abc}");
		Parser parser = new Parser(tokenizer.tokenize());
		Node node = parser.parse();
		Node witness = new Node(Node.Type.VARIABLE, new Node(Node.Type.LITERAL, "abc"));
		assertEquals(witness, node);
	}

	@Test
	public void literalVariableLiteral() throws ScanException {
		Tokenizer tokenizer = new Tokenizer("a${b}c");
		Parser parser = new Parser(tokenizer.tokenize());
		Node node = parser.parse();
		Node witness = new Node(Node.Type.LITERAL, "a");
		witness.next = new Node(Node.Type.VARIABLE, new Node(Node.Type.LITERAL, "b"));
		witness.next.next = new Node(Node.Type.LITERAL, "c");
		assertEquals(witness, node);
	}

	// /LOGBACK-744
	@Test
	public void withColon() throws ScanException {
		Tokenizer tokenizer = new Tokenizer("a:${b}");
		Parser parser = new Parser(tokenizer.tokenize());
		Node node = parser.parse();
		Node witness = new Node(Node.Type.LITERAL, "a");
		Node t = witness.next = new Node(Node.Type.LITERAL, ":");
		t.next = new Node(Node.Type.VARIABLE, new Node(Node.Type.LITERAL, "b"));
		assertEquals(witness, node);
	}

	@Test
	public void withNoClosingBraces() throws ScanException {
		Tokenizer tokenizer = new Tokenizer("a${b");
		Parser parser = new Parser(tokenizer.tokenize());
		try {
			parser.parse();
		} catch (IllegalArgumentException e) {
			assertEquals("All tokens consumed but was expecting \"}\"", e.getMessage());
			return;
		}
		fail();
	}

	@Test
	public void nested() throws ScanException {
		Tokenizer tokenizer = new Tokenizer("a${b${c}}d");
		Parser parser = new Parser(tokenizer.tokenize());
		Node node = parser.parse();
		Node witness = new Node(Node.Type.LITERAL, "a");
		Node bLiteralNode = new Node(Node.Type.LITERAL, "b");
		Node cLiteralNode = new Node(Node.Type.LITERAL, "c");
		Node bVariableNode = new Node(Node.Type.VARIABLE, bLiteralNode);
		Node cVariableNode = new Node(Node.Type.VARIABLE, cLiteralNode);
		bLiteralNode.next = cVariableNode;

		witness.next = bVariableNode;
		witness.next.next = new Node(Node.Type.LITERAL, "d");
		assertEquals(witness, node);
	}

	@Test
	public void withDefault() throws ScanException {
		Tokenizer tokenizer = new Tokenizer("${b:-c}");
		Parser parser = new Parser(tokenizer.tokenize());
		Node node = parser.parse();
		Node witness = new Node(Node.Type.VARIABLE, new Node(Node.Type.LITERAL, "b"));
		witness.defaultPart = new Node(Node.Type.LITERAL, "c");
		assertEquals(witness, node);
	}

	@Test
	public void defaultSeparatorOutsideOfAVariable() throws ScanException {
		Tokenizer tokenizer = new Tokenizer("{a:-b}");
		Parser parser = new Parser(tokenizer.tokenize());
		Node node = parser.parse();

		dump(node);
		Node witness = new Node(Node.Type.LITERAL, "{");
		Node t = witness.next = new Node(Node.Type.LITERAL, "a");

		t.next = new Node(Node.Type.LITERAL, ":-");
		t = t.next;

		t.next = new Node(Node.Type.LITERAL, "b");
		t = t.next;

		t.next = new Node(Node.Type.LITERAL, "}");

		assertEquals(witness, node);
	}

	@Test
	public void emptyTokenListDoesNotThrowNullPointerException() throws ScanException {
		// An empty token list would be returned from Tokenizer.tokenize()
		// if it were constructed with an empty string. The parser should
		// be able to handle this.
		Parser parser = new Parser(new ArrayList<Token>());
		parser.parse();
	}

	private void dump(Node node) {
		while (node != null) {
			System.out.println(node.toString());
			node = node.next;
		}
	}

}
