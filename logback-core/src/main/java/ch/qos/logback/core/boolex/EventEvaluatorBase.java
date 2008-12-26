/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.boolex;

import ch.qos.logback.core.spi.ContextAwareBase;

abstract public class EventEvaluatorBase<E> extends ContextAwareBase implements
    EventEvaluator<E> {

  String name;
  boolean started;

  public String getName() {

    return name;
  }

  public void setName(String name) {
    if (this.name != null) {
      throw new IllegalStateException("name has been already set");
    }
    this.name = name;
  }
  
  public boolean isStarted() {
    return started;
  }

  public void start() {
    started = true;

  }

  public void stop() {
    started = false;
  }

}
