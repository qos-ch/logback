/**
 * Logback: the generic, reliable, fast and flexible logging framework for Java.
 * 
 * Copyright (C) 2000-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.joran.ia;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;

public class FruitContextAction extends Action {

  private boolean inError = false;

  
  @Override
  public void begin(InterpretationContext ec, String name, Attributes attributes)
      throws ActionException {

    inError = false;
    
    try {
      ec.pushObject(context);
    } catch (Exception oops) {
      inError = true;
      addError(
        "Could not push context", oops);
      throw new ActionException(ActionException.SKIP_CHILDREN, oops);
    }
  }

  @Override
  public void end(InterpretationContext ec, String name) throws ActionException {
    if (inError) {
      return;
    }

    Object o = ec.peekObject();

    if (o != context) {
      addWarn(
        "The object at the of the stack is not the context named ["
        + context.getName() + "] pushed earlier.");
    } else {
      addInfo(
        "Popping context named [" + context.getName()
        + "] from the object stack");
      ec.popObject();
    }
  }

  
}
