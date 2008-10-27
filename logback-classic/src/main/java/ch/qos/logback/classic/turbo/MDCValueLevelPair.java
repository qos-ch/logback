/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.turbo;

import ch.qos.logback.classic.Level;


/**
 * Bean pairing an MDC value with a log level.
 * 
 * @author Raplh Goers
 * @author Ceki G&uuml;lc&uuml;
 */
public class MDCValueLevelPair {
  private String value;
  private Level level;

  public String getValue() {
    return value;
  }

  public void setValue(String name) {
    this.value = name;
  }

  public Level getLevel() {
    return level;
  }

  public void setLevel(Level level) {
    this.level = level;
  }
}
