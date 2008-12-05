/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.selector;

import java.util.Arrays;
import java.util.List;

import ch.qos.logback.classic.LoggerContext;

public class DefaultContextSelector implements ContextSelector {

  private LoggerContext context;
  
  public DefaultContextSelector(LoggerContext context) {
    this.context = context;
  }
  
  public LoggerContext getLoggerContext() {
    return getDefaultLoggerContext();
  }

  public LoggerContext getDefaultLoggerContext() {
    return context;
  }

  public LoggerContext detachLoggerContext(String loggerContextName) {
    return context;
  }
  
  public List<String> getContextNames() {
    return Arrays.asList(context.getName());
  }
  
  public LoggerContext getLoggerContext(String name) {
    if (context.getName().equals(name)) {
      return context;
    } else {
      return null;
    }
  }
}
