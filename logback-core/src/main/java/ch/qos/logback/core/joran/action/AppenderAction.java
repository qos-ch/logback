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


import java.util.HashMap;

import org.xml.sax.Attributes;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.OptionHelper;



public class AppenderAction<E> extends Action {
  Appender appender;
  private boolean inError = false;

  /**
   * Instantiates an appender of the given class and sets its name.
   *
   * The appender thus generated is placed in the ExecutionContext appender bag.
   */
  @SuppressWarnings("unchecked")
  public void begin(
    InterpretationContext ec, String localName, Attributes attributes) throws ActionException {
    // We are just beginning, reset variables
    appender = null;
    inError = false;
    
    String className = attributes.getValue(CLASS_ATTRIBUTE);
    if(OptionHelper.isEmpty(className)) {
      addError(
        "Missing class name for appender. Near ["
          + localName + "] line " + getLineNumber(ec));
      inError = true;
      return;
    }

    
    try {
      addInfo("About to instantiate appender of type ["+className+"]");

      appender = (Appender) OptionHelper.instantiateByClassName(
          className, ch.qos.logback.core.Appender.class, context);

      appender.setContext(context);

      String appenderName = attributes.getValue(NAME_ATTRIBUTE);

      if (OptionHelper.isEmpty(appenderName)) {
        addWarn(
          "No appender name given for appender of type " + className + "].");
      } else {
        appender.setName(appenderName);
        addInfo("Naming appender as [" + appenderName + "]");
      }

      // The execution context contains a bag which contains the appenders
      // created thus far.
      HashMap<String, Appender> appenderBag =
        (HashMap) ec.getObjectMap().get(ActionConst.APPENDER_BAG);

      // add the appender just created to the appender bag.
      appenderBag.put(appenderName, appender);

      ec.pushObject(appender);
    } catch (Exception oops) {
      inError = true;
      addError(
        "Could not create an Appender of type ["+className+"].", oops);
      throw new ActionException(oops);
    }
  }

  /**
   * Once the children elements are also parsed, now is the time to activate
   * the appender options.
   */
  public void end(InterpretationContext ec, String name) {
    if (inError) {
      return;
    }

    if (appender instanceof LifeCycle) {
      ((LifeCycle) appender).start();
    }

    Object o = ec.peekObject();

    if (o != appender) {
      addWarn(
        "The object at the of the stack is not the appender named ["
        + appender.getName() + "] pushed earlier.");
    } else {
      addInfo(
        "Popping appender named [" + appender.getName()
        + "] from the object stack");
      ec.popObject();
    }
  }
}
