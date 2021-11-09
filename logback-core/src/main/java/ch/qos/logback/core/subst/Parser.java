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

import java.util.List;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.ScanException;

// E = TE|T
//   = T(E|~)
// E = TEopt where Eopt = E|~
// T = LITERAL | { C } |'${' V '}'
// C = E|E :- E
//   = E(':-'E|~)
// V = E|E :- E
//   = E(':-'E|~)

/**
 * Parse a token list returning a node chain.
 *
 * @author Ceki Gulcu
 */
public class Parser {

	final List<Token> tokenList;
	int pointer = 0;

	public Parser(final List<Token> tokenList) {
		this.tokenList = tokenList;
	}

	public Node parse() throws ScanException {
		if (tokenList == null || tokenList.isEmpty()) {
			return null;
		}
		return E();
	}

	private Node E() throws ScanException {
		final Node t = T();
		if (t == null) {
			return null;
		}
		final Node eOpt = Eopt();
		if (eOpt != null) {
			t.append(eOpt);
		}
		return t;
	}

	// Eopt = E|~
	private Node Eopt() throws ScanException {
		final Token next = peekAtCurentToken();
		if (next == null) {
			return null;
		}
		return E();
	}

	// T = LITERAL | '${' V '}'
	private Node T() throws ScanException {
		final Token t = peekAtCurentToken();

		switch (t.type) {
		case LITERAL:
			advanceTokenPointer();
			return makeNewLiteralNode(t.payload);
		case CURLY_LEFT:
			advanceTokenPointer();
			final Node innerNode = C();
			final Token right = peekAtCurentToken();
			expectCurlyRight(right);
			advanceTokenPointer();
			final Node curlyLeft = makeNewLiteralNode(CoreConstants.LEFT_ACCOLADE);
			curlyLeft.append(innerNode);
			curlyLeft.append(makeNewLiteralNode(CoreConstants.RIGHT_ACCOLADE));
			return curlyLeft;
		case START:
			advanceTokenPointer();
			final Node v = V();
			final Token w = peekAtCurentToken();
			expectCurlyRight(w);
			advanceTokenPointer();
			return v;
		default:
			return null;
		}
	}

	private Node makeNewLiteralNode(final String s) {
		return new Node(Node.Type.LITERAL, s);
	}

	// V = E(':='E|~)
	private Node V() throws ScanException {
		final Node e = E();
		final Node variable = new Node(Node.Type.VARIABLE, e);
		final Token t = peekAtCurentToken();
		if (isDefaultToken(t)) {
			advanceTokenPointer();
			final Node def = E();
			variable.defaultPart = def;
		}
		return variable;
	}

	// C = E(':='E|~)
	private Node C() throws ScanException {
		final Node e0 = E();
		final Token t = peekAtCurentToken();
		if (isDefaultToken(t)) {
			advanceTokenPointer();
			final Node literal = makeNewLiteralNode(CoreConstants.DEFAULT_VALUE_SEPARATOR);
			e0.append(literal);
			final Node e1 = E();
			e0.append(e1);
		}
		return e0;
	}

	private boolean isDefaultToken(final Token t) {
		return t != null && t.type == Token.Type.DEFAULT;
	}

	void advanceTokenPointer() {
		pointer++;
	}

	void expectNotNull(final Token t, final String expected) {
		if (t == null) {
			throw new IllegalArgumentException("All tokens consumed but was expecting \"" + expected + "\"");
		}
	}

	void expectCurlyRight(final Token t) throws ScanException {
		expectNotNull(t, "}");
		if (t.type != Token.Type.CURLY_RIGHT) {
			throw new ScanException("Expecting }");
		}
	}

	Token peekAtCurentToken() {
		if (pointer < tokenList.size()) {
			return tokenList.get(pointer);
		}
		return null;
	}

}
