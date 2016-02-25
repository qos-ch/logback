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
package ch.qos.logback.core.joran.spi;

import java.util.ArrayList;
import java.util.List;

/**
 * A element path characterizes a traversal path in an XML document.
 *
 * @author Ceki Gulcu
 * @since 1.1.0
 */
public class ElementPath {
    // contains String instances
    ArrayList<String> partList = new ArrayList<String>();

    public ElementPath() {
    }

    public ElementPath(List<String> list) {
        partList.addAll(list);
    }

    /**
     * Build an elementPath from a string.
     * <p/>
     * Note that "/x" is considered equivalent to "x" and to "x/"
     */
    public ElementPath(String pathStr) {
        if (pathStr == null) {
            return;
        }

        String[] partArray = pathStr.split("/");
        if (partArray == null)
            return;

        for (String part : partArray) {
            if (part.length() > 0) {
                partList.add(part);
            }
        }
    }

    public ElementPath duplicate() {
        ElementPath p = new ElementPath();
        p.partList.addAll(this.partList);
        return p;
    }

    // Joran error skipping relies on the equals method
    @Override
    public boolean equals(Object o) {
        if ((o == null) || !(o instanceof ElementPath)) {
            return false;
        }

        ElementPath r = (ElementPath) o;

        if (r.size() != size()) {
            return false;
        }

        int len = size();

        for (int i = 0; i < len; i++) {
            if (!equalityCheck(get(i), r.get(i))) {
                return false;
            }
        }

        // if everything matches, then the two patterns are equal
        return true;
    }

    private boolean equalityCheck(String x, String y) {
        return x.equalsIgnoreCase(y);
    }

    public List<String> getCopyOfPartList() {
        return new ArrayList<String>(partList);
    }

    public void push(String s) {
        partList.add(s);
    }

    public String get(int i) {
        return (String) partList.get(i);
    }

    public void pop() {
        if (!partList.isEmpty()) {
            partList.remove(partList.size() - 1);
        }
    }

    public String peekLast() {
        if (!partList.isEmpty()) {
            int size = partList.size();
            return (String) partList.get(size - 1);
        } else {
            return null;
        }
    }

    public int size() {
        return partList.size();
    }

    protected String toStableString() {
        StringBuilder result = new StringBuilder();
        for (String current : partList) {
            result.append("[").append(current).append("]");
        }
        return result.toString();
    }

    @Override
    public String toString() {
        return toStableString();
    }
}
