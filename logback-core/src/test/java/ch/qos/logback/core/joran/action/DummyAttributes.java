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
package ch.qos.logback.core.joran.action;

import java.util.HashMap;

import org.xml.sax.Attributes;

public class DummyAttributes implements Attributes {

    HashMap<String, String> atts = new HashMap<>();

    @Override
    public int getIndex(final String qName) {
        return 0;
    }

    @Override
    public int getIndex(final String uri, final String localName) {
        return 0;
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public String getLocalName(final int index) {
        return null;
    }

    @Override
    public String getQName(final int index) {
        return null;
    }

    @Override
    public String getType(final int index) {
        return null;
    }

    @Override
    public String getType(final String qName) {
        return null;
    }

    @Override
    public String getType(final String uri, final String localName) {
        return null;
    }

    @Override
    public String getURI(final int index) {
        return null;
    }

    @Override
    public String getValue(final int index) {
        return null;
    }

    @Override
    public String getValue(final String qName) {
        return atts.get(qName);
    }

    public void setValue(final String key, final String value) {
        atts.put(key, value);
    }

    @Override
    public String getValue(final String uri, final String localName) {
        return null;
    }

}
