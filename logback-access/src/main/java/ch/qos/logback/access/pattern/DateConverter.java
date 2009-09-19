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
package ch.qos.logback.access.pattern;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.CoreConstants;



public class DateConverter extends AccessConverter {

  long lastTimestamp = -1;
  String timesmapStr = null;
  SimpleDateFormat simpleFormat = null;
  
  public void start() {
    
    String datePattern = getFirstOption();
    if(datePattern == null) {
      datePattern = CoreConstants.CLF_DATE_PATTERN;
    }
    
    if (datePattern.equals(CoreConstants.ISO8601_STR)) {
      datePattern = CoreConstants.ISO8601_PATTERN;
    } 
    
    try {
      simpleFormat = new SimpleDateFormat(datePattern);
      //maximumCacheValidity = CachedDateFormat.getMaximumCacheValidity(pattern);
    } catch (IllegalArgumentException e) {
      addWarn(
        "Could not instantiate SimpleDateFormat with pattern " + datePattern, e);
      // default to the ISO8601 format
      simpleFormat = new SimpleDateFormat(CoreConstants.CLF_DATE_PATTERN);
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
