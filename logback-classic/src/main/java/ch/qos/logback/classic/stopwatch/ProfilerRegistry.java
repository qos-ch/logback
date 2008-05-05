/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic.stopwatch;

import java.util.HashMap;
import java.util.Map;

/**
 * A miniminalistic registry of profilers.
 * 
 * @author Ceki
 */
public class ProfilerRegistry {

  
  Map<String, Profiler> profilerMap = new HashMap<String, Profiler>();
  
  public void put(String name, Profiler profiler) {
    profilerMap.put(name, profiler);
  }
  
  public Profiler get(String name) {
    return profilerMap.get(name);
  }
  
  public void clear() {
    profilerMap.clear();
  }
}
