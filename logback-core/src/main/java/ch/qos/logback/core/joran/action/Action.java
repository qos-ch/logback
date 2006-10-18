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


import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.ExecutionContext;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.spi.ContextAwareBase;



/**
 *
 * Most of the work for configuring log4j is done by Actions.
 *
 * Methods of an Action are invoked while an XML file is parsed through.
 *
 * This class is largely copied from the relevant class in the commons-digester
 * project of the Apache Software Foundation.
 *
 * @author Craig McClanahan
 * @author Christopher Lenz
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public abstract class Action extends ContextAwareBase {
  
  public static final String NAME_ATTRIBUTE = "name";
  public static final String VALUE_ATTRIBUTE = "value";
  public static final String FILE_ATTRIBUTE = "file";
  public static final String CLASS_ATTRIBUTE = "class";
  public static final String PATTERN_ATTRIBUTE = "pattern";
  public static final String ACTION_CLASS_ATTRIBUTE = "actionClass";

  /**
   * Called when the parser first encounters an element.
   *
   * The return value indicates whether child elements should be processed. If
   * the returned value is 'false', then child elements are ignored.
   */
  public abstract void begin(
    ExecutionContext ec, String name, Attributes attributes) throws ActionException ;


  public void body(ExecutionContext ec, String body) throws ActionException {
    // NOP
  }

  public abstract void end(ExecutionContext ec, String name) throws ActionException;

  public String toString() {
    return this.getClass().getName();
  }

  protected int getColumnNumber(ExecutionContext ec) {
    Interpreter jp = ec.getJoranInterpreter();
    Locator locator = jp.getLocator();
    if (locator != null) {
      return locator.getColumnNumber();
    }
    return -1;
  }

  protected int getLineNumber(ExecutionContext ec) {
    Interpreter jp = ec.getJoranInterpreter();
    Locator locator = jp.getLocator();
    if (locator != null) {
      return locator.getLineNumber();
    }
    return -1;
  }
}
