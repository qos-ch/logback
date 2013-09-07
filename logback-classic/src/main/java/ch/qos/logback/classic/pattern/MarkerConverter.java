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

import org.slf4j.Marker;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Return the event's marker value(s).
 * 
 * @author S&eacute;bastien Pennec
 */
public class MarkerConverter extends ClassicConverter {

  private static String EMPTY = "";

  public String convert(ILoggingEvent le) {
    Marker marker = le.getMarker();
    if (marker == null) {
      return EMPTY;
    } else {
      return marker.toString();
    }
  }

}
