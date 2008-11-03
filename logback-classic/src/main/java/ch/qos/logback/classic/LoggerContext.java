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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.slf4j.ILoggerFactory;
import org.slf4j.Marker;

import ch.qos.logback.classic.spi.ContextListener;
import ch.qos.logback.classic.spi.LoggerComparator;
import ch.qos.logback.classic.spi.LoggerContextRemoteView;
import ch.qos.logback.classic.spi.TurboFilterList;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.status.StatusManager;

/**
 * LoggerContext glues many of the logback-classic components together. In
 * principle, every logback-classic component instance is attached either
 * directly or indirecty to a LoggerContext instance. Just as importantly
 * LoggerContext implements the {@link ILoggerFactory} acting as the
 * manufacturing source of {@link Logger} instances.
 * 
 * @author Ceki Gulcu
 */
public class LoggerContext extends ContextBase implements ILoggerFactory,
    LifeCycle {

  public static final String ROOT_NAME = "root";

  final Logger root;
  private int size;
  private int noAppenderWarning = 0;
  final private List<ContextListener> contextListenerList = new ArrayList<ContextListener>();

  // We want loggerCache to be synchronized so Hashtable is a good choice. In
  // practice, it performs a little faster than the map returned by
  // Collections.synchronizedMap at the
  // cost of a very slightly higher memory footprint.
  private Hashtable<String, Logger> loggerCache;

  private LoggerContextRemoteView loggerContextRemoteView;
  private final TurboFilterList turboFilterList = new TurboFilterList();

  boolean started = false;

  public LoggerContext() {
    super();
    this.loggerCache = new Hashtable<String, Logger>();
    this.loggerContextRemoteView = new LoggerContextRemoteView(this);
    this.root = new Logger(ROOT_NAME, null, this);
    this.root.setLevel(Level.DEBUG);
    loggerCache.put(ROOT_NAME, root);
    putObject(CoreConstants.EVALUATOR_MAP, new HashMap());
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
  public void putProperty(String key, String val) {
    super.putProperty(key, val);
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

    if (name == null) {
      throw new IllegalArgumentException("name argument cannot be null");
    }

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

  synchronized int size() {
    return size;
  }

  /**
   * Check if the named logger exists in the hierarchy. If so return its
   * reference, otherwise returns <code>null</code>.
   * 
   * @param name
   *                the name of the logger to search for.
   */
  public Logger exists(String name) {
    return (Logger) loggerCache.get(name);
  }

  final void noAppenderDefinedWarning(final Logger logger) {
    if (noAppenderWarning++ == 0) {
      getStatusManager().add(
          new ErrorStatus("No appenders present in context [" + getName()
              + "] for logger [" + logger.getName() + "].", logger));
    }
  }

  public List<Logger> getLoggerList() {
    Collection<Logger> collection = loggerCache.values();
    List<Logger> loggerList = new ArrayList<Logger>(collection);
    Collections.sort(loggerList, new LoggerComparator());
    return loggerList;
  }

  public LoggerContextRemoteView getLoggerContextRemoteView() {
    return loggerContextRemoteView;
  }

  public void shutdownAndReset() {
    root.recursiveReset();
    clearAllTurboFilters();
    fireOnReset();
    // TODO is it a good idea to reset the status listeners?
    resetStatusListeners();
  }

  void resetStatusListeners() {
    StatusManager sm = getStatusManager();
    for (StatusListener sl : sm.getCopyOfStatusListenerList()) {
      sm.remove(sl);
    }
  }

  public TurboFilterList getTurboFilterList() {
    return turboFilterList;
  }
  
  public void addTurboFilter(TurboFilter newFilter) {
    turboFilterList.add(newFilter);
  }

  public void clearAllTurboFilters() {
    turboFilterList.clear();
  }

  final FilterReply getTurboFilterChainDecision_0_3OrMore(final Marker marker,
      final Logger logger, final Level level, final String format,
      final Object[] params, final Throwable t) {
    if (turboFilterList.size() == 0) {
      return FilterReply.NEUTRAL;
    }
    return turboFilterList.getTurboFilterChainDecision(marker, logger, level,
        format, params, t);
  }

  final FilterReply getTurboFilterChainDecision_1(final Marker marker,
      final Logger logger, final Level level, final String format,
      final Object param, final Throwable t) {
    if (turboFilterList.size() == 0) {
      return FilterReply.NEUTRAL;
    }
    return turboFilterList.getTurboFilterChainDecision(marker, logger, level,
        format, new Object[] { param }, t);
  }

  final FilterReply getTurboFilterChainDecision_2(final Marker marker,
      final Logger logger, final Level level, final String format,
      final Object param1, final Object param2, final Throwable t) {
    if (turboFilterList.size() == 0) {
      return FilterReply.NEUTRAL;
    }
    return turboFilterList.getTurboFilterChainDecision(marker, logger, level,
        format, new Object[] { param1, param2 }, t);
  }

  public void addListener(ContextListener listener) {
    contextListenerList.add(listener);
  }

  public void removeListener(ContextListener listener) {
    contextListenerList.remove(listener);
  }

  private void fireOnReset() {
    for (ContextListener listener : contextListenerList) {
      listener.onReset(this);
    }
  }

  private void fireOnStart() {
    for (ContextListener listener : contextListenerList) {
      listener.onStart(this);
    }
  }

  public boolean isStarted() {
    return started;
  }

  public void start() {
    started = true;
    fireOnStart();
  }

  public void stop() {
    started = false;
  }

  @Override
  public String toString() {
    return this.getClass().getName() + "[" + getName() + "]";
  }
}
