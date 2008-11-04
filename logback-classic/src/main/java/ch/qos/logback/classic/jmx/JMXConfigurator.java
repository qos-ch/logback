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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListenerAsList;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * A class that provides access to logback components via JMX.
 * 
 * <p>Since this class implements {@link JMXConfiguratorMBean} it has to be
 * named as Configurator}.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * 
 * Contributor: Sebastian Davids See http://bugzilla.qos.ch/show_bug.cgi?id=35
 */
public class JMXConfigurator extends ContextAwareBase implements
    JMXConfiguratorMBean, LoggerContextListener {

  private static String EMPTY = "";

  final LoggerContext loggerContext;
  final MBeanServer mbs;
  final ObjectName objectName;

  public JMXConfigurator(LoggerContext loggerContext, MBeanServer mbs,
      ObjectName objectName) {
    this.context = loggerContext;
    this.loggerContext = loggerContext;
    this.mbs = mbs;
    this.objectName = objectName;
    removePreviousInstanceAsListener();
    loggerContext.addListener(this);
  }

  private void removePreviousInstanceAsListener() {
    List<LoggerContextListener> lcll = loggerContext.getCopyOfListenerList();

    for (LoggerContextListener lcl : lcll) {
      if (lcl instanceof JMXConfigurator) {
        JMXConfigurator jmxConfigurator = (JMXConfigurator) lcl;
        if (objectName.equals(jmxConfigurator.objectName)) {
          addInfo("Removing previous JMXConfigurator from the logger context listener list");
          loggerContext.removeListener(lcl);
        }
      }
    }
  }

  public void reloadDefaultConfiguration() throws JoranException {
    ContextInitializer ci = new ContextInitializer(loggerContext);
    URL url = ci.findURLOfDefaultConfigurationFile(true);
    reloadByURL(url);
  }

  public void reloadByFileName(String fileName) throws JoranException,
      FileNotFoundException {
    File f = new File(fileName);
    if (f.exists() && f.isFile()) {
      URL url;
      try {
        url = f.toURI().toURL();
        reloadByURL(url);
      } catch (MalformedURLException e) {
        throw new RuntimeException(
            "Unexpected MalformedURLException occured. See nexted cause.", e);
      }

    } else {
      String errMsg = "Could not find [" + fileName + "]";
      addInfo(errMsg);
      throw new FileNotFoundException(errMsg);
    }
  }

  public void reloadByURL(URL url) throws JoranException {
    StatusListenerAsList statusListenerAsList = new StatusListenerAsList();
    StatusManager sm = loggerContext.getStatusManager();
    sm.add(statusListenerAsList);

    addInfo("Resetting context: " + loggerContext.getName());
    loggerContext.reset();

    try {
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(loggerContext);
      configurator.doConfigure(url);
      addInfo("Context: " + loggerContext.getName() + " reloaded.");
    } finally {
      StatusPrinter.print(statusListenerAsList.getStatusList());
    }
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
    LoggerContext lc = (LoggerContext) context;
    List<String> strList = new ArrayList<String>();
    Iterator<Logger> it = lc.getLoggerList().iterator();
    while (it.hasNext()) {
      Logger log = it.next();
      strList.add(log.getName());
    }
    return strList;
  }

  public List<String> getStatuses() {
    List<String> list = new ArrayList<String>();
    Iterator<Status> it = context.getStatusManager().getCopyOfStatusList()
        .iterator();
    while (it.hasNext()) {
      list.add(it.next().toString());
    }
    return list;
  }

  /**
   * When the associated LoggerContext is reset, this configurator must be
   * unregistered
   */
  public void onReset(LoggerContext context) {
    if (mbs.isRegistered(objectName)) {
      try {
        addInfo("Unregistering mbean [" + objectName + "]");
        mbs.unregisterMBean(objectName);
      } catch (InstanceNotFoundException e) {
        // this is theoretically impossible
        addError("Unable to find a verifiably registered mbean [" + objectName
            + "]", e);
      } catch (MBeanRegistrationException e) {
        addError("Failed to unregister [" + objectName + "]", e);
      }
    } else {
      addInfo("mbean [" + objectName
          + "] was not in the mbean registry. This is OK.");
    }

  }

  public void onStart(LoggerContext context) {
  }

  @Override
  public String toString() {
    return this.getClass().getName() + "(" + context.getName() + ")";
  }
}
