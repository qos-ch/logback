/**
 * Logback: the generic, reliable, fast and flexible logging framework for Java.
 * 
 * Copyright (C) 2000-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
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
