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

public interface LoggerContextListener {
  

  /**
   * Some listeners should not be removed when the LoggerContext is
   * reset. Such listeners are said to be reset resistant.
   * @return whether this listener is reset resistant or not.
   */
  public boolean isResetResistant();
  public void onStart(LoggerContext context);
  public void onReset(LoggerContext context);
  public void onStop(LoggerContext context);
}
