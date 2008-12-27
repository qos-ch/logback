/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic.net;

import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * A simple EventEvaluator implementation that triggers email transmission after
 * a given number of events occur, regardless of event level.
 * 
 * <p>By default, the limit is 1024.
 */
public class CounterBasedEvaluator extends ContextAwareBase implements EventEvaluator {

  static int DEFAULT_LIMIT = 1024;
  int limit = DEFAULT_LIMIT;
  int counter = 0;
  String name;
  boolean started;

  public boolean evaluate(Object event) throws NullPointerException,
      EvaluationException {
    counter++;

    if (counter == limit) {
      counter = 0;
      return true;
    } else {
      return false;
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
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

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }
  
  
}
