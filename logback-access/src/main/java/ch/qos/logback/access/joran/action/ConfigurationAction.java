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
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.StatusPrinter;



public class ConfigurationAction extends Action {
  static final String INTERNAL_DEBUG_ATTR = "debug";
  boolean debugMode = false;

  public void begin(InterpretationContext ec, String name, Attributes attributes) {
    String debugAttrib = attributes.getValue(INTERNAL_DEBUG_ATTR);

    if (
      (debugAttrib == null) || debugAttrib.equals("")
        || debugAttrib.equals("false") || debugAttrib.equals("null")) {
      addInfo("Ignoring " + INTERNAL_DEBUG_ATTR + " attribute.");
    } else { 
      debugMode = true;
    }

    // the context is appender attachable, so it is pushed on top of the stack
    ec.pushObject(getContext());
  }

  public void end(InterpretationContext ec, String name) {
    addInfo("End of configuration.");
    if (debugMode) {
      StatusPrinter.print(context);
    }
    
    ec.popObject();
  }
}
