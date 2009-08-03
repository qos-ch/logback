/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.rolling.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

import ch.qos.logback.core.pattern.DynamicConverter;

/**
 * 
 * @author Ceki G&uuml;c&uuml;
 */
public class DateTokenConverter extends DynamicConverter implements MonoTypedConverter {

  /**
   * The conversion word/character with which this converter is registered.
   */
  public final static String CONVERTER_KEY = "d";

  String datePattern;
  SimpleDateFormat sdf;

  public DateTokenConverter() {
  }

  public void start() {
    this.datePattern = getFirstOption();
    if (this.datePattern == null) {
      this.datePattern = "yyyy-MM-dd";
      ;
    }
    sdf = new SimpleDateFormat(datePattern);
  }

  public String convert(Date date) {
    return sdf.format(date);
  }

  public String convert(Object o) {
    if (o == null) {
      throw new IllegalArgumentException("Null argument forbidden");
    }
    if (o instanceof Date) {
      return convert((Date) o);
    } 
    throw new IllegalArgumentException("Cannot convert "+o+" of type"+o.getClass().getName());
  }

  /**
   * Return the date pattern.
   */
  public String getDatePattern() {
    return datePattern;
  }

  public boolean isApplicable(Object o) {
    return (o instanceof Date);
  }

  String toRegex() {
    return null;
  }
  /**
   * Set the date pattern.
   */
  // public void setDatePattern(String string) {
  // datePattern = string;
  // }
}
