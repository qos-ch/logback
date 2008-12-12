/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.spi;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;


public class LoggerContextAwareBase extends ContextAwareBase implements LoggerContextAware {
  
  /**
   * Set the owning context. The owning context cannot be set more than
   * once.
   */
  public void setLoggerContext(LoggerContext context) {
    super.setContext(context);
  }

  public void setContext(Context context) {
    // check that the context is of type LoggerContext. Otherwise, throw an exception
    // Context == null is a degenarate case but nonetheless permitted.
    if(context instanceof LoggerContext || context == null) {
      super.setContext(context);
    } else {
      throw new IllegalArgumentException("LoggerContextAwareBase only accepts contexts of type c.l.classic.LoggerContext");
    }
  }

  /**
   * Return the {@link LoggerContext} this component is attached to.
   * 
   * @return The owning LoggerContext
   */
  public LoggerContext getLoggerContext() {
    return (LoggerContext) context;
  }
  
}
