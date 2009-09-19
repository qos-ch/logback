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
package ch.qos.logback.classic.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * A class that filters events depending on their level.
 * 
 * All events with a level under or above the specified
 * level will be denied, while all events with a level
 * equal or above the specified level will trigger a
 * FilterReply.NEUTRAL result, to allow the rest of the
 * filter chain process the event.
 * 
 * For more information about filters, please refer to the online manual at
 * http://logback.qos.ch/manual/filters.html
 *
 * @author S&eacute;bastien Pennec
 */
public class ThresholdFilter extends Filter {

  Level level;
  
  @Override
  public FilterReply decide(Object eventObject) {
    if (!isStarted()) {
      return FilterReply.NEUTRAL;
    }
    
    ILoggingEvent event = (ILoggingEvent)eventObject;
    
    if (event.getLevel().isGreaterOrEqual(level)) {
      return FilterReply.NEUTRAL;
    } else {
      return FilterReply.DENY;
    }
  }
  
  public void setLevel(String level) {
    this.level = Level.toLevel(level);
  }
  
  public void start() {
    if (this.level != null) {
      super.start();
    }
  }
}
