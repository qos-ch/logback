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

public class Node {

	enum Type {
		LITERAL, VARIABLE
	}

	Type type;
	Object payload;
	Object defaultPart;
	Node next;

	public Node(final Type type, final Object payload) {
		this.type = type;
		this.payload = payload;
	}

	public Node(final Type type, final Object payload, final Object defaultPart) {
		this.type = type;
		this.payload = payload;
		this.defaultPart = defaultPart;
	}

	void append(final Node newNode) {
		if (newNode == null) {
			return;
		}
		Node n = this;
		while (true) {
			if (n.next == null) {
				n.next = newNode;
				return;
			}
			n = n.next;
		}
	}

	@Override
	public String toString() {
		switch (type) {
		case LITERAL:
			return "Node{" + "type=" + type + ", payload='" + payload + "'}";
		case VARIABLE:
			final StringBuilder payloadBuf = new StringBuilder();
			final StringBuilder defaultPartBuf2 = new StringBuilder();
			if (defaultPart != null) {
				recursive((Node) defaultPart, defaultPartBuf2);
			}

			recursive((Node) payload, payloadBuf);
			final StringBuilder r = new StringBuilder("Node{").append("type=").append(type).append(", payload='").append(payloadBuf.toString()).append("'");
			if (defaultPart != null) {
				r.append(", defaultPart=").append(defaultPartBuf2.toString());
			}
			r.append('}');
			return r.toString();
		}
		return null;
	}

	public void dump() {
		System.out.print(toString());
		System.out.print(" -> ");
		if (next != null) {
			next.dump();
		} else {
			System.out.print(" null");
		}
	}

	void recursive(final Node n, final StringBuilder sb) {
		Node c = n;
		while (c != null) {
			sb.append(c.toString()).append(" --> ");
			c = c.next;
		}
		sb.append("null ");
	}

	public void setNext(final Node n) {
		next = n;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final Node node = (Node) o;

		if (type != node.type || (payload != null ? !payload.equals(node.payload) : node.payload != null) || (defaultPart != null ? !defaultPart.equals(node.defaultPart) : node.defaultPart != null)
				|| (next != null ? !next.equals(node.next) : node.next != null)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = type != null ? type.hashCode() : 0;
		result = 31 * result + (payload != null ? payload.hashCode() : 0);
		result = 31 * result + (defaultPart != null ? defaultPart.hashCode() : 0);
		return 31 * result + (next != null ? next.hashCode() : 0);
	}
}
