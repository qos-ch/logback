/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.joran.action;


import org.xml.sax.Attributes;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;

public class RootLoggerAction extends Action {
  static final String NAME_ATTR = "name";
  static final String CLASS_ATTR = "class";
  static final String ADDITIVITY_ATTR = "additivity";
  static final String EMPTY_STR = "";
  static final Class[] ONE_STRING_PARAM = new Class[] { String.class };
 
  Logger root;
  boolean inError = false;
  
  public void begin(InterpretationContext ec, String name, Attributes attributes) {
    inError = false;
    //logger.debug("In begin method");

    LoggerContext loggerContext = (LoggerContext) this.context;
    root = loggerContext.getLogger(LoggerContext.ROOT_NAME);

    //getLogger().debug("Pushing root logger on stack");
    ec.pushObject(root);
  }

  public void end(InterpretationContext ec, String name) {
    //logger.debug("end() called.");

    if (inError) {
      return;
    }

    Object o = ec.peekObject();

    if (o != root) {
      addWarn(
        "The object on the top the of the stack is not the root logger");
      addWarn("It is: "+o);
    } else {
      //getLogger().debug("Removing root logger from top of stack.");
      ec.popObject();
    }
  }

  public void finish(InterpretationContext ec) {
  }
}
