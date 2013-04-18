/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.issue.lbclassic203;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class InstanceCountingAppender extends AppenderBase<ILoggingEvent> {
  
  static public volatile int INSTANCE_COUNT = 0;

  public InstanceCountingAppender() {
    INSTANCE_COUNT++;
  }

  protected void append(ILoggingEvent e) {
  }
  
}
