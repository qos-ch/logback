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

public class Token {

	static public final Token START_TOKEN = new Token(Type.START, null);
	static public final Token CURLY_LEFT_TOKEN = new Token(Type.CURLY_LEFT, null);
	static public final Token CURLY_RIGHT_TOKEN = new Token(Type.CURLY_RIGHT, null);
	static public final Token DEFAULT_SEP_TOKEN = new Token(Type.DEFAULT, null);

	public enum Type {
		LITERAL, START, CURLY_LEFT, CURLY_RIGHT, DEFAULT
	}

	Type type;
	String payload;

	public Token(final Type type, final String payload) {
		this.type = type;
		this.payload = payload;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final Token token = (Token) o;

		if (type != token.type || (payload != null ? !payload.equals(token.payload) : token.payload != null)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		final int result = type != null ? type.hashCode() : 0;
		return 31 * result + (payload != null ? payload.hashCode() : 0);
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder("Token{").append("type=").append(type);
		if (payload != null) {
			result.append(", payload='").append(payload).append('\'');
		}

		result.append('}');
		return result.toString();
	}
}
