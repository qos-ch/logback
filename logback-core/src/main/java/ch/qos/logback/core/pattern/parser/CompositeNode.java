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

    CompositeNode(String keyword) {
        super(Node.COMPOSITE_KEYWORD, keyword);

    }

    public Node getChildNode() {
        return childNode;
    }

    public void setChildNode(Node childNode) {
        this.childNode = childNode;
    }

    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        if (!(o instanceof CompositeNode)) {
            return false;
        }
        CompositeNode r = (CompositeNode) o;

        return (childNode != null) ? childNode.equals(r.childNode) : (r.childNode == null);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (childNode != null) {
            buf.append("CompositeNode(" + childNode + ")");
        } else {
            buf.append("CompositeNode(no child)");
        }
        buf.append(printNext());
        return buf.toString();
    }
}