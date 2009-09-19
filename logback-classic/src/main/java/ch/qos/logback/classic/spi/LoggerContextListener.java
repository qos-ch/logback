/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
