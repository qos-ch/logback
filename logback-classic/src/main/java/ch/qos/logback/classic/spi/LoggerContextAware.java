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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.spi.ContextAware;


public interface LoggerContextAware extends ContextAware {


  /** 
   * Set owning logger context for this component. This operation can
   * only be performed once. Once set, the owning context cannot be changed.
   *   
   * @param context The context where this component is attached.
   * @throws IllegalStateException If you try to change the context after it
   * has been set.
   **/
  public void setLoggerContext(LoggerContext context);
 
}
