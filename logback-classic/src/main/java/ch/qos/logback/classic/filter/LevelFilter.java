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
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * A class that filters events depending on their level.
 * 
 * One can specify a level and the behaviour of the filter when 
 * said level is encountered in a logging event.
 *
 * For more information about filters, please refer to the online manual at
 * http://logback.qos.ch/manual/filters.html
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class LevelFilter extends AbstractMatcherFilter {

  Level level;
  
  @Override
  public FilterReply decide(Object eventObject) {
    if (!isStarted()) {
      return FilterReply.NEUTRAL;
    }
    
    ILoggingEvent event = (ILoggingEvent)eventObject;
    
    if (event.getLevel().equals(level)) {
      return onMatch;
    } else {
      return onMismatch;
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
