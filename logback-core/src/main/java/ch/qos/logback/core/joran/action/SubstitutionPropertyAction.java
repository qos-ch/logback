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

import java.util.Properties;

import ch.qos.logback.core.joran.spi.ExecutionContext;

/**
 * This action sets new substitution properties for the execution context by 
 * name, value pair, or adds all the properties passed in the {@link Properties}
 * argument.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class SubstitutionPropertyAction extends PropertyAction {

  public void setProperties(ExecutionContext ec, Properties props) {
    ec.addProperties(props);
  }
  
  public void setProperty(ExecutionContext ec, String key, String value) {
    ec.addProperty(key, value);
  }
}
