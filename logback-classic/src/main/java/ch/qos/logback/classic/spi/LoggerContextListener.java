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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public interface LoggerContextListener {
  

  /**
   * Some listeners should not be removed when the LoggerContext is
   * reset. Such listeners are said to be reset resistant.
   * @return whether this listener is reset resistant or not.
   */
  boolean isResetResistant();
  void onStart(LoggerContext context);
  void onReset(LoggerContext context);
  void onStop(LoggerContext context);
  void onLevelChange(Logger logger, Level level);
}
