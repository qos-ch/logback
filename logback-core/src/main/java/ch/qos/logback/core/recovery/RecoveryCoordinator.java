/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.recovery;

public class RecoveryCoordinator {

  public final static long BACKOFF_COEFFICIENT_MIN = 20;
  static long BACKOFF_COEFFICIENT_MAX = 327680;  // BACKOFF_COEFFICIENT_MIN * 4^7
  
  private long backOffCoefficient = BACKOFF_COEFFICIENT_MIN;
  
  private static long UNSET = -1;
  private long currentTime = UNSET;
  long next = System.currentTimeMillis()+getBackoffCoefficient();
  
  public boolean isTooSoon() {
    long now = getCurrentTime();
    if(now > next) {
      next = now + getBackoffCoefficient();
      return false;
    } else {
      return true;
    }
  }
  
  void setCurrentTime(long forcedTime) {
    currentTime = forcedTime;
  }
  
  private long getCurrentTime() {
    if(currentTime != UNSET) {
      return currentTime;
    }
    return System.currentTimeMillis();
  }
  
  private long getBackoffCoefficient() {
    long currentCoeff = backOffCoefficient;
    if(backOffCoefficient < BACKOFF_COEFFICIENT_MAX) {
      backOffCoefficient*=4;
    }
    return currentCoeff;
  }
}
