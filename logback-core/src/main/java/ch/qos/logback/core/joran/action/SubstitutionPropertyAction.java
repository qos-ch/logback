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

import ch.qos.logback.core.joran.spi.InterpretationContext;

/**
 * This action sets new substitution properties for the execution context by 
 * name, value pair, or adds all the properties passed in the {@link Properties}
 * argument.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class SubstitutionPropertyAction extends PropertyAction {

  public void setProperties(InterpretationContext ec, Properties props) {
    ec.addProperties(props);
//    for(Object o: props.keySet()) {
//      String key = (String) o;
//      ec.getContext().setProperty(key, props.getProperty(key));
//    }
  }
  
  public void setProperty(InterpretationContext ec, String key, String value) {
    ec.addSubstitutionProperty(key, value);
    //ec.getContext().setProperty(key, value);
  }
}
