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
package ch.qos.logback.core.joran.event;

import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

public class SaxEvent {

    final public String namespaceURI;
    final public String localName;
    final public String qName;
    final public Locator locator;

    SaxEvent(String namespaceURI, String localName, String qName, Locator locator) {
        this.namespaceURI = namespaceURI;
        this.localName = localName;
        this.qName = qName;
        // locator impl is used to take a snapshot!
        this.locator = new LocatorImpl(locator);
    }

    public String getLocalName() {
        return localName;
    }

    public Locator getLocator() {
        return locator;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public String getQName() {
        return qName;
    }
}
