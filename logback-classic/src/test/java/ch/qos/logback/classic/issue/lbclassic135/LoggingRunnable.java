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
package ch.qos.logback.classic.issue.lbclassic135;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.contention.RunnableWithCounterAndDone;

public class LoggingRunnable extends RunnableWithCounterAndDone {

  Logger logger;

  public LoggingRunnable(Logger logger) {
    this.logger = logger;
  }

  public void run() {
    while (!isDone()) {
      logger.info("hello world ABCDEFGHI");
      counter++;
      // don't hog the CPU forever
      if (counter % 100 == 0) {
        Thread.yield();
      }
    }
  }

}
