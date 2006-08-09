/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.access.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ExecutionContext;



public class ConfigurationAction extends Action {
  static final String INTERNAL_DEBUG_ATTR = "debug";
  boolean attachment = false;

  public void begin(ExecutionContext ec, String name, Attributes attributes) {
    //String debugAttrib = attributes.getValue(INTERNAL_DEBUG_ATTR);

    // the context is appender attachable, so it is pushed on top of the stack
    ec.pushObject(getContext());
  }

  public void end(ExecutionContext ec, String name) {
    ec.popObject();
  }
}
