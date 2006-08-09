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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ActionConst;
import ch.qos.logback.core.joran.spi.ExecutionContext;
import ch.qos.logback.core.util.OptionHelper;



public class LoggerAction extends Action {
  boolean inError = false;
  
  public void begin(ExecutionContext ec, String name, Attributes attributes) {
    // Let us forget about previous errors (in this object)
    inError = false;

    LoggerContext loggerContext = (LoggerContext) this.context;

    // Create a new org.apache.log4j.Category object from the <category> element.
    String loggerName = attributes.getValue(NAME_ATTRIBUTE);

    if (OptionHelper.isEmpty(loggerName)) {
      inError = true;

      String line =
        ", around line " + getLineNumber(ec) + " column "
        + getColumnNumber(ec);

      String errorMsg = "No 'name' attribute in element " + name + line;
      addError(errorMsg);

      return;
    }

    //getLogger().debug("Logger name is [" + loggerName + "].");

    Logger l = loggerContext.getLogger(loggerName);
    

    boolean additive =
      OptionHelper.toBoolean(
        attributes.getValue(ActionConst.ADDITIVITY_ATTRIBUTE), true);
    //getLogger().debug(
    //  "Setting [" + l.getName() + "] additivity to [" + additivity + "].");
    l.setAdditive(additive);

    //getLogger().debug("Pushing logger named [" + loggerName + "].");
    ec.pushObject(l);
  }

  public void end(ExecutionContext ec, String e) {
    if (!inError) {
      ec.popObject();
    }
  }

  public void finish(ExecutionContext ec) {
  }
}
