/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class MDCConverter extends ClassicConverter {

  String key;
  private static final String EMPTY_STRING = "";

  @Override
  public void start() {
    key = getFirstOption();
    super.start();
  }

  @Override
  public void stop() {
    key = null;
    super.stop();
  }

  @Override
  public String convert(ILoggingEvent event) {
    Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();

    if (mdcPropertyMap == null) {
      return EMPTY_STRING;
    }

    if (key == null) {
      // if no key is specified, return all the
      // values present in the MDC, separated with a single space.
      StringBuilder buf = new StringBuilder();
      boolean first = true;
      for(Map.Entry<String, String> entry : mdcPropertyMap.entrySet()) {
        if(first) {
          first = false;
        } else {
          buf.append(", ");
        }
        //format: {testKey=testValue, testKey2=testValue2}
        buf.append(entry.getKey()).append('=').append(entry.getValue());
      }
      return buf.toString();
    }

    String value = event.getMDCPropertyMap().get(key);
    if (value != null) {
      return value;
    } else {
      return EMPTY_STRING;
    }
  }
}
