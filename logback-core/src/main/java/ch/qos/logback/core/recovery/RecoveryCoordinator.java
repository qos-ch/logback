/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2010, QOS.ch. All rights reserved.
 * 
 * This program and the accompanying materials are dual-licensed under either
 * the terms of the Eclipse Public License v1.0 as published by the Eclipse
 * Foundation
 * 
 * or (per the licensee's choosing)
 * 
 * under the terms of the GNU Lesser General Public License version 2.1 as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.recovery;

public class RecoveryCoordinator {

  
  long next = System.currentTimeMillis()+100;
  
  public boolean isTooSoon() {
    long now =System.currentTimeMillis();
    if(now > next) {
      next = now + 100;
      return false;
    } else {
      return true;
    }
  }
}
