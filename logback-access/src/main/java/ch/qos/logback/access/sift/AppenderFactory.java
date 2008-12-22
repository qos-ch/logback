/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.access.sift;

import java.util.List;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.sift.AppenderFactoryBase;
import ch.qos.logback.core.sift.SiftingJoranConfiguratorBase;

public class AppenderFactory extends AppenderFactoryBase<AccessEvent> {

  String keyName;

  AppenderFactory(Context context, List<SaxEvent> eventList, String keyName) {
    super(context, eventList);
    this.keyName = keyName;
  }

  public SiftingJoranConfiguratorBase<AccessEvent> getSiftingJoranConfigurator(
      String keyValue) {
    return new SiftingJoranConfigurator(keyName, keyValue);
  }

}
