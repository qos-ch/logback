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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ExecutionContext;


public class LevelAction extends Action {

  static final String VALUE_ATTR = "value";
  static final String CLASS_ATTR = "class";
  static final String INHERITED = "INHERITED";
  static final String NULL = "NULL";
  static final String EMPTY_STR = "";
  static final Class[] ONE_STRING_PARAM = new Class[] {String.class};

  boolean inError = false;

  public void begin(ExecutionContext ec, String name, Attributes attributes) {
    Object o = ec.peekObject();

    if (!(o instanceof Logger)) {
      inError = true;
      addError("For element <level>, could not find a logger at the top of execution stack.");
      return;
    }

    Logger l = (Logger) o;

    String loggerName = l.getName();

    String levelStr = attributes.getValue(VALUE_ATTR);
    //addInfo("Encapsulating logger name is [" + loggerName
    //    + "], level value is  [" + levelStr + "].");

    if (INHERITED.equalsIgnoreCase(levelStr) || NULL.equalsIgnoreCase(levelStr)) {
      l.setLevel(null);
    } else {
      l.setLevel(Level.toLevel(levelStr, Level.DEBUG));
    }

    addInfo(loggerName + " level set to " + l.getLevel());
  }

  public void finish(ExecutionContext ec) {
  }

  public void end(ExecutionContext ec, String e) {
  }
}
