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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.status.Status;

/**
 * A class that provides access to logback components via
 * JMX.
 * 
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * 
 * Contributor:
 *   Sebastian Davids
 *   See http://bugzilla.qos.ch/show_bug.cgi?id=35
 */
public class Configurator extends ContextAwareBase implements
    ConfiguratorMBean {

  private static String EMPTY = "";
  
  public Configurator(LoggerContext loggerContext) {
    this.context = loggerContext;
  }

  public void reload() {
    LoggerContext lc = (LoggerContext) context;
    addInfo("Shutting down context: " + lc.getName());
    lc.shutdownAndReset();
    try {
      new ContextInitializer(lc).autoConfig();
      addInfo("Context: " + lc.getName() + " reloaded.");
    } catch(JoranException je) {
      addError("Reloading of context: " + lc.getName() + " failed.", je);
    }
  }

  public void reload(String fileName) throws JoranException {
    LoggerContext lc = (LoggerContext) context;
    addInfo("Shutting down context: " + lc.getName());
    lc.shutdownAndReset();
    JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(lc);
    configurator.doConfigure(fileName);
    addInfo("Context: " + lc.getName() + " reloaded.");
  }

  public void reload(URL url) throws JoranException {
    LoggerContext lc = (LoggerContext) context;
    addInfo("Shutting down context: " + lc.getName());
    lc.shutdownAndReset();
    new ContextInitializer(lc).configureByResource(url);
    addInfo("Context: " + lc.getName() + " reloaded.");
  }

  public void setLoggerLevel(String loggerName, String levelStr) {
    if (loggerName == null) {
      return;
    }
    if (levelStr == null) {
      return;
    }
    loggerName = loggerName.trim();
    levelStr = levelStr.trim();
    
    addInfo("Trying to set level " + levelStr + " to logger " + loggerName);
    LoggerContext lc = (LoggerContext) context;
    
    Logger logger = lc.getLogger(loggerName);
    if ("null".equalsIgnoreCase(levelStr)) {
      logger.setLevel(null);
    } else {
      Level level = Level.toLevel(levelStr, null);
      if (level != null) {
        logger.setLevel(level);
      }
    }
  }

  public String getLoggerLevel(String loggerName) {
    if (loggerName == null) {
      return EMPTY;
    }
    
    loggerName = loggerName.trim();
    
    LoggerContext lc = (LoggerContext) context;
    Logger logger = lc.exists(loggerName);
    if (logger != null) {
      return logger.getLevel().toString();
    } else {
      return EMPTY;
    }
  }

  public String getLoggerEffectiveLevel(String loggerName) {
    if (loggerName == null) {
      return EMPTY;
    }
    
    loggerName = loggerName.trim();
    
    LoggerContext lc = (LoggerContext) context;
    Logger logger = lc.exists(loggerName);
    if (logger != null) {
      return logger.getEffectiveLevel().toString();
    } else {
      return EMPTY;
    }
  }

  public List<String> getLoggerList() {
    LoggerContext lc = (LoggerContext)context;
    List<String> strList = new ArrayList<String>();
    Iterator<Logger> it = lc.getLoggerList().iterator();
    while(it.hasNext()) {
      Logger log = it.next();
      strList.add(log.getName());
    }
    return strList;
  }
  
  public List<String> getStatuses() {
    List<String> list = new ArrayList<String>();
    Iterator<Status> it = context.getStatusManager().getCopyOfStatusList().iterator();
    while(it.hasNext()) {
      list.add(it.next().toString());
    }
    return list;
  }

}
