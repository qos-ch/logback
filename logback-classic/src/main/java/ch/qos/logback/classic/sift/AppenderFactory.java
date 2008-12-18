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

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.sift.AppenderFactoryBase;
import ch.qos.logback.core.sift.SiftingJoranConfiguratorBase;

public class AppenderFactory extends AppenderFactoryBase<LoggingEvent, String>{

  String mdcKey;
  
  AppenderFactory(Context context, List<SaxEvent> eventList, String mdcKey) {
      super(context, eventList);
      this.mdcKey = mdcKey;
  }

  public SiftingJoranConfiguratorBase<LoggingEvent> getSiftingJoranConfigurator(String k) {
    return new HoardingJoranConfigurator(mdcKey, k);
  }

}
