/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic.db;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableDataPoint;

/**
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class DBHelper {

  public static short PROPERTIES_EXIST = 0x01;
  public static short EXCEPTION_EXISTS = 0x02;

  public static short computeReferenceMask(LoggingEvent event) {
    short mask = 0;

    int mdcPropSize = 0;
    if (event.getMDCPropertyMap() != null) {
      mdcPropSize = event.getMDCPropertyMap().keySet().size();
    }
    int contextPropSize = 0;
    if (event.getLoggerRemoteView().getLoggerContextView().getPropertyMap() != null) {
      contextPropSize = event.getLoggerRemoteView().getLoggerContextView()
          .getPropertyMap().size();
    }

    if (mdcPropSize > 0 || contextPropSize > 0) {
      mask = PROPERTIES_EXIST;
    }
    if (event.getThrowableProxy() != null) {
      ThrowableDataPoint[] tdpArray = event.getThrowableProxy().getThrowableDataPointArray();
      if (tdpArray != null) {
        mask |= EXCEPTION_EXISTS;
      }
    }
    return mask;
  }
}
