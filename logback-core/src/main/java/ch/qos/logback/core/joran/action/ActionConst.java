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

/**
 *
 * This class contains costants used by other Actions.
 * 
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public abstract class ActionConst {
	
  public static final String APPENDER_TAG = "appender";
  public static final String REF_ATTRIBUTE = "ref";
  public static final String ADDITIVITY_ATTRIBUTE = "additivity";
  public static final String LEVEL_ATTRIBUTE = "level";
  public static final String CONVERTER_CLASS_ATTRIBUTE = "converterClass";
  public static final String CONVERSION_WORD_ATTRIBUTE = "conversionWord";
  public static final String PATTERN_ATTRIBUTE = "pattern";
  public static final String VALUE_ATTR = "value";
  public static final String ACTION_CLASS_ATTRIBUTE = "actionClass";

  public static final String INHERITED = "INHERITED";
  public static final String NULL = "NULL";
  static final Class<?>[] ONE_STRING_PARAM = new Class[] { String.class };

  public static final String APPENDER_BAG = "APPENDER_BAG";
  public static final String FILTER_CHAIN_BAG = "FILTER_CHAIN_BAG";
}
