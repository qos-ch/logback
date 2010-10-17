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
package ch.qos.logback.classic.db;

import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class DBHelper {

  public static short PROPERTIES_EXIST = 0x01;
  public static short EXCEPTION_EXISTS = 0x02;

  public static short computeReferenceMask(ILoggingEvent event) {
    short mask = 0;

    int mdcPropSize = 0;
    if (event.getMDCPropertyMap() != null) {
      mdcPropSize = event.getMDCPropertyMap().keySet().size();
    }
    int contextPropSize = 0;
    if (event.getLoggerContextVO().getPropertyMap() != null) {
      contextPropSize = event.getLoggerContextVO().getPropertyMap().size();
    }

    if (mdcPropSize > 0 || contextPropSize > 0) {
      mask = PROPERTIES_EXIST;
    }
    if (event.getThrowableProxy() != null) {
      mask |= EXCEPTION_EXISTS;
    }
    return mask;
  }

  public static String truncateTo254(final String s) {
    if(s == null) {
      return null;
    }
    if(s.length() <= 254) {
      return s;
    } else {
      return s.substring(0, 254);
    }
  }

  public static Map<String, String> mergePropertyMaps(ILoggingEvent event) {
    Map<String, String> mergedMap = new HashMap<String, String>();
    // we add the context properties first, then the event properties, since
    // we consider that event-specific properties should have priority over
    // context-wide
    // properties.
    Map<String, String> loggerContextMap = event.getLoggerContextVO()
        .getPropertyMap();
    Map<String, String> mdcMap = event.getMDCPropertyMap();
    if (loggerContextMap != null) {
      mergedMap.putAll(loggerContextMap);
    }
    if (mdcMap != null) {
      mergedMap.putAll(mdcMap);
    }

    return mergedMap;
  }
}
