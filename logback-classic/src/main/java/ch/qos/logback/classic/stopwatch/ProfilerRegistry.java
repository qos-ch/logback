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

  private static final InheritableThreadLocal<ProfilerRegistry> inheritableThreadLocal = new InheritableThreadLocal<ProfilerRegistry>();

  
  Map<String, Profiler> profilerMap = new HashMap<String, Profiler>();

  public void put(Profiler profiler) {
    put(profiler.getName(), profiler);
  }
  
  public void put(String name, Profiler profiler) {
    profilerMap.put(name, profiler);
  }
  
  
  public static ProfilerRegistry getThreadContextInstance() {
    ProfilerRegistry pr = inheritableThreadLocal.get();
    if(pr == null) {
      pr = new ProfilerRegistry();
      inheritableThreadLocal.set(pr);
    }
    return pr;
  }
  
  public Profiler get(String name) {
    return profilerMap.get(name);
  }
  
  public void clear() {
    profilerMap.clear();
  }
}
