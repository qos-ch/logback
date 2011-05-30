/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.sift.tracker;


public class SimulationEvent {

  public enum SimEventType {
    PUT, REMOVE_NOW;
  }
  
  final public String key;
  final public long timestamp;
  final public SimEventType simEventType;
  
  public SimulationEvent(SimEventType simEventType, String key, long timestamp) {
    this.simEventType = simEventType;
    this.key = key;
    this.timestamp = timestamp;
  }

  public String toString() {
      return "Type: "+simEventType+", Event: k=" + key +", timestamp=" + timestamp;
  }
}
