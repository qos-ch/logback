/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package joran.calculator;


import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;

import java.util.EmptyStackException;


/**
 *
 * This action multiplies the two integers at the top of the stack (they are removed)
 * and pushes the result on top the stack.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class MultiplyAction extends Action {
  
  
  public void begin(InterpretationContext ec, String name, Attributes attributes) {
    int first = fetchInteger(ec);
    int second = fetchInteger(ec);
    ec.pushObject(new Integer(first * second));
  }

  /**
   * Pop the Integer object at the top of the stack.
   * This code illustrates usage of Joran's error handling paradigm.
   */
  int fetchInteger(InterpretationContext ec) {
    int result = 0;

    try {
      Object o1 = ec.popObject();

      if (o1 instanceof Integer) {
        result = ((Integer) o1).intValue();
      } else {
        String errMsg =
          "Object [" + o1
          + "] currently at the top of the stack is not an integer.";
        ec.addError(errMsg);
        throw new IllegalArgumentException(errMsg);
      }
    } catch (EmptyStackException ese) {
      ec.addError("Expecting an integer on the execution stack.");
      throw ese;
    }
    return result;
  }

  public void end(InterpretationContext ec, String name) {
    // Nothing to do here.
    // In general, the end() method of actions associated with elements
    // having no children do not need to perform any processing in their
    // end() method.
  }
}
