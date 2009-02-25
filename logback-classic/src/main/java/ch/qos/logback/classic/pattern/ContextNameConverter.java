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
    return getContext().getName();
  }

}
