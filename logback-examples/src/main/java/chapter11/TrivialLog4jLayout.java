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

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;


/**
 * 
 * A very simple log4j layout which formats a logging event
 * by returning the message contained therein.
 * 
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public class TrivialLog4jLayout extends Layout {

  public void activateOptions() {
    // there are no options to activate
  }

  public String format(LoggingEvent loggingEvent) {
    return loggingEvent.getRenderedMessage();
  }

  @Override
  public boolean ignoresThrowable() {
    return true;
  }

}
