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
  void setLoggerContext(LoggerContext context);
 
}
