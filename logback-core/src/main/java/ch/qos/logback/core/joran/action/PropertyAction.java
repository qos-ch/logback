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

import ch.qos.logback.core.joran.spi.ExecutionContext;
import ch.qos.logback.core.pattern.util.EscapeUtil;
import ch.qos.logback.core.util.OptionHelper;


import java.io.FileInputStream;
import java.io.IOException;

import java.util.Properties;

/**
 * This class serves as a base for other actions, which similar to the ANT 
 * <property> task which add/set properties of a given object.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
abstract public class PropertyAction extends Action {
  static String INVALID_ATTRIBUTES =
    "In <property> element, either the \"file\" attribute or both the \"name\" and \"value\" attributes must be set.";

  
  abstract void setProperties(ExecutionContext ec, Properties props);
  abstract void setProperty(ExecutionContext ec, String key, String value);
  
  /**
   * Set a new property for the execution context by name, value pair, or adds
   * all the properties found in the given file.
   *
   */
  public void begin(
    ExecutionContext ec, String localName, Attributes attributes) {
    String name = attributes.getValue(NAME_ATTRIBUTE);
    String value = attributes.getValue(NAME_ATTRIBUTE);
    String fileName = attributes.getValue(FILE_ATTRIBUTE);

    if (
      !OptionHelper.isEmpty(fileName)
        && (OptionHelper.isEmpty(name) && OptionHelper.isEmpty(value))) {
      Properties props = new Properties();

      try {
        FileInputStream istream = new FileInputStream(fileName);
        props.load(istream);
        istream.close();
        setProperties(ec, props);
      } catch (IOException e) {
        String errMsg = "Could not read properties file [" + fileName + "].";
        addError(errMsg, e);
        addError("Ignoring configuration file [" + fileName + "].");
    
      }
    } else if (
      !(OptionHelper.isEmpty(name) || OptionHelper.isEmpty(value))
        && OptionHelper.isEmpty(fileName)) {
      value = EscapeUtil.basicEscape(value);
      // now remove both leading and trailing spaces
      value = value.trim();
      setProperty(ec, name, value);
    } else {
      
      addError(INVALID_ATTRIBUTES);
    }
  }

  public void end(ExecutionContext ec, String name) {
  }

  public void finish(ExecutionContext ec) {
  }
}
