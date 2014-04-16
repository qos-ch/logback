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
package ch.qos.logback.core.hook;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.util.Duration;

/**
 * ShutdownHook implementation that stops the Logback context after a specified
 * delay. 
 * 
 * @author Mike Reinhold
 */
public class DelayingShutdownHook extends ShutdownHookBase {
  /**
   * Default immediate shutdown of the context
   */
  public static final Duration DEFAULT_DELAY = Duration.buildByMilliseconds(0);
  
  /**
   * The delay in milliseconds before the ShutdownHook stops the
   * Logback context
   */
  private Duration delay = DEFAULT_DELAY;
    
  public DelayingShutdownHook() {
  }

  public Duration getDelay() {
    return delay;
  }

  public void setDelay(Duration delay) {
    this.delay = delay;
  }

  public void run() {
    try {
      Thread.sleep(delay.getMilliseconds());
    } catch (InterruptedException e) {
      addError("");
    }
    
    super.stop();
  }
}
