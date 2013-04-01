/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.net.server;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * A mock {@link AppenderBase} with instrumentation for unit testing.
 *
 * @author Carl Harris
 */
class MockAppender extends AppenderBase<ILoggingEvent> {

  private int eventCount;
  private ILoggingEvent lastEvent;
  
  @Override
  protected void append(ILoggingEvent eventObject) {
    eventCount++;
    lastEvent = eventObject;
  }

  public int getEventCount() {
    return eventCount;
  }

  public ILoggingEvent getLastEvent() {
    return lastEvent;
  }

}
