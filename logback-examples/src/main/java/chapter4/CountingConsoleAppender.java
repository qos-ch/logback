/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package chapter4;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;


public class CountingConsoleAppender extends AppenderBase {
  static int DEFAULT_LIMIT = 10;
  int counter = 0;
  int limit = DEFAULT_LIMIT;
  
  private Layout layout;

  public CountingConsoleAppender() {
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public int getLimit() {
    return limit;
  }  
  
  @Override
  public void start() {
    if (this.layout == null) {
      addError("No layout set for the appender named ["+ name +"].");
      return;
    }
    
    super.start();
  }

  public void append(Object eventObject) {

    if (counter >= limit) {
      return;
    }

    // output the events as formatted by our layout
    System.out.print(this.layout.doLayout(eventObject));

    // prepare for next event
    counter++;
  }

  public Layout getLayout() {
    return layout;
  }

  public void setLayout(Layout layout) {
    this.layout = layout;
  }
}
