/** 
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2005, QOS.ch, LOGBack.com
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.control;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.ClassicGlobal;
import ch.qos.logback.classic.Level;

/**
 * LoggerContext quite optimized for logger retrieval.
 * 
 * It uses a single loggerMap where the key is the logger name and 
 * the value is the logger.
 * 
 * This approach acts a lower limit for what is acheivable for low memory usage 
 * as well as low creation/retreival times. However, this simplicity also results 
 * in slow effective level evaluation, the most frequently exercised part of the API.
 * 
 * 
 * @author ceki
 */
public class ControlLoggerContext {

  private ControlLogger root;
  //
  // Hashtable loggerMap = new Hashtable();
   Map<String, ControlLogger> loggerMap = new HashMap<String, ControlLogger>();
   
  public ControlLoggerContext() {
    this.root = new ControlLogger("root", null);
    this.root.setLevel(Level.DEBUG);
  }

  /**
   * Return this contexts root logger
   *
   * @return
   */
  public ControlLogger getRootLogger() {
    return root;
  }

  public ControlLogger exists(String name) {
    if (name == null) {
      throw new IllegalArgumentException("name parameter cannot be null");
    }

    synchronized (loggerMap) {
      return (ControlLogger) loggerMap.get(name);
    }
  }

  public final ControlLogger getLogger(String name) {
    if (name == null) {
      throw new IllegalArgumentException("name parameter cannot be null");
    }

    synchronized (loggerMap) {
      ControlLogger cl = (ControlLogger) loggerMap.get(name);
      if (cl != null) {
        return cl;
      }
      ControlLogger parent = this.root;

      int i = 0;
      while (true) {
        i = name.indexOf(ClassicGlobal.LOGGER_SEPARATOR, i);
        if (i == -1) {
          //System.out.println("FINAL-Creating logger named [" + name + "] with parent " + parent.getName());
          cl = new ControlLogger(name, parent);
          loggerMap.put(name, cl);
          return cl;
        } else {
          String parentName = name.substring(0, i);
          ControlLogger p = (ControlLogger) loggerMap.get(parentName);
          if (p == null) {
            //System.out.println("INTERMEDIARY-Creating logger [" + parentName + "] with parent " + parent.getName());
            p = new ControlLogger(parentName, parent);
            loggerMap.put(parentName, p);
          }
          parent = p;
        }
        // make i move past the last found dot.
        i++;
      }
    }
  }

  public Map getLoggerMap() {
    return loggerMap;
  }
}
