/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;

public class ContextNameAction extends Action {

  public void begin(InterpretationContext ec, String name, Attributes attributes) {
  }

  public void body(InterpretationContext ec, String body) {

    String finalBody = ec.subst(body);
    addInfo("Setting logger context name as [" + finalBody + "]");
    try {
      context.setName(finalBody);
    } catch (IllegalStateException e) {
      addError("Failed to rename context [" + context.getName() + "] as ["
          + finalBody + "]", e);
    }
  }

  public void end(InterpretationContext ec, String name) {
  }
}
