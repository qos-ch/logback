/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package org.slf4j.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class RecursiveLBAppender extends AppenderBase<ILoggingEvent> {

  public List<ILoggingEvent> list = new ArrayList<ILoggingEvent>();
  public List<String> stringList = new ArrayList<String>();
  
  PatternLayout layout;
  
  public RecursiveLBAppender() {
    this(null);
  }
  
  public RecursiveLBAppender(PatternLayout layout) {
    this.layout = layout;
  }
  
  @Override
  public void start() {
    int diff = new Random().nextInt();
    Logger logger = LoggerFactory.getLogger("ResursiveLBAppender"+diff);
    logger.info("testing");
    super.start();
  }
  
  protected void append(ILoggingEvent e) {
    list.add(e);
    if(layout != null) {
      String s = layout.doLayout(e);
      stringList.add(s);
    }
  }
}
