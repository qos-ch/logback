/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2005, QOS.ch, LOGBack.com
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */

package ch.qos.logback.classic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Marker;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;


public final class Logger implements org.slf4j.Logger, AppenderAttachable {

  /**
   * The fully qualified name of this class. Used in gathering caller
   * information.
   */
  public static final String FQCN = ch.qos.logback.classic.Logger.class.getName();

  static int instanceCount = 0;

  /**
   * The name of this logger
   */
  private String name;

  // The assigned levelInt of this logger. Can be null.
  private Level level;

  // The effective levelInt is the assigned levelInt and if null, a levelInt is
  // inherited form a parent.
  private int effectiveLevelInt;

  /**
   * The parent of this category. All categories have at least one ancestor
   * which is the root category.
   */
  private Logger parent;

  /**
   * The children of this logger. A logger may have zero or more children.
   */
  private List<Logger> childrenList;

  private AppenderAttachableImpl aai;
  /**
   * Additivity is set to true by default, that is children inherit the
   * appenders of their ancestors by default. If this variable is set to
   * <code>false</code> then the appenders located in the ancestors of this
   * logger will not be used. However, the children of this logger will inherit
   * its appenders, unless the children have their additivity flag set to
   * <code>false</code> too. See the user manual for more details.
   */
  private boolean additive = true;

  final LoggerContext loggerContext;

  Logger(String name, Logger parent, LoggerContext loggerContext) {
    this.name = name;
    this.parent = parent;
    this.loggerContext = loggerContext;
    instanceCount++;
  }

  public final Level getEffectiveLevel() {
    return Level.toLevel(effectiveLevelInt);
  }

  int getEffectiveLevelInt() {
    return effectiveLevelInt;
  }

  public Level getLevel() {
    return level;
  }

  public String getName() {
    return name;
  }

  private final boolean isRootLogger() {
    // only the root logger has a null parent
    return parent == null;
  }

  /**
   * Get a child by its suffix.
   * 
   * <p>
   * IMPORTANT: Calls to this method must be within a synchronized block on this
   * logger!
   * 
   * @param suffix
   * @return
   */
  Logger getChildBySuffix(final String suffix) {
    if (childrenList == null) {
      return null;
    } else {
      int len = this.childrenList.size();
      int childNameOffset;
      if (isRootLogger()) {
        childNameOffset = 0;
      } else {
        childNameOffset = this.name.length() + 1;
      }

      for (int i = 0; i < len; i++) {
        final Logger childLogger = (Logger) childrenList.get(i);
        final String childName = childLogger.getName();

        if (suffix.equals(childName.substring(childNameOffset))) {
          return childLogger;
        }
      }
      // no child found
      return null;
    }
  }

  Logger getChildByName(final String childName) {
    if (childrenList == null) {
      return null;
    } else {
      int len = this.childrenList.size();
      for (int i = 0; i < len; i++) {
        final Logger childLogger_i = (Logger) childrenList.get(i);
        final String childName_i = childLogger_i.getName();

        if (childName.equals(childName_i)) {
          return childLogger_i;
        }
      }
      // no child found
      return null;
    }
  }

  public synchronized void setLevel(Level newLevel) {
    if (level == newLevel) {
      // nothing to do;
      return;
    }
    level = newLevel;
    effectiveLevelInt = newLevel.levelInt;
    if (childrenList != null) {
      int len = childrenList.size();
      for (int i = 0; i < len; i++) {
        Logger child = (Logger) childrenList.get(i);
        // tell child to handle parent levelInt change
        child.handleParentLevelChange(newLevel);
      }
    }
  }

  /**
   * This method is invoked by parent logger to let this logger know that the
   * prent's levelInt changed.
   * 
   * @param newParentLevel
   */
  private synchronized void handleParentLevelChange(Level newParentLevel) {
    // changes in the parent levelInt affect children only if their levelInt is
    // null
    if (level == null) {
      effectiveLevelInt = newParentLevel.levelInt;

      // propagate the parent levelInt change to this logger's children
      // propagate the parent levelInt change to this logger's children
      if (childrenList != null) {
        int len = childrenList.size();
        for (int i = 0; i < len; i++) {
          Logger child = (Logger) childrenList.get(i);
          child.handleParentLevelChange(newParentLevel);
        }
      }
    }
  }

  /**
   * Remove all previously added appenders from this logger instance. <p/> This
   * is useful when re-reading configuration information.
   */
  public synchronized void detachAndStopAllAppenders() {
    if (aai != null) {
      aai.detachAndStopAllAppenders();
    }
  }

  public synchronized Appender detachAppender(String name) {
    if (aai == null) {
      return null;
    }
    return aai.detachAppender(name);
  }

  public synchronized void addAppender(Appender newAppender) {
    if (aai == null) {
      aai = new AppenderAttachableImpl();
    }
    aai.addAppender(newAppender);
  }

  public boolean isAttached(Appender appender) {
    if (aai == null) {
      return false;
    }
    return aai.isAttached(appender);
  }

  public synchronized Iterator iteratorForAppenders() {
    if (aai != null) {
      return Collections.EMPTY_LIST.iterator();
    }
    return aai.iteratorForAppenders();
  }

  public Appender getAppender(String name) {
    if (aai == null) {
      aai = new AppenderAttachableImpl();
    }
    return aai.getAppender(name);
  }

  // public boolean isAttached(Appender appender) {
  // // TODO Auto-generated method stub
  // return false;
  // }

  /**
   * Invoke all the appenders of this logger.
   * 
   * @param event
   *          The event to log
   */
  public void callAppenders(LoggingEvent event) {
    int writes = 0;
    // System.out.println("callAppenders");
    for (Logger l = this; l != null; l = l.parent) {
      // System.out.println("l="+l.getName());
      // Protected against simultaneous call to addAppender, removeAppender,...
      synchronized (l) {
        writes += l.appendLoopOnAppenders(event);
        if (!l.additive) {
          break;
        }
      }
    }

    // No appenders in hierarchy
    if (writes == 0) {
    	loggerContext.noAppenderDefinedWarning(this);
    }
  }

  private int appendLoopOnAppenders(LoggingEvent event) {
    int size = 0;
    if (aai != null) {
      size = aai.appendLoopOnAppenders(event);
    }
    return size;
  }

  /**
   * Remove the appender passed as parameter form the list of appenders.
   */
  public synchronized boolean detachAppender(Appender appender) {
    if (aai == null) {
      return false;
    }
    return aai.detachAppender(appender);
  }

  /**
   * Create a child of this logger by suffix, that is, the part of the name
   * extending this logger. For example, if this logger is named "x.y" and the
   * lastPart is "z", then the created child logger will be named "x.y.z".
   * 
   * <p>
   * IMPORTANT: Calls to this method must be within a syncronized block on this
   * logger.
   * 
   * @param lastPart
   *          the suffix (i.e. last part) of the child logger name. This
   *          parameter may not include dots, i.e. the logger separator
   *          character.
   * @return
   */
  Logger createChildByLastNamePart(final String lastPart) {
    int i_index = lastPart.indexOf(ClassicGlobal.LOGGER_SEPARATOR);
    if (i_index != -1) {
      throw new IllegalArgumentException("Child name [" + lastPart
          + " passed as parameter, may not include ["
          + ClassicGlobal.LOGGER_SEPARATOR + "]");
    }

    if (childrenList == null) {
      childrenList = new ArrayList<Logger>();
    }
    Logger childLogger;
    if (this.isRootLogger()) {
      childLogger = new Logger(lastPart, this, this.loggerContext);
    } else {
      childLogger = new Logger(
          name + ClassicGlobal.LOGGER_SEPARATOR + lastPart, this,
          this.loggerContext);
    }
    childrenList.add(childLogger);
    childLogger.effectiveLevelInt = this.effectiveLevelInt;
    return childLogger;
  }

  /**
   * The default size of child list arrays. The JDK 1.5 default is 10. We use a
   * smaller value to save a little space.
   */
  static private final int DEFAULT_CHILD_ARRAY_SIZE = 5;

  Logger createChildByName(final String childName) {
    int i_index = childName.indexOf(ClassicGlobal.LOGGER_SEPARATOR, this.name
        .length() + 1);
    if (i_index != -1) {
      throw new IllegalArgumentException("For logger [" + this.name
          + "] child name [" + childName
          + " passed as parameter, may not include '.' after index"
          + (this.name.length() + 1));
    }

    if (childrenList == null) {
      childrenList = new ArrayList<Logger>(DEFAULT_CHILD_ARRAY_SIZE);
    }
    Logger childLogger;
    childLogger = new Logger(childName, this, this.loggerContext);
    childrenList.add(childLogger);
    childLogger.effectiveLevelInt = this.effectiveLevelInt;
    return childLogger;
  }

  private void filterAndLog(String caller, Level level, String format,
      Throwable t) {
    LoggingEvent le = new LoggingEvent(caller, this, level, format, t);
    loggerContext.getFilterChainDecision(le);
    if (loggerContext.getFilterChainDecision(le) != Filter.DENY) {
      callAppenders(le);
    }
  }

  private void filterAndLog(String caller, Level level, String format,
      Object[] argArray, Throwable t) {
    LoggingEvent le = new LoggingEvent(caller, this, level, format, t);
    le.setArgumentArray(argArray);
    if (loggerContext.getFilterChainDecision(le) != Filter.DENY) {
      callAppenders(le);
    }
  }

  private void filterAndLog(String caller, Level level, Marker marker,
      String format, Object[] argArray, Throwable t) {
    LoggingEvent le = new LoggingEvent(caller, this, level, format, t);
    le.setMarker(marker);
    le.setArgumentArray(argArray);
    if (loggerContext.getFilterChainDecision(le) != Filter.DENY) {
      callAppenders(le);
    }
  }

  public void debug(String msg) {
    if (isDebugEnabled()) {
      filterAndLog(FQCN, Level.DEBUG, msg, null);
    }
  }

  public void debug(String format, Object arg) {
    if (isDebugEnabled()) {
      filterAndLog(FQCN, Level.DEBUG, format, new Object[] {arg}, null);
    }
  }

  public void debug(String format, Object arg1, Object arg2) {
    if (isDebugEnabled()) {
      filterAndLog(FQCN, Level.DEBUG, format, new Object[] {arg1, arg2}, null);
    }
  }

  public void debug(String format, Object[] argArray) {
    if (isDebugEnabled()) {
      filterAndLog(FQCN, Level.DEBUG, format, new Object[] {argArray}, null);
    }
  }

  public void debug(String msg, Throwable t) {
    if (isDebugEnabled()) {
      filterAndLog(FQCN, Level.DEBUG, msg, t);
    }
  }

  public final void debug(Marker marker, String msg) {
    if (isDebugEnabled()) {
      filterAndLog(FQCN, Level.DEBUG, marker, msg, null, null);
    }
  }

  public void debug(Marker marker, String format, Object arg) {
    if (isDebugEnabled()) {
      filterAndLog(FQCN, Level.DEBUG, marker, format, new Object[] {arg}, null);
    }
  }

  public void debug(Marker marker, String format, Object arg1, Object arg2) {
    if (isDebugEnabled()) {
      filterAndLog(FQCN, Level.DEBUG, marker, format,
          new Object[] {arg1, arg2}, null);
    }
  }

  public void debug(Marker marker, String format, Object[] argArray) {
    if (isDebugEnabled()) {
      filterAndLog(FQCN, Level.DEBUG, marker, format, new Object[] {argArray},
          null);
    }
  }

  public void debug(Marker marker, String msg, Throwable t) {
    if (isDebugEnabled()) {
      filterAndLog(FQCN, Level.DEBUG, marker, msg, null, t);
    }
  }

  public void error(String msg) {
    if (isErrorEnabled()) {
      filterAndLog(FQCN, Level.ERROR, msg, null);
    }
  }

  public void error(String format, Object arg) {
    if (isErrorEnabled()) {
      filterAndLog(FQCN, Level.ERROR, format, new Object[] {arg}, null);
    }
  }

  public void error(String format, Object arg1, Object arg2) {
    if (isErrorEnabled()) {
      filterAndLog(FQCN, Level.ERROR, format, new Object[] {arg1, arg2}, null);
    }
  }

  public void error(String format, Object[] argArray) {
    if (isErrorEnabled()) {
      filterAndLog(FQCN, Level.ERROR, format, new Object[] {argArray}, null);
    }
  }

  public void error(String msg, Throwable t) {
    if (isErrorEnabled()) {
      filterAndLog(FQCN, Level.ERROR, msg, t);
    }
  }

  public void error(Marker marker, String msg) {
    if (isErrorEnabled()) {
      filterAndLog(FQCN, Level.ERROR, marker, msg, null, null);
    }
  }

  public void error(Marker marker, String format, Object arg) {
    if (isErrorEnabled()) {
      filterAndLog(FQCN, Level.ERROR, marker, format, new Object[] {arg}, null);
    }
  }

  public void error(Marker marker, String format, Object arg1, Object arg2) {
    if (isErrorEnabled()) {
      filterAndLog(FQCN, Level.ERROR, marker, format,
          new Object[] {arg1, arg2}, null);
    }
  }

  public void error(Marker marker, String format, Object[] argArray) {
    if (isErrorEnabled()) {
      filterAndLog(FQCN, Level.ERROR, marker, format, new Object[] {argArray},
          null);
    }
  }

  public void error(Marker marker, String msg, Throwable t) {
    if (isErrorEnabled()) {
      filterAndLog(FQCN, Level.ERROR, marker, msg, null, t);
    }
  }

  public void info(String msg) {
    if (isInfoEnabled()) {
      filterAndLog(FQCN, Level.INFO, msg, null);
    }
  }

  public void info(String format, Object arg) {
    if (isInfoEnabled()) {
      filterAndLog(FQCN, Level.INFO, format, new Object[] {arg}, null);
    }
  }

  public void info(String format, Object arg1, Object arg2) {
    if (isInfoEnabled()) {
      filterAndLog(FQCN, Level.INFO, format, new Object[] {arg1, arg2}, null);
    }
  }

  public void info(String format, Object[] argArray) {
    if (isInfoEnabled()) {
      filterAndLog(FQCN, Level.INFO, format, new Object[] {argArray}, null);
    }
  }

  public void info(String msg, Throwable t) {
    if (isInfoEnabled()) {
      filterAndLog(FQCN, Level.INFO, msg, t);
    }
  }

  public void info(Marker marker, String msg) {
    if (isInfoEnabled()) {
      filterAndLog(FQCN, Level.INFO, marker, msg, null, null);
    }
  }

  public void info(Marker marker, String format, Object arg) {
    if (isInfoEnabled()) {
      filterAndLog(FQCN, Level.INFO, marker, format, new Object[] {arg}, null);
    }
  }

  public void info(Marker marker, String format, Object arg1, Object arg2) {
    if (isInfoEnabled()) {
      filterAndLog(FQCN, Level.INFO, marker, format, new Object[] {arg1, arg2},
          null);
    }
  }

  public void info(Marker marker, String format, Object[] argArray) {
    if (isInfoEnabled()) {
      filterAndLog(FQCN, Level.INFO, marker, format, new Object[] {argArray},
          null);
    }
  }

  public void info(Marker marker, String msg, Throwable t) {
    if (isInfoEnabled()) {
      filterAndLog(FQCN, Level.INFO, marker, msg, null, t);
    }
  }

  public final boolean isDebugEnabled() {
    return (effectiveLevelInt <= Level.DEBUG_INT);
  }

  public boolean isDebugEnabled(Marker marker) {
    return isDebugEnabled();
  }

  public final boolean isErrorEnabled() {
    return (effectiveLevelInt <= Level.ERROR_INT);
  }

  public boolean isErrorEnabled(Marker marker) {
    return isErrorEnabled();
  }

  public boolean isInfoEnabled() {
    return (effectiveLevelInt <= Level.INFO_INT);
  }

  public boolean isInfoEnabled(Marker marker) {
    return isInfoEnabled();
  }

  public boolean isWarnEnabled() {
    return (effectiveLevelInt <= Level.WARN_INT);
  }

  public boolean isWarnEnabled(Marker marker) {
    return isWarnEnabled();
  }

  public void warn(String msg) {
    if (isWarnEnabled()) {
      filterAndLog(FQCN, Level.WARN, msg, null);
    }
  }

  public void warn(String msg, Throwable t) {
    if (isWarnEnabled()) {
      filterAndLog(FQCN, Level.WARN, msg, t);
    }
  }

  public void warn(String format, Object arg) {
    if (isWarnEnabled()) {
      filterAndLog(FQCN, Level.WARN, format, new Object[] {arg}, null);
    }
  }

  public void warn(String format, Object arg1, Object arg2) {
    if (isWarnEnabled()) {
      filterAndLog(FQCN, Level.WARN, format, new Object[] {arg1, arg2}, null);
    }
  }

  public void warn(String format, Object[] argArray) {
    if (effectiveLevelInt <= Level.WARN_INT) {
      filterAndLog(FQCN, Level.WARN, format, new Object[] {argArray}, null);
    }
  }

  public void warn(Marker marker, String msg) {
    if (isWarnEnabled()) {
      filterAndLog(FQCN, Level.WARN, marker, msg, null, null);
    }
  }

  public void warn(Marker marker, String format, Object arg) {
    if (isWarnEnabled()) {
      filterAndLog(FQCN, Level.WARN, marker, format, new Object[] {arg}, null);
    }
  }

  public void warn(Marker marker, String format, Object[] argArray) {
    if (isWarnEnabled()) {
      filterAndLog(FQCN, Level.WARN, marker, format, new Object[] {argArray},
          null);
    }
  }

  public void warn(Marker marker, String format, Object arg1, Object arg2) {
    if (isWarnEnabled()) {
      filterAndLog(FQCN, Level.WARN, marker, format, new Object[] {arg1, arg2},
          null);
    }
  }

  public void warn(Marker marker, String msg, Throwable t) {
    if (isWarnEnabled()) {
      filterAndLog(FQCN, Level.WARN, marker, msg, null, t);
    }
  }

  public boolean isAdditive() {
    return additive;
  }

  public void setAdditive(boolean additive) {
    this.additive = additive;
  }

  public String toString() {
    return "Logger[" + name + "]";
  }

  /**
   * Return the context for this logger.
   * @return
   */
	public LoggerContext getLoggerContext() {
		return loggerContext;
	}
}
