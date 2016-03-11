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

import ch.qos.logback.core.joran.spi.ElementPath;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.AttributesImpl;

public class StartEvent extends SaxEvent {

    final public Attributes attributes;
    final public ElementPath elementPath;

    StartEvent(ElementPath elementPath, String namespaceURI, String localName, String qName, Attributes attributes, Locator locator) {
        super(namespaceURI, localName, qName, locator);
        // locator impl is used to take a snapshot!
        this.attributes = new AttributesImpl(attributes);
        this.elementPath = elementPath;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("StartEvent(");
        b.append(getQName());
        if(attributes != null) {
            for(int i = 0; i < attributes.getLength(); i++) {
                if(i > 0) 
                    b.append(' ');
                b.append(attributes.getLocalName(i)).append("=\"").append(attributes.getValue(i)).append("\"");
            }
        }
        b.append(")  [");
        b.append( locator.getLineNumber());
        b.append(",");
        b.append(locator.getColumnNumber());
        b.append("]");
        return b.toString();
    }

}
