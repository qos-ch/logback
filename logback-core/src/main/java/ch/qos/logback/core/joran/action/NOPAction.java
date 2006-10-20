/**
 * Logback: the generic, reliable, fast and flexible logging framework for Java.
 * 
 * Copyright (C) 2000-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ExecutionContext;


/**
 * No operation (NOP) action that does strictly nothing. 
 * Setting a rule to this pattern is sometimes useful in order
 * to prevent implicit actions to kick in.
 *  
 * @author Ceki G&uuml;lc&uuml;
 */
public class NOPAction extends Action {
  
  public void begin(ExecutionContext ec, String name, Attributes attributes) {
  }


  public void end(ExecutionContext ec, String name) {
  }
}
