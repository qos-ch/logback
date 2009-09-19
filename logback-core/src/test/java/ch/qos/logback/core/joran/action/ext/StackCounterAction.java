/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.joran.action.ext;


import org.xml.sax.Attributes;

import ch.qos.logback.core.Layout;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;



public class StackCounterAction extends Action {
 Layout layout;


  public StackCounterAction() {
  }

  public void begin(InterpretationContext ec, String name, Attributes attributes) {
    //String str = "Pushing "+name+"-begin";
    ec.pushObject(name+"-begin");
  }

  public void end(InterpretationContext ec, String name) {
    ec.pushObject(name+"-end");    
  }

  public void finish(InterpretationContext ec) {
  }
}
