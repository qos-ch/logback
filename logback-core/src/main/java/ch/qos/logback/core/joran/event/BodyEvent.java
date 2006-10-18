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


public class BodyEvent extends SaxEvent {

  private String text;

  BodyEvent(String text, Locator locator) {
    super(null, null, null, locator);
    this.text = text;
  }

  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return "BodyEvent(" + getText() + ")" + locator.getLineNumber() + ","
        + locator.getColumnNumber();
  }

  public void append(String str) {
    text += str;
  }

}
