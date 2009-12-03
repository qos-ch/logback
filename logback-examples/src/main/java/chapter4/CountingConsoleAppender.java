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
package chapter4;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;


public class CountingConsoleAppender extends AppenderBase<ILoggingEvent> {
  static int DEFAULT_LIMIT = 10;
  int counter = 0;
  int limit = DEFAULT_LIMIT;
  
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

  public void append(ILoggingEvent event) {
    if (counter >= limit) {
      return;
    }
    // output the events as formatted by our layout
    System.out.print(this.layout.doLayout(event));

    // prepare for next event
    counter++;
  }
}
