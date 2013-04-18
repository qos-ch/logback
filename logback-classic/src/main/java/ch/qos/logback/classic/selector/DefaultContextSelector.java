/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.selector;

import java.util.Arrays;
import java.util.List;

import ch.qos.logback.classic.LoggerContext;

public class DefaultContextSelector implements ContextSelector {

  private LoggerContext defaultLoggerContext;
  
  public DefaultContextSelector(LoggerContext context) {
    this.defaultLoggerContext = context;
  }
  
  public LoggerContext getLoggerContext() {
    return getDefaultLoggerContext();
  }

  public LoggerContext getDefaultLoggerContext() {
    return defaultLoggerContext;
  }

  public LoggerContext detachLoggerContext(String loggerContextName) {
    return defaultLoggerContext;
  }
  
  public List<String> getContextNames() {
    return Arrays.asList(defaultLoggerContext.getName());
  }
  
  public LoggerContext getLoggerContext(String name) {
    if (defaultLoggerContext.getName().equals(name)) {
      return defaultLoggerContext;
    } else {
      return null;
    }
  }
}
