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

public final class ContextPropertyConverter extends ClassicConverter {

  String propertyName;

  public void start() {
    String optStr = getFirstOption();
    if (optStr != null) {
      propertyName = optStr;
      super.start();
    }
  }

  public String convert(ILoggingEvent event) {
    if(propertyName == null) {
      return "ContextProperty_HAS_NO_NAME";
    } else {
      LoggerContextVO lcvo = event.getLoggerContextVO();
      Map<String, String> map = lcvo.getPropertyMap();
      return map.get(propertyName);
    }
  }
}
