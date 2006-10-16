/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ExecutionContext;



public class BadBeginAction extends Action {


  public BadBeginAction() {
  }

  public void begin(ExecutionContext ec, String name, Attributes attributes) {
    throw new IllegalStateException("bad begin");
  }

  public void end(ExecutionContext ec, String name) {
  }
}
