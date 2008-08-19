/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.util.OptionHelper;


public class StatusListenerAction extends Action {

 
  boolean inError = false;
  StatusListener statusListener = null;
  
  public void begin(InterpretationContext ec, String name, Attributes attributes) throws ActionException {
    inError = false;
    String className = attributes.getValue(CLASS_ATTRIBUTE);
    if(OptionHelper.isEmpty(className)) {
      addError(
        "Missing class name for statusListener. Near ["
          + name + "] line " + getLineNumber(ec));
      inError = true;
      return;
    }
    
    try {
      statusListener = (StatusListener) OptionHelper.instantiateByClassName(
          className, StatusListener.class, context);
      ec.getContext().getStatusManager().add(statusListener);
      ec.pushObject(statusListener);
    } catch (Exception e) {
      inError = true;
      addError(
        "Could not create an StatusListener of type ["+className+"].", e);
      throw new ActionException(e);
    }
    
  }
 
  public void finish(InterpretationContext ec) {
  }

  public void end(InterpretationContext ec, String e) {
    if (inError) {
      return;
    }
    if (statusListener instanceof LifeCycle) {
      ((LifeCycle) statusListener).start();
    }
    Object o = ec.peekObject();
    if (o != statusListener) {
      addWarn(
        "The object at the of the stack is not the statusListener pushed earlier.");
    } else {
      ec.popObject();
    }
  }
}
