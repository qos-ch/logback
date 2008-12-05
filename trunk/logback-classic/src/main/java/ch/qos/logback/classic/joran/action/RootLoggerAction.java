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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ActionConst;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

public class RootLoggerAction extends Action {
 
  Logger root;
  boolean inError = false;

  public void begin(InterpretationContext ec, String name, Attributes attributes) {
    inError = false;

    LoggerContext loggerContext = (LoggerContext) this.context;
    root = loggerContext.getLogger(LoggerContext.ROOT_NAME);

    String levelStr =  ec.subst(attributes.getValue(ActionConst.LEVEL_ATTRIBUTE));
    if (!OptionHelper.isEmpty(levelStr)) {
      Level level = Level.toLevel(levelStr);
      addInfo("Setting level of ROOT logger to " + level);
      root.setLevel(level);
    }

    ec.pushObject(root);
  }

  public void end(InterpretationContext ec, String name) {
    if (inError) {
      return;
    }
    Object o = ec.peekObject();
    if (o != root) {
      addWarn("The object on the top the of the stack is not the root logger");
      addWarn("It is: " + o);
    } else {
      ec.popObject();
    }
  }

  public void finish(InterpretationContext ec) {
  }
}
