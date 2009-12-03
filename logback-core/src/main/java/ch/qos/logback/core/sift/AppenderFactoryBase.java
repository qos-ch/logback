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
package ch.qos.logback.core.sift;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.JoranException;

public abstract class AppenderFactoryBase<E> {

  final List<SaxEvent> eventList;
  
  protected AppenderFactoryBase(List<SaxEvent> eventList) {
    this.eventList = new ArrayList<SaxEvent>(eventList);
    removeHoardElement();

  }

  void removeHoardElement() {
    eventList.remove(0);
    eventList.remove(eventList.size() - 1);
  }

  public abstract SiftingJoranConfiguratorBase<E> getSiftingJoranConfigurator(String k);
  
  Appender<E> buildAppender(Context context, String discriminatingValue) throws JoranException {
    SiftingJoranConfiguratorBase<E> sjc = getSiftingJoranConfigurator(discriminatingValue);
    sjc.setContext(context);
    sjc.doConfigure(eventList);
    return sjc.getAppender();
  }

  public List<SaxEvent> getEventList() {
    return eventList;
  }

}
