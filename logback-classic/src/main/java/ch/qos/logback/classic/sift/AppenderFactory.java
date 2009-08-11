/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.sift;

import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.sift.AppenderFactoryBase;
import ch.qos.logback.core.sift.SiftingJoranConfiguratorBase;

public class AppenderFactory extends AppenderFactoryBase<ILoggingEvent>{

  String key;
  
  AppenderFactory(List<SaxEvent> eventList, String key) {
      super(eventList);
      this.key = key;
  }

  public SiftingJoranConfiguratorBase<ILoggingEvent> getSiftingJoranConfigurator(String discriminatingValue) {
    return new SiftingJoranConfigurator(key, discriminatingValue);
  }

}
