/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package chapter11;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.LayoutBase;


/**
 * 
 * A very simple logback-classic layout which formats a logging event
 * by returning the message contained therein.
 * 
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public class TrivialLogbackLayout extends LayoutBase<LoggingEvent> {

  public String doLayout(LoggingEvent loggingEvent) {
    return loggingEvent.getMessage();
  }
}
