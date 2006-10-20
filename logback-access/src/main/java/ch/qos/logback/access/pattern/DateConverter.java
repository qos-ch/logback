/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.access.pattern;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.CoreGlobal;



public class DateConverter extends AccessConverter {

  long lastTimestamp = -1;
  String timesmapStr = null;
  SimpleDateFormat simpleFormat = null;
  
  public void start() {
    
    String datePattern = getFirstOption();
    if(datePattern == null) {
      datePattern = CoreGlobal.CLF_DATE_PATTERN;
    }
    
    if (datePattern.equals(CoreGlobal.ISO8601_FORMAT)) {
      datePattern = CoreGlobal.ISO8601_PATTERN;
    } else if (datePattern.equals(CoreGlobal.DATE_AND_TIME_FORMAT)) {
      datePattern = CoreGlobal.DATE_AND_TIME_PATTERN;
    } else if (datePattern.equals(CoreGlobal.ABSOLUTE_FORMAT)) {
      datePattern = CoreGlobal.ABSOLUTE_PATTERN;
    }
    
    try {
      simpleFormat = new SimpleDateFormat(datePattern);
      //maximumCacheValidity = CachedDateFormat.getMaximumCacheValidity(pattern);
    } catch (IllegalArgumentException e) {
      addWarn(
        "Could not instantiate SimpleDateFormat with pattern " + datePattern, e);
      // default to the ISO8601 format
      simpleFormat = new SimpleDateFormat(CoreGlobal.CLF_DATE_PATTERN);
    }
    
    List optionList = getOptionList();
    
    // if the option list contains a TZ option, then set it.
    if (optionList != null && optionList.size() > 1) {
      TimeZone tz = TimeZone.getTimeZone((String) optionList.get(1));
      simpleFormat.setTimeZone(tz);
    }
  }
  

  public String convert(AccessEvent accessEvent) {
  
    long timestamp = accessEvent.getTimeStamp();
    
    // if called multiple times within the same millisecond
    // return old value
    if(timestamp == lastTimestamp) {
      return timesmapStr;
    } else {
      lastTimestamp = timestamp;
      timesmapStr = simpleFormat.format(new Date(timestamp));
      return timesmapStr;
    }
  }
}
