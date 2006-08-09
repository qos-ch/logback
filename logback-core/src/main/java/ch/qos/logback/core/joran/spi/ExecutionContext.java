/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.joran.spi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import org.xml.sax.Locator;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.WarnStatus;
import ch.qos.logback.core.util.OptionHelper;


/**
 * 
 * The ExecutionContext contains the contextual state of a Joran parsing
 * session. {@link Action} objects depend on this context to exchange 
 * and store information.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class ExecutionContext extends ContextAwareBase {
  Stack<Object> objectStack;
  Map<String, Object> objectMap;
  Properties substitutionProperties;
  Interpreter joranInterpreter;

  public ExecutionContext(Interpreter joranInterpreter) {
    this.joranInterpreter = joranInterpreter;
    objectStack = new Stack<Object> ();
    objectMap = new HashMap<String, Object>(5);
    substitutionProperties = new Properties();
  }

  // /**
  // * Clear the internal structures for reuse of the execution context
  // *
  // */
  // public void clear() {
  // objectStack.clear();
  // objectMap.clear();
  // errorList.clear();
  // substitutionProperties.clear();
  // }

  public void addError(String msg, Object origin) {
    msg = updateLocationInfo(msg);
    addStatus(new ErrorStatus(msg, origin));
  }

  public void addError(String msg, Object origin, Exception e) {
    msg = updateLocationInfo(msg);
    addStatus(new ErrorStatus(msg, origin, e));
  }

  public void addWarn(String msg, Object origin) {
    msg = updateLocationInfo(msg);
    addStatus(new WarnStatus(msg, origin));
  }

  public void addWarn(String msg, Object origin, Exception e) {
    msg = updateLocationInfo(msg);
    addStatus(new WarnStatus(msg, origin, e));
  }
  
  public void addInfo(String msg, Object origin) {
    msg = updateLocationInfo(msg);
    addStatus(new InfoStatus(msg, origin));
  }

  
  String updateLocationInfo(String msg) {
    Locator locator = joranInterpreter.getLocator();

    if (locator != null) {
      return msg + locator.getLineNumber() + ":" + locator.getColumnNumber();
    } else {
      return msg;
    }
  }
  
  public Locator getLocator() {
    return joranInterpreter.getLocator();
  }

  public Interpreter getJoranInterpreter() {
    return joranInterpreter;
  }

  public Stack<Object> getObjectStack() {
    return objectStack;
  }

  public Object peekObject() {
    return objectStack.peek();
  }

  public void pushObject(Object o) {
    objectStack.push(o);
  }

  public Object popObject() {
    return objectStack.pop();
  }

  public Object getObject(int i) {
    return objectStack.get(i);
  }

  public Map<String, Object> getObjectMap() {
    return objectMap;
  }

  /**
   * Add a property to the properties of this execution context. If the property
   * exists already, it is overwritten.
   */
  public void addProperty(String key, String value) {
    if (key == null || value == null) {
      return;
    }
    // if (substitutionProperties.contains(key)) {
    // LogLog.warn(
    // "key [" + key
    // + "] already contained in the EC properties. Overwriting.");
    // }

    // values with leading or trailing spaces are bad. We remove them now.
    value = value.trim();
    substitutionProperties.put(key, value);
  }

  public void addProperties(Properties props) {
    if (props == null) {
      return;
    }
    Iterator i = props.keySet().iterator();
    while (i.hasNext()) {
      String key = (String) i.next();
      addProperty(key, props.getProperty(key));
    }
  }

  public String getSubstitutionProperty(String key) {
    return substitutionProperties.getProperty(key);
  }

  public String subst(String value) {
    if (value == null) {
      return null;
    }
    return OptionHelper.substVars(value, substitutionProperties);
  }
}
