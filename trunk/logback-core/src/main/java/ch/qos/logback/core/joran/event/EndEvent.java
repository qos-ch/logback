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


public class EndEvent extends SaxEvent {

  EndEvent(String namespaceURI, String localName, String qName, Locator locator) {
    super(namespaceURI, localName, qName, locator);
  }

  @Override
  public String toString() {
    return "  EndEvent("+getQName()+")  ["+locator.getLineNumber()+","+locator.getColumnNumber()+"]";
  }


}
