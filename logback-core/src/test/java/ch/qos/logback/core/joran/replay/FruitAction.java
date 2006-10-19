/**
 * Logback: the generic, reliable, fast and flexible logging framework for Java.
 * 
 * Copyright (C) 2000-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.joran.replay;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.ExecutionContext;
import ch.qos.logback.core.util.OptionHelper;

public class FruitAction extends Action {

  Fruit fruit;
  private boolean inError = false;

  @Override
  public void begin(ExecutionContext ec, String name, Attributes attributes)
      throws ActionException {
    String className = attributes.getValue(CLASS_ATTRIBUTE);

    // We are just beginning, reset variables
    fruit = null;
    inError = false;

    try {
      addInfo("About to instantiate fruit of type ["+className+"]");

      fruit = (Fruit) OptionHelper.instantiateByClassName(
          className, ch.qos.logback.core.joran.replay.Fruit.class);

      String fruitName = attributes.getValue(NAME_ATTRIBUTE);

      if (OptionHelper.isEmpty(fruitName)) {
        addWarn(
          "No fruit name given for fruit of type " + className + "].");
      } else {
        fruit.setName(fruitName);
        addInfo("Fruit named as [" + fruitName + "]");
      }

      ec.pushObject(fruit);
    } catch (Exception oops) {
      inError = true;
      addError(
        "Could not create an Fruit of type ["+className+"].", oops);
      throw new ActionException(ActionException.SKIP_CHILDREN, oops);
    }
    
  }

  @Override
  public void end(ExecutionContext ec, String name) throws ActionException {
    if (inError) {
      return;
    }

    Object o = ec.peekObject();

    if (o != fruit) {
      addWarn(
        "The object at the of the stack is not the fruit named ["
        + fruit.getName() + "] pushed earlier.");
    } else {
      addInfo(
        "Popping fruit named [" + fruit.getName()
        + "] from the object stack");
      ec.popObject();
    }
  }


}
