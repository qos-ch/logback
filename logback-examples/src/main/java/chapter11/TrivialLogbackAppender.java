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
package chapter11;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class TrivialLogbackAppender extends AppenderBase<ILoggingEvent> {

  @Override
  public void start() {
    if (this.layout == null) {
      addError("No layout set for the appender named [" + name + "].");
      return;
    }
    super.start();
  }

  @Override
  protected void append(ILoggingEvent loggingevent) {
    // note that AppenderBase.doAppend will invoke this method only if
    // this appender was successfully started.
    
    String s = this.layout.doLayout(loggingevent);
    System.out.println(s);
  }

}
