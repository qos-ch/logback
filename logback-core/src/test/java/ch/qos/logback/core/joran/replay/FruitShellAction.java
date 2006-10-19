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

public class FruitShellAction extends Action {

  FruitShell fruitShell;
  private boolean inError = false;

  
  @Override
  public void begin(ExecutionContext ec, String name, Attributes attributes)
      throws ActionException {

    // We are just beginning, reset variables
    fruitShell = new FruitShell();
    inError = false;
    
    try {


      fruitShell.setContext(context);

      String shellName = attributes.getValue(NAME_ATTRIBUTE);

      if (OptionHelper.isEmpty(shellName)) {
        addWarn(
          "No appender name given for fruitShell].");
      } else {
        fruitShell.setName(shellName);
        addInfo("FruitShell named as [" + shellName + "]");
      }

      ec.pushObject(fruitShell);
    } catch (Exception oops) {
      inError = true;
      addError(
        "Could not create an FruitShell", oops);
      throw new ActionException(ActionException.SKIP_CHILDREN, oops);
    }
  }

  @Override
  public void end(ExecutionContext ec, String name) throws ActionException {
    if (inError) {
      return;
    }

    Object o = ec.peekObject();

    if (o != fruitShell) {
      addWarn(
        "The object at the of the stack is not the fruitShell named ["
        + fruitShell.getName() + "] pushed earlier.");
    } else {
      addInfo(
        "Popping fruitSHell named [" + fruitShell.getName()
        + "] from the object stack");
      ec.popObject();
      FruitContext fruitContext = (FruitContext) ec.getContext();
      fruitContext.addFruitShell(fruitShell);
    }
  }

  
}
