/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.AttributesImpl;

import ch.qos.logback.core.joran.spi.Pattern;

public class StartEvent extends SaxEvent {

  final public Attributes attributes;
  final public Pattern pattern;
  
  StartEvent(Pattern pattern, String namespaceURI, String localName, String qName,
      Attributes attributes, Locator locator) {
    super(namespaceURI, localName, qName, locator);
    // locator impl is used to take a snapshot!
    this.attributes = new AttributesImpl(attributes);
    this.pattern = pattern;
  }

  public Attributes getAttributes() {
    return attributes;
  }

  
  @Override
  public String toString() {
    return "StartEvent("+getQName()+")  ["+locator.getLineNumber()+","+locator.getColumnNumber()+"]";
  }

}
