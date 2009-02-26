/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.net;

import java.io.Serializable;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.LoggingEventSDO;
import ch.qos.logback.core.spi.PreSerializationTransformer;

public class LoggingEventPreSerializationTransformer implements
    PreSerializationTransformer<ILoggingEvent> {

  public Serializable transform(ILoggingEvent event) {
    if(event == null) {
      return null;
    }
    if (event instanceof LoggingEvent) {
      return LoggingEventSDO.build(event);
    } else if (event instanceof LoggingEventSDO) {
      return (LoggingEventSDO)  event;
    } else {
      throw new IllegalArgumentException("Unsupported type "+event.getClass().getName());
    }
  }

}
