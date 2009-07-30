/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.rolling.helper;

import ch.qos.logback.core.pattern.DynamicConverter;

/**
 * When asked to convert an integer, <code>IntegerTokenConverter</code> the
 * string value of that integer.
 * 
 * @author Ceki Gulcu
 */
public class IntegerTokenConverter extends DynamicConverter implements MonoTypedConverter {

  public final static String CONVERTER_KEY = "i";
  
  public IntegerTokenConverter() {
  }

  public String convert(int i) {
    return Integer.toString(i);
  }

  public String convert(Object o) {
    if(o == null) {
      throw new IllegalArgumentException("Null argument forbidden");
    }
    if(o instanceof Integer) {
      Integer i = (Integer) o;
      return convert(i.intValue());
    } 
    throw new IllegalArgumentException("Cannot convert "+o+" of type"+o.getClass().getName());
  }

  public boolean isApplicable(Object o) {
    return (o instanceof Integer);
  }
}
