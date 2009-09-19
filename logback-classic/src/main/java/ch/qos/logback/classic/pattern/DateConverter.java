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
package ch.qos.logback.classic.pattern;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;

public class DateConverter extends ClassicConverter {

  long lastTimestamp = -1;
  String timestampStrCache = null;
  SimpleDateFormat simpleFormat = null;

  public void start() {

    String datePattern = getFirstOption();
    if (datePattern == null) {
      datePattern = CoreConstants.ISO8601_PATTERN;
    }

    if (datePattern.equals(CoreConstants.ISO8601_STR)) {
      datePattern = CoreConstants.ISO8601_PATTERN;
    }

    try {
      simpleFormat = new SimpleDateFormat(datePattern);
      // maximumCacheValidity =
      // CachedDateFormat.getMaximumCacheValidity(pattern);
    } catch (IllegalArgumentException e) {
      addWarn("Could not instantiate SimpleDateFormat with pattern "
          + datePattern, e);
      // default to the ISO8601 format
      simpleFormat = new SimpleDateFormat(CoreConstants.ISO8601_PATTERN);
    }

    List optionList = getOptionList();

    // if the option list contains a TZ option, then set it.
    if (optionList != null && optionList.size() > 1) {
      TimeZone tz = TimeZone.getTimeZone((String) optionList.get(1));
      simpleFormat.setTimeZone(tz);
    }
  }

  public String convert(ILoggingEvent le) {
    long timestamp = le.getTimeStamp();

    synchronized (this) {
      // if called multiple times within the same millisecond
      // return cache value
      if (timestamp == lastTimestamp) {
        return timestampStrCache;
      } else {
        lastTimestamp = timestamp;
        // SimpleDateFormat is not thread safe. 
        // See also http://jira.qos.ch/browse/LBCLASSIC-36
        timestampStrCache = simpleFormat.format(new Date(timestamp));
        return timestampStrCache;
      }
    }
  }
}
