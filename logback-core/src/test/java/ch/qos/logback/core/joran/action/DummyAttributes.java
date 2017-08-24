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

    HashMap<String, String> atts = new HashMap<String, String>();

    public int getIndex(String qName) {
        return 0;
    }

    public int getIndex(String uri, String localName) {
        return 0;
    }

    public int getLength() {
        return 0;
    }

    public String getLocalName(int index) {
        return null;
    }

    public String getQName(int index) {
        return null;
    }

    public String getType(int index) {
        return null;
    }

    public String getType(String qName) {
        return null;
    }

    public String getType(String uri, String localName) {
        return null;
    }

    public String getURI(int index) {
        return null;
    }

    public String getValue(int index) {
        return null;
    }

    public String getValue(String qName) {
        return atts.get(qName);
    }

    public void setValue(String key, String value) {
        atts.put(key, value);
    }

    public String getValue(String uri, String localName) {
        return null;
    }

}
