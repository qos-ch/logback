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
package ch.qos.logback.classic.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.util.OptionHelper;

public class StatusListenerConfigHelper {

  static void installIfAsked(LoggerContext loggerContext) {
    String slClass = OptionHelper.getSystemProperty(
        ContextInitializer.STATUS_LISTENER_CLASS);
    if (!OptionHelper.isEmpty(slClass)) {
      addStatusListener(loggerContext, slClass);
    }
  }

  static void addStatusListener(LoggerContext loggerContext,
      String listenerClass) {
    StatusListener listener = null;
    if (ContextInitializer.SYSOUT.equalsIgnoreCase(listenerClass)) {
      listener = new OnConsoleStatusListener();
    } else {
      try {
        listener = (StatusListener) OptionHelper.instantiateByClassName(
            listenerClass, StatusListener.class, loggerContext);
      } catch (Exception e) {
        // printing on the console is the best we can do
        e.printStackTrace();
      }
    }
    if (listener != null) {
      loggerContext.getStatusManager().add(listener);
    }
  }
}
