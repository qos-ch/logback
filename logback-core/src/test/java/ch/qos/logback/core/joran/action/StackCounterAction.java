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

import ch.qos.logback.core.Layout;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;



public class StackCounterAction extends Action {
 Layout layout;


  public StackCounterAction() {
  }
  /**
   * Instantiates an layout of the given class and sets its name.
   *
   */
  public void begin(InterpretationContext ec, String name, Attributes attributes) {
    //String str = "Pushing "+name+"-begin";
    ec.pushObject(name+"-begin");
  }

  /**
   * Once the children elements are also parsed, now is the time to activate
   * the appender options.
   */
  public void end(InterpretationContext ec, String name) {
    //String str = "Pushing "+name+"-end";
    ec.pushObject(name+"-end");    
  }

  public void finish(InterpretationContext ec) {
  }
}
