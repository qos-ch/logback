/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.rolling.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

import ch.qos.logback.core.pattern.DynamicConverter;

/**
 * Returns a date formatted by SimpleDateFormatter.
 * 
 * @author Ceki G&uuml;c&uuml;
 */
public class DateTokenConverter<E> extends DynamicConverter<E> implements MonoTypedConverter {

  /**
   * The conversion word/character with which this converter is registered.
   */
  public final static String CONVERTER_KEY = "d";

  private String datePattern;
  private SimpleDateFormat sdf;

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

  public String toRegex() {
    DatePatternToRegexUtil toRegex = new DatePatternToRegexUtil(datePattern);
    return toRegex.toRegex();
  }
}
