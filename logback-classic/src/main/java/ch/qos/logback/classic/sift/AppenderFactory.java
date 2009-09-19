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
