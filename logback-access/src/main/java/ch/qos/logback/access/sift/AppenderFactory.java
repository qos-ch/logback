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
package ch.qos.logback.access.sift;

import java.util.List;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.sift.AppenderFactoryBase;
import ch.qos.logback.core.sift.SiftingJoranConfiguratorBase;

public class AppenderFactory extends AppenderFactoryBase<AccessEvent> {

  String keyName;

  AppenderFactory(List<SaxEvent> eventList, String keyName) {
    super(eventList);
    this.keyName = keyName;
  }

  public SiftingJoranConfiguratorBase<AccessEvent> getSiftingJoranConfigurator(
      String keyValue) {
    return new SiftingJoranConfigurator(keyName, keyValue);
  }

}
