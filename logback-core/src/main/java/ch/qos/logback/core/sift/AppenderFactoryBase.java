/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
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
  Context context;
  
  protected AppenderFactoryBase(Context context, List<SaxEvent> eventList) {
    this.context = context;
    this.eventList = new ArrayList<SaxEvent>(eventList);
    removeHoardElement();

  }

  void removeHoardElement() {
    eventList.remove(0);
    eventList.remove(eventList.size() - 1);
    System.out.println(eventList);
  }

  public abstract SiftingJoranConfiguratorBase<E> getSiftingJoranConfigurator(String k);
  
  Appender<E> buildAppender(Context context, String k) throws JoranException {
    SiftingJoranConfiguratorBase<E> sjc = getSiftingJoranConfigurator(k);
    sjc.setContext(context);
    sjc.doConfigure(eventList);
    return sjc.getAppender();
  }

  public List<SaxEvent> getEventList() {
    return eventList;
  }

}
