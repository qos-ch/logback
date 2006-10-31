/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic;

import java.util.HashMap;
import java.util.Hashtable;

import org.slf4j.ILoggerFactory;
import org.slf4j.Marker;

import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.classic.spi.TurboFilterAttachable;
import ch.qos.logback.classic.spi.TurboFilterAttachableImpl;
import ch.qos.logback.classic.spi.LoggerContextRemoteView;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.CoreGlobal;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.status.ErrorStatus;

/**
 * @author ceki
 */
public class LoggerContext extends ContextBase implements ILoggerFactory,
    TurboFilterAttachable {

  public static final String ROOT_NAME = "root";

  final Logger root;
  private int size;
  private int noAppenderWarning = 0;

  // We want loggerCache to be synchronized so Hashtable is a good choice. In
  // practice, it
  // performs a little faster than the map returned by
  // Collections.synchronizedMap at the
  // cost of a very slightly higher memory footprint.
  private Hashtable<String, Logger> loggerCache;

  LoggerContextRemoteView loggerContextRemoteView;

  TurboFilterAttachableImpl cfai = null;

  public LoggerContext() {
    super();
    this.loggerCache = new Hashtable<String, Logger>();
    this.loggerContextRemoteView = new LoggerContextRemoteView(this);
    this.root = new Logger(ROOT_NAME, null, this);
    this.root.setLevel(Level.DEBUG);
    loggerCache.put(ROOT_NAME, root);
    putObject(CoreGlobal.EVALUATOR_MAP, new HashMap());
    size = 1;
  }

  /**
   * A new instance of LoggerContextRemoteView needs to be created each time the
   * name or propertyMap (including keys or values) changes.
   */
  private void syncRemoteView() {
    loggerContextRemoteView = new LoggerContextRemoteView(this);
    for (Logger logger : loggerCache.values()) {
      logger.buildRemoteView();
    }
  }

  @Override
  public void setProperty(String key, String val) {
    super.setProperty(key, val);
    syncRemoteView();
  }

  @Override
  public void setName(String name) {
    super.setName(name);
    syncRemoteView();
  }

  public final Logger getLogger(final Class clazz) {
    return getLogger(clazz.getName());
  }

  public final Logger getLogger(final String name) {

    // if we are asking for the root logger, then let us return it without
    // wasting time
    if (ROOT_NAME.equalsIgnoreCase(name)) {
      return root;
    }

    int i = 0;
    Logger logger = root;

    // check if the desired logger exists, if it does, return it
    // without further ado.
    Logger childLogger = (Logger) loggerCache.get(name);
    // if we have the child, then let us return it without wasting time
    if (childLogger != null) {
      return childLogger;
    }

    // if the desired logger does not exist, them create all the loggers
    // in between as well (if they don't already exist)
    String childName;
    while (true) {
      int h = name.indexOf('.', i);
      if (h == -1) {
        childName = name;
      } else {
        childName = name.substring(0, h);
      }
      // move i left of the last point
      i = h + 1;
      synchronized (logger) {
        childLogger = logger.getChildByName(childName);
        if (childLogger == null) {
          childLogger = logger.createChildByName(childName);
          loggerCache.put(childName, childLogger);
          incSize();
        }
      }
      logger = childLogger;
      if (h == -1) {
        return childLogger;
      }
    }
  }

  private synchronized void incSize() {
    size++;
  }

  int size() {
    return size;
  }

  /**
   * Check if the named logger exists in the hierarchy. If so return its
   * reference, otherwise returns <code>null</code>.
   * 
   * @param name
   *          the name of the logger to search for.
   */
  Logger exists(String name) {
    return (Logger) loggerCache.get(name);
  }

  final void noAppenderDefinedWarning(final Logger logger) {
    if (noAppenderWarning++ == 0) {
      getStatusManager().add(
          new ErrorStatus("No appenders present in context [" + getName()
              + "] for logger [" + logger.getName() + "].", logger));
    }
  }

  public LoggerContextRemoteView getLoggerContextRemoteView() {
    return loggerContextRemoteView;
  }

  public void reset() {

    root.recursiveReset();

  }

  public void addTurboFilter(TurboFilter newFilter) {
    if (cfai == null) {
      cfai = new TurboFilterAttachableImpl();
    }
    cfai.addTurboFilter(newFilter);
  }

  public void clearAllTurboFilters() {
    if (cfai == null) {
      return;
    }
    cfai.clearAllTurboFilters();
    cfai = null;
  }

  final public int getTurboFilterChainDecision(final Marker marker,
      final Logger logger, final Level level, final String format, final Object[] params,
      final Throwable t) {
    if (cfai == null) {
      return Filter.NEUTRAL;
    }
    return cfai
        .getTurboFilterChainDecision(marker, logger, level, format, params, t);
  }

  public TurboFilter getFirstTurboFilter() {
    if (cfai == null) {
      return null;
    }
    return cfai.getFirstTurboFilter();
  }
}
