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

import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.PropertySetter;



public class ParamAction extends Action {
  static String NO_NAME = "No name attribute in <param> element";
  static String NO_VALUE = "No name attribute in <param> element";
  boolean inError = false;

  public void begin(
    InterpretationContext ec, String localName, Attributes attributes) {
    String name = attributes.getValue(NAME_ATTRIBUTE);
    String value = attributes.getValue(VALUE_ATTRIBUTE);

    if (name == null) {
      inError = true;
      addError(NO_NAME);
      return;
    }

    if (value == null) {
      inError = true;
      addError(NO_VALUE);
      return;
    }

    // remove both leading and trailing spaces
    value = value.trim();

    Object o = ec.peekObject();
    PropertySetter propSetter = new PropertySetter(o);
    value = ec.subst(value);

    // allow for variable substitution for name as well
    name = ec.subst(name);

    //getLogger().debug(
    //  "In ParamAction setting parameter [{}] to value [{}].", name, value);
    propSetter.setProperty(name, value);
  }

  public void end(InterpretationContext ec, String localName) {
  }

  public void finish(InterpretationContext ec) {
  }
}
