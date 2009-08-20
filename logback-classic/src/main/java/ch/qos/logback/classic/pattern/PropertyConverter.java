/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.pattern;

import java.util.Map;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextVO;

public final class PropertyConverter extends ClassicConverter {

  String key;

  public void start() {
    String optStr = getFirstOption();
    if (optStr != null) {
      key = optStr;
      super.start();
    }
  }

  public String convert(ILoggingEvent event) {
    if (key == null) {
      return "Property_HAS_NO_KEY";
    } else {
      LoggerContextVO lcvo = event.getLoggerContextVO();
      Map<String, String> map = lcvo.getPropertyMap();
      String val = map.get(key);
      if (val != null) {
        return val;
      } else {
        return System.getProperty(key);
      }
    }
  }
}
