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
import ch.qos.logback.core.CoreGlobal;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.util.OptionHelper;


import java.util.HashMap;

public class AppenderRefAction extends Action {
  boolean inError = false;

  public void begin(InterpretationContext ec, String tagName, Attributes attributes) {
    // Let us forget about previous errors (in this object)
    inError = false;

    // logger.debug("begin called");

    Object o = ec.peekObject();

    if (!(o instanceof AppenderAttachable)) {
      String errMsg = "Could not find an AppenderAttachable at the top of execution stack. Near ["
          + tagName + "] line " + getLineNumber(ec);
      inError = true;
      addError(errMsg);
      return;
    }

    AppenderAttachable appenderAttachable = (AppenderAttachable) o;

    String appenderName = attributes.getValue(ActionConst.REF_ATTRIBUTE);

    if (OptionHelper.isEmpty(appenderName)) {
      // print a meaningful error message and return
      String errMsg = "Missing appender ref attribute in <appender-ref> tag.";
      inError = true;
      addError(errMsg);

      return;
    }

    HashMap appenderBag = (HashMap) ec.getObjectMap().get(
        ActionConst.APPENDER_BAG);
    Appender appender = (Appender) appenderBag.get(appenderName);

    if (appender == null) {
      String msg = "Could not find an appender named [" + appenderName
          + "]. Did you define it below in the config file?";
      inError = true;
      addError(msg);
      addError("See " + CoreGlobal.CODES_HREF
          + "#appender_order for more details.");
      return;
    }

    addInfo("Attaching appender named [" + appenderName + "] to "
        + appenderAttachable);
    appenderAttachable.addAppender(appender);
  }

  public void end(InterpretationContext ec, String n) {
  }

  public void finish(InterpretationContext ec) {
  }
}
