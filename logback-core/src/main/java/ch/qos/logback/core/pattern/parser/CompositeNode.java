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

public class CompositeNode extends SimpleKeywordNode {
    Node childNode;

    CompositeNode(final String keyword) {
        super(Node.COMPOSITE_KEYWORD, keyword);

    }

    public Node getChildNode() {
        return childNode;
    }

    public void setChildNode(final Node childNode) {
        this.childNode = childNode;
    }

    @Override
    public boolean equals(final Object o) {
        if (!super.equals(o) || !(o instanceof CompositeNode)) {
            return false;
        }
        final CompositeNode r = (CompositeNode) o;

        return childNode != null ? childNode.equals(r.childNode) : r.childNode == null;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        if (childNode != null) {
            buf.append("CompositeNode(" + childNode + ")");
        } else {
            buf.append("CompositeNode(no child)");
        }
        buf.append(printNext());
        return buf.toString();
    }
}