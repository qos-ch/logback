/**
 * Logback: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package chapter10.calculator;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

/**
 *
 * This action converts the value attribute of the associated element to
 * an integer and pushes the resulting Integer object on top of the execution
 * context stack.
 *
 * It also illustrates usage of Joran's error handling paradigm.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class LiteralAction extends Action {
  public static String VALUE_ATR = "value";

  public void begin(InterpretationContext ec, String name, Attributes attributes) {
    String valueStr = attributes.getValue(VALUE_ATR);

    if (OptionHelper.isEmpty(valueStr)) {
      ec.addError("The literal action requires a value attribute");
      return;
    }

    try {
      Integer i = Integer.valueOf(valueStr);
      ec.pushObject(i);
    } catch (NumberFormatException nfe) {
      ec.addError("The value [" + valueStr + "] could not be converted to an Integer",
          nfe);
      throw nfe;
    }
  }

  public void end(InterpretationContext ec, String name) {
    // Nothing to do here.
    // In general, the end() method of actions associated with elements
    // having no children do not need to perform any processing in their
    // end() method.
  }
}
