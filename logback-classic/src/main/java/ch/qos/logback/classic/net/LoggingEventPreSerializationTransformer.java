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
package ch.qos.logback.classic.net;

import java.io.Serializable;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.LoggingEventVO;
import ch.qos.logback.core.spi.PreSerializationTransformer;

public class LoggingEventPreSerializationTransformer implements
    PreSerializationTransformer<ILoggingEvent> {

  public Serializable transform(ILoggingEvent event) {
    if(event == null) {
      return null;
    }
    if (event instanceof LoggingEvent) {
      return LoggingEventVO.build(event);
    } else if (event instanceof LoggingEventVO) {
      return (LoggingEventVO)  event;
    } else {
      throw new IllegalArgumentException("Unsupported type "+event.getClass().getName());
    }
  }

}
