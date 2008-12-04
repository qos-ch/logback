/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic.joran.action;

import javax.naming.Context;
import javax.naming.NamingException;

import org.xml.sax.Attributes;

import ch.qos.logback.classic.util.JNDIUtil;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

/**
 * Insert an env-entry found in JNDI as a new context variable  

 * @author Ceki Gulcu
 *
 */
public class InsertFromJNDIAction extends Action {

  public static String ENV_ENTRY_NAME_ATTR="env-entry-name";
  public static String AS_ATTR="as";
  
  public void begin(InterpretationContext ec, String name, Attributes attributes) {

    int errorCount = 0;
    String envEntryName = attributes.getValue(ENV_ENTRY_NAME_ATTR);
    String asName = attributes.getValue(AS_ATTR);
    String envEntryValue;
    
    if(OptionHelper.isEmpty(envEntryName)) {
      String lineColStr = getLineColStr(ec);
      addError("["+ENV_ENTRY_NAME_ATTR+"] missing, around "+lineColStr);
      errorCount++;
    }
    
    if(OptionHelper.isEmpty(asName)) {
      String lineColStr = getLineColStr(ec);
      addError("["+AS_ATTR+"] missing, around "+lineColStr);
      errorCount++;
    }
    
    if(errorCount != 0) {
      return;
    }
    
    try {
      Context ctx = JNDIUtil.getInitialContext();
      envEntryValue = JNDIUtil.lookup(ctx, envEntryName);
      if(OptionHelper.isEmpty(envEntryValue)) {
        addError("["+envEntryName+"] has null or empty value");
      } else {
        addInfo("Setting context variable ["+asName+"] to ["+envEntryValue+"]");
        context.putProperty(asName, envEntryValue);
      }
    } catch (NamingException e) {
      addError("Failed to lookup JNDI env-entry ["+envEntryName+"]");
    }
    
    
  }

  public void end(InterpretationContext ec, String name) {
  }
}
