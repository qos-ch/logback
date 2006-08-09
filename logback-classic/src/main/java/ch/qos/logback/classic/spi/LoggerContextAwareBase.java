/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.spi;

import org.slf4j.Logger;
import org.slf4j.impl.SimpleLogger;
import org.slf4j.impl.SimpleLoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;


public class LoggerContextAwareBase extends ContextAwareBase implements LoggerContextAware {
  
  private Logger logger;

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
  
  /**
   * Return an instance specific logger to be used by the component itself.
   * This logger is not intended to be accessed by the end-user, hence the 
   * protected keyword.
   * 
   * <p>In case the context for this component is not set,
   * this implementations returns a {@link SimpleLogger} instance.
   * 
   * @return A Logger instance.
   */
  protected Logger getLogger() {
    return getLogger(this);
  } 

  /**
   * Return an instance specific logger to be used by the calling component.
   * This logger is not intended to be accessed by the end-user but by LOGBack
   * components.
   *  
   * <p>In case the context for this component is not set,
   * this implementations returns a {@link SimpleLogger} instance.
   * 
   * @return A Logger instance.
   */

  public Logger getLogger(Object component) {
    if(logger == null) {
      if(context != null) {
        logger = ((LoggerContext) context).getLogger(component.getClass().getName());
      } else {
        logger = SimpleLoggerFactory.INSTANCE.getLogger(component.getClass().getName());
      }
    } 
    return logger;
  }


  
}
