/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.jmx;

import java.net.URL;
import java.util.List;

import ch.qos.logback.core.joran.spi.JoranException;

public interface ConfiguratorMBean {
  
  public void reload();
  
  public void reload(String fileName) throws JoranException;
  
  public void reload(URL url) throws JoranException;
  
  public void setLoggerLevel(String loggerName, String levelStr);
  
  public String getLoggerLevel(String loggerName);
  
  public String getLoggerEffectiveLevel(String loggerName);

  public List<String> getLoggerList();
  
  public List<String> getStatuses();
}
