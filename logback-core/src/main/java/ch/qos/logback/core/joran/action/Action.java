/**
 * Logback: the generic, reliable, fast and flexible logging framework for Java.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * 
 * Most of the work for configuring logback is done by Actions.
 * 
 * <p>Action methods are invoked as the XML file is parsed.
 * 
 * <p>This class is largely inspired from the relevant class in the
 * commons-digester project of the Apache Software Foundation.
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
   * Called when the parser encounters an element matching a
   * {@link ch.qos.logback.core.joran.spi.Pattern Pattern}.
   */
  public abstract void begin(InterpretationContext ec, String name,
      Attributes attributes) throws ActionException;

  public void body(InterpretationContext ec, String body)
      throws ActionException {
    // NOP
  }

  /*
   * Called when the parser encounters an endElement event matching a
   * {@link ch.qos.logback.core.joran.spi.Pattern Pattern}.
   */
  public abstract void end(InterpretationContext ec, String name)
      throws ActionException;

  public String toString() {
    return this.getClass().getName();
  }

  protected int getColumnNumber(InterpretationContext ec) {
    Interpreter jp = ec.getJoranInterpreter();
    Locator locator = jp.getLocator();
    if (locator != null) {
      return locator.getColumnNumber();
    }
    return -1;
  }

  protected int getLineNumber(InterpretationContext ec) {
    Interpreter jp = ec.getJoranInterpreter();
    Locator locator = jp.getLocator();
    if (locator != null) {
      return locator.getLineNumber();
    }
    return -1;
  }

  protected String getLineColStr(InterpretationContext ec) {
    String line = "line: " + getLineNumber(ec) + ", column: "
        + getColumnNumber(ec);
    return line;
  }
}
