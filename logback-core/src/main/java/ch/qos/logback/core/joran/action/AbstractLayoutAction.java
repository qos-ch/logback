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

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.OptionHelper;



abstract public class AbstractLayoutAction extends Action {
  Layout layout;
  boolean inError = false;

  /**
   * Instantiates an layout of the given class and sets its name.
   *
   */
  public void begin(InterpretationContext ec, String name, Attributes attributes) {
    // Let us forget about previous errors (in this object)
    inError = false;

    String className = attributes.getValue(CLASS_ATTRIBUTE);
    try {
      layout = (Layout)
        OptionHelper.instantiateByClassName(
          className, ch.qos.logback.core.Layout.class);
      
      if(isOfCorrectType(layout)) {
        layout.setContext(this.context);
        //getLogger().debug("Pushing layout on top of the object stack.");
        ec.pushObject(layout);        
      } else {
        inError = true;
        addError("Layout of class ["+className+"] is not of the desired type");
      }

    } catch (Exception oops) {
      inError = true;
      addError("Could not create layout of type " + className + "].", oops);
    }
  }

  /**
   * Is the layout of the desired type?
   * @param layout
   * @return true if the layout is of the correct type
   */
  abstract protected boolean isOfCorrectType(Layout layout);
  
  /**
   * Once the children elements are also parsed, now is the time to activate
   * the appender options.
   */
  public void end(InterpretationContext ec, String e) {
    if (inError) {
      return;
    }

    if (layout instanceof LifeCycle) {
      ((LifeCycle) layout).start();
    }

    Object o = ec.peekObject();

    if (o != layout) {
      addWarn(
        "The object on the top the of the stack is not the layout pushed earlier.");
    } else {
      ec.popObject();

      try {
        //getLogger().debug(
        //  "About to set the layout of the containing appender.");
        Appender appender = (Appender) ec.peekObject();
        appender.setLayout(layout);
      } catch (Exception ex) {
        addError(
          "Could not set the layout for containing appender.", ex);
      }
    }
  }

  public void finish(InterpretationContext ec) {
  }
}
