/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package org.dummy;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class DummyLBAppender extends AppenderBase<LoggingEvent> {

  public List<LoggingEvent> list = new ArrayList<LoggingEvent>();
  public List<String> stringList = new ArrayList<String>();
  
  PatternLayout layout;
  
  DummyLBAppender() {
    this(null);
  }
  
  DummyLBAppender(PatternLayout layout) {
    this.layout = layout;
  }
  
  protected void append(LoggingEvent e) {
    list.add(e);
    if(layout != null) {
      String s = layout.doLayout(e);
      stringList.add(s);
    }
  }
}
