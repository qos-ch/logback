/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.LoggingEvent;

public class FileOfCallerConverter extends ClassicConverter {

  public String convert(LoggingEvent le) {
    CallerData[] cda = le.getCallerData();
    if (cda != null && cda.length > 0) {
      return cda[0].getFileName();
    } else {
      return CallerData.NA;
    }
  }

}
