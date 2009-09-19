/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.jmx;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.List;

import ch.qos.logback.core.joran.spi.JoranException;

public interface JMXConfiguratorMBean {
  
  public void reloadDefaultConfiguration() throws JoranException;
  
  public void reloadByFileName(String fileName) throws JoranException, FileNotFoundException;
  
  public void reloadByURL(URL url) throws JoranException;
  
  public void setLoggerLevel(String loggerName, String levelStr);
  
  public String getLoggerLevel(String loggerName);
  
  public String getLoggerEffectiveLevel(String loggerName);

  public List<String> getLoggerList();
  
  public List<String> getStatuses();
}
