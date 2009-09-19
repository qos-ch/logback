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
package ch.qos.logback.classic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.slf4j.ILoggerFactory;
import org.slf4j.Marker;

import ch.qos.logback.classic.spi.LoggerComparator;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.TurboFilterList;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.WarnStatus;

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

  final Logger root;
  private int size;
  private int noAppenderWarning = 0;
  final private List<LoggerContextListener> loggerContextListenerList = new ArrayList<LoggerContextListener>();

  // We want loggerCache to be synchronized so Hashtable is a good choice. In
  // practice, it performs a little faster than the map returned by
  // Collections.synchronizedMap at the cost of a very slightly higher memory
  // footprint.
  private Hashtable<String, Logger> loggerCache;

  private LoggerContextVO loggerContextRemoteView;
  private final TurboFilterList turboFilterList = new TurboFilterList();
  private boolean packagingDataEnabled = true;

  private int maxCallerDataDepth = ClassicConstants.DEFAULT_MAX_CALLEDER_DATA_DEPTH;

  boolean started = false;

  int resetCount = 0;
  
  public LoggerContext() {
    super();
    this.loggerCache = new Hashtable<String, Logger>();
    this.loggerContextRemoteView = new LoggerContextVO(this);
    this.root = new Logger(Logger.ROOT_LOGGER_NAME, null, this);
    this.root.setLevel(Level.DEBUG);
    loggerCache.put(Logger.ROOT_LOGGER_NAME, root);
    putObject(CoreConstants.EVALUATOR_MAP, new HashMap());
    size = 1;
  }

  /**
   * A new instance of LoggerContextRemoteView needs to be created each time the
   * name or propertyMap (including keys or values) changes.
   */
  private void syncRemoteView() {
    loggerContextRemoteView = new LoggerContextVO(this);
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
    if (Logger.ROOT_LOGGER_NAME.equalsIgnoreCase(name)) {
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
      int h = name.indexOf(ClassicConstants.LOGGER_SEPARATOR, i);
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

  private void incSize() {
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
   *                the name of the logger to search for.
   */
  public Logger exists(String name) {
    return (Logger) loggerCache.get(name);
  }

  final void noAppenderDefinedWarning(final Logger logger) {
    if (noAppenderWarning++ == 0) {
      getStatusManager().add(
          new WarnStatus("No appenders present in context [" + getName()
              + "] for logger [" + logger.getName() + "].", logger));
    }
  }

  public List<Logger> getLoggerList() {
    Collection<Logger> collection = loggerCache.values();
    List<Logger> loggerList = new ArrayList<Logger>(collection);
    Collections.sort(loggerList, new LoggerComparator());
    return loggerList;
  }

  public LoggerContextVO getLoggerContextRemoteView() {
    return loggerContextRemoteView;
  }

  public void setPackagingDataEnabled(boolean packagingDataEnabled) {
    this.packagingDataEnabled = packagingDataEnabled;
  }

  public boolean isPackagingDataEnabled() {
    return packagingDataEnabled;
  }

  /**
   * This method clears all internal properties, closes all appenders, removes
   * any turboFilters, fires an OnReset event, removes all status listeners,
   * removes all context listeners (except those which are reset resistant).
   */
  @Override
  public void reset() {
    resetCount++;
    super.reset();
    root.recursiveReset();
    resetTurboFilterList();
    fireOnReset();
    resetListenersExceptResetResistant();
    resetStatusListeners();
  }

  private void resetStatusListeners() {
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

  /**
   * First stop all registered turbo filters and then clear the registration
   * list.
   */
  public void resetTurboFilterList() {
    for (TurboFilter tf : turboFilterList) {
      tf.stop();
    }
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

  // === start listeners ==============================================
  public void addListener(LoggerContextListener listener) {
    loggerContextListenerList.add(listener);
  }

  public void removeListener(LoggerContextListener listener) {
    loggerContextListenerList.remove(listener);
  }

  private void resetListenersExceptResetResistant() {
    List<LoggerContextListener> toRetain = new ArrayList<LoggerContextListener>();

    for (LoggerContextListener lcl : loggerContextListenerList) {
      if (lcl.isResetResistant()) {
        toRetain.add(lcl);
      }
    }
    loggerContextListenerList.retainAll(toRetain);
  }

  private void resetAllListeners() {
    loggerContextListenerList.clear();
  }

  public List<LoggerContextListener> getCopyOfListenerList() {
    return new ArrayList<LoggerContextListener>(loggerContextListenerList);
  }

  private void fireOnReset() {
    for (LoggerContextListener listener : loggerContextListenerList) {
      listener.onReset(this);
    }
  }

  private void fireOnStart() {
    for (LoggerContextListener listener : loggerContextListenerList) {
      listener.onStart(this);
    }
  }

  private void fireOnStop() {
    for (LoggerContextListener listener : loggerContextListenerList) {
      listener.onStop(this);
    }
  }

  // === end listeners ==============================================

  public boolean isStarted() {
    return started;
  }

  public void start() {
    started = true;
    fireOnStart();
  }

  public void stop() {
    reset();
    fireOnStop();
    resetAllListeners();
    started = false;
  }

  @Override
  public String toString() {
    return this.getClass().getName() + "[" + getName() + "]";
  }

  public int getMaxCallerDataDepth() {
    return maxCallerDataDepth;
  }

  public void setMaxCallerDataDepth(int maxCallerDataDepth) {
    this.maxCallerDataDepth = maxCallerDataDepth;
  }
}
