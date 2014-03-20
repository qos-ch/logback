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

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.spi.ShutdownHookBase;

/**
 * ShutdownHook implementation that stops the Logback context after a specified
 * delay. 
 * 
 * @author Mike Reinhold
 */
public class DelayingCleanupHook extends ShutdownHookBase {
  
  public DelayingCleanupHook() {
  }

  private long delay;

  /**
   * @return the delay
   */
  public long getDelay() {
    return delay;
  }

  /**
   * @param delay the delay to set
   */
  public void setDelay(long delay) {
    this.delay = delay;
  }
  
  public void run() {
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      addError("");
    }
    
    Context hookContext = getContext();
    
    if (hookContext instanceof ContextBase) {
      ContextBase context = (ContextBase) hookContext;
      context.stop();
    }
  }
}
