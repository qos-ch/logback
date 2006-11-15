/**
 * Logback: the generic, reliable, fast and flexible logging framework for Java.
 * 
 * Copyright (C) 2000-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Duration {

  private final static String DOUBLE_PART = "([0-9]*(.[0-9]+)?)";
  private final static int DOUBLE_GROUP = 1;

  private final static String UNIT_PART = "(millisecond|second|minute|hour|day)s?";
  private final static int UNIT_GROUP = 3;

  private static final Pattern DURATION_PATTERN = Pattern.compile(DOUBLE_PART
      + "\\s*" + UNIT_PART, Pattern.CASE_INSENSITIVE);

  static final long SECONDS_COEFFICIENT = 1000;
  static final long MINUTES_COEFFICIENT = 60 * SECONDS_COEFFICIENT;
  static final long HOURS_COEFFICIENT = 60 * MINUTES_COEFFICIENT;
  static final long DAYS_COEFFICIENT = 24 * HOURS_COEFFICIENT;

  final long millis;

  Duration(long millis) {
    this.millis = millis;
  }

  Duration buildByMilliseconds(double value) {
    return new Duration((long) (value));
  }
  
  Duration buildBySeconds(double value) {
    return new Duration((long) (SECONDS_COEFFICIENT*value));
  }

  Duration buildByMinutes(double value) {
    return new Duration((long) (MINUTES_COEFFICIENT*value));
  }
  
  Duration buildByHours(double value) {
    return new Duration((long) (HOURS_COEFFICIENT*value));
  }

  Duration buildByDays(double value) {
    return new Duration((long) (DAYS_COEFFICIENT*value));
  }
  
  
  public long getMilliSeconds() {
    return millis;
  }

  public static Duration valueOf(String durationStr) {
    Matcher matcher = DURATION_PATTERN.matcher(durationStr);

    long coefficient;
    if (matcher.matches()) {
      String doubleStr = matcher.group(DOUBLE_GROUP);
      String unitStr = matcher.group(UNIT_GROUP);

      double doubleValue = Double.valueOf(doubleStr);
      if (unitStr.equalsIgnoreCase("millisecond")) {
        coefficient = 1;
      } else if (unitStr.equalsIgnoreCase("second")) {
        coefficient = SECONDS_COEFFICIENT;
      } else if (unitStr.equalsIgnoreCase("minute")) {
        coefficient = MINUTES_COEFFICIENT;
      } else if (unitStr.equalsIgnoreCase("hour")) {
        coefficient = HOURS_COEFFICIENT;
      } else if (unitStr.equalsIgnoreCase("day")) {
        coefficient = DAYS_COEFFICIENT;
      } else {
        throw new IllegalStateException("Unexpected" + unitStr);
      }
      return new Duration((long) (doubleValue * coefficient));
    } else {
      throw new IllegalArgumentException("String value [" + durationStr
          + "] is not in the expected format.");
    }

  }
}
