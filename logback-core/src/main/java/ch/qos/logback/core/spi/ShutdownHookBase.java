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
package ch.qos.logback.core.spi;

/**
 * Base class for classes implementing a Logback ShutdownHook via extension
 * 
 * @author Mike Reinhold
 */
public abstract class ShutdownHookBase extends ContextAwareBase implements ShutdownHook {
  
  public ShutdownHookBase() {
  }  
  
}
