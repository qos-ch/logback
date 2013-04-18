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
    // Context == null is a degenerate case but nonetheless permitted.
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
