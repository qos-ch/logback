/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
