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

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Converts an event to the logger context's name.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class ContextNameConverter extends ClassicConverter {

  /**
   * Return the name of the logger context's name.
   */
  public String convert(ILoggingEvent event) {
    return event.getLoggerContextVO().getName();
  }

}
