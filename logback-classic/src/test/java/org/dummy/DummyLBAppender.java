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
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class DummyLBAppender extends AppenderBase<ILoggingEvent> {

  public List<ILoggingEvent> list = new ArrayList<ILoggingEvent>();
  public List<String> stringList = new ArrayList<String>();
  
  PatternLayout layout;
  
  DummyLBAppender() {
    this(null);
  }
  
  DummyLBAppender(PatternLayout layout) {
    this.layout = layout;
  }
  
  protected void append(ILoggingEvent e) {
    list.add(e);
    if(layout != null) {
      String s = layout.doLayout(e);
      stringList.add(s);
    }
  }
}
