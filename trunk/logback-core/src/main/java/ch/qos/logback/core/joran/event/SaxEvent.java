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
