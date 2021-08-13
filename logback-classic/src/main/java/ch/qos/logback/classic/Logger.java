/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.spi.LocationAwareLogger;
import org.slf4j.spi.LoggingEventBuilder;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LogbackLoggingEventBuilder;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.LoggerNameUtil;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import ch.qos.logback.core.spi.FilterReply;

public final class Logger implements org.slf4j.Logger, LocationAwareLogger, AppenderAttachable<ILoggingEvent>, Serializable {

    private static final long serialVersionUID = 5454405123156820674L; // 8745934908040027998L;

    /**
     * The fully qualified name of this class. Used in gathering caller
     * information.
     */
    public static final String FQCN = ch.qos.logback.classic.Logger.class.getName();

    /**
     * The name of this logger
     */
    private String name;

    // The assigned levelInt of this logger. Can be null.
    transient private Level level;

    // The effective levelInt is the assigned levelInt and if null, a levelInt is
    // inherited form a parent.
    transient private int effectiveLevelInt;

    /**
     * The parent of this category. All categories have at least one ancestor
     * which is the root category.
     */
    transient private Logger parent;

    /**
     * The children of this logger. A logger may have zero or more children.
     */
    transient private List<Logger> childrenList;

    /**
     * It is assumed that once the 'aai' variable is set to a non-null value, it
     * will never be reset to null. it is further assumed that only place where
     * the 'aai'ariable is set is within the addAppender method. This method is
     * synchronized on 'this' (Logger) protecting against simultaneous
     * re-configuration of this logger (a very unlikely scenario).
     * 
     * <p>
     * It is further assumed that the AppenderAttachableImpl is responsible for
     * its internal synchronization and thread safety. Thus, we can get away with
     * *not* synchronizing on the 'aai' (check null/ read) because
     * <p>
     * 1) the 'aai' variable is immutable once set to non-null
     * <p>
     * 2) 'aai' is getAndSet only within addAppender which is synchronized
     * <p>
     * 3) all the other methods check whether 'aai' is null
     * <p>
     * 4) AppenderAttachableImpl is thread safe
     */
    transient private AppenderAttachableImpl<ILoggingEvent> aai;
    /**
     * Additivity is set to true by default, that is children inherit the
     * appenders of their ancestors by default. If this variable is set to
     * <code>false</code> then the appenders located in the ancestors of this
     * logger will not be used. However, the children of this logger will inherit
     * its appenders, unless the children have their additivity flag set to
     * <code>false</code> too. See the user manual for more details.
     */
    transient private boolean additive = true;

    final transient LoggerContext loggerContext;

    Logger(String name, Logger parent, LoggerContext loggerContext) {
        this.name = name;
        this.parent = parent;
        this.loggerContext = loggerContext;
    }

    public Level getEffectiveLevel() {
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

    private boolean isRootLogger() {
        // only the root logger has a null parent
        return parent == null;
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
        if (newLevel == null && isRootLogger()) {
            throw new IllegalArgumentException("The level of the root logger cannot be set to null");
        }

        level = newLevel;
        if (newLevel == null) {
            effectiveLevelInt = parent.effectiveLevelInt;
            newLevel = parent.getEffectiveLevel();
        } else {
            effectiveLevelInt = newLevel.levelInt;
        }

        if (childrenList != null) {
            int len = childrenList.size();
            for (int i = 0; i < len; i++) {
                Logger child = (Logger) childrenList.get(i);
                // tell child to handle parent levelInt change
                child.handleParentLevelChange(effectiveLevelInt);
            }
        }
        // inform listeners
        loggerContext.fireOnLevelChange(this, newLevel);
    }

    /**
     * This method is invoked by parent logger to let this logger know that the
     * prent's levelInt changed.
     * 
     * @param newParentLevelInt
     */
    private synchronized void handleParentLevelChange(int newParentLevelInt) {
        // changes in the parent levelInt affect children only if their levelInt is
        // null
        if (level == null) {
            effectiveLevelInt = newParentLevelInt;

            // propagate the parent levelInt change to this logger's children
            if (childrenList != null) {
                int len = childrenList.size();
                for (int i = 0; i < len; i++) {
                    Logger child = (Logger) childrenList.get(i);
                    child.handleParentLevelChange(newParentLevelInt);
                }
            }
        }
    }

    /**
     * Remove all previously added appenders from this logger instance.
     * <p/>
     * This is useful when re-reading configuration information.
     */
    public void detachAndStopAllAppenders() {
        if (aai != null) {
            aai.detachAndStopAllAppenders();
        }
    }

    public boolean detachAppender(String name) {
        if (aai == null) {
            return false;
        }
        return aai.detachAppender(name);
    }

    // this method MUST be synchronized. See comments on 'aai' field for further
    // details.
    public synchronized void addAppender(Appender<ILoggingEvent> newAppender) {
        if (aai == null) {
            aai = new AppenderAttachableImpl<ILoggingEvent>();
        }
        aai.addAppender(newAppender);
    }

    public boolean isAttached(Appender<ILoggingEvent> appender) {
        if (aai == null) {
            return false;
        }
        return aai.isAttached(appender);
    }

    @SuppressWarnings("unchecked")
    public Iterator<Appender<ILoggingEvent>> iteratorForAppenders() {
        if (aai == null) {
            return Collections.EMPTY_LIST.iterator();
        }
        return aai.iteratorForAppenders();
    }

    public Appender<ILoggingEvent> getAppender(String name) {
        if (aai == null) {
            return null;
        }
        return aai.getAppender(name);
    }

    /**
     * Invoke all the appenders of this logger.
     * 
     * @param event
     *          The event to log
     */
    public void callAppenders(ILoggingEvent event) {
        int writes = 0;
        for (Logger l = this; l != null; l = l.parent) {
            writes += l.appendLoopOnAppenders(event);
            if (!l.additive) {
                break;
            }
        }
        // No appenders in hierarchy
        if (writes == 0) {
            loggerContext.noAppenderDefinedWarning(this);
        }
    }

    private int appendLoopOnAppenders(ILoggingEvent event) {
        if (aai != null) {
            return aai.appendLoopOnAppenders(event);
        } else {
            return 0;
        }
    }

    /**
     * Remove the appender passed as parameter form the list of appenders.
     */
    public boolean detachAppender(Appender<ILoggingEvent> appender) {
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
     * IMPORTANT: Calls to this method must be within a synchronized block on this
     * logger.
     * 
     * @param lastPart
     *          the suffix (i.e. last part) of the child logger name. This
     *          parameter may not include dots, i.e. the logger separator
     *          character.
     * @return
     */
    Logger createChildByLastNamePart(final String lastPart) {
        int i_index = LoggerNameUtil.getFirstSeparatorIndexOf(lastPart);
        if (i_index != -1) {
            throw new IllegalArgumentException("Child name [" + lastPart + " passed as parameter, may not include [" + CoreConstants.DOT + "]");
        }

        if (childrenList == null) {
            childrenList = new CopyOnWriteArrayList<Logger>();
        }
        Logger childLogger;
        if (this.isRootLogger()) {
            childLogger = new Logger(lastPart, this, this.loggerContext);
        } else {
            childLogger = new Logger(name + CoreConstants.DOT + lastPart, this, this.loggerContext);
        }
        childrenList.add(childLogger);
        childLogger.effectiveLevelInt = this.effectiveLevelInt;
        return childLogger;
    }

    private void localLevelReset() {
        effectiveLevelInt = Level.DEBUG_INT;
        if (isRootLogger()) {
            level = Level.DEBUG;
        } else {
            level = null;
        }
    }

    void recursiveReset() {
        detachAndStopAllAppenders();
        localLevelReset();
        additive = true;
        if (childrenList == null) {
            return;
        }
        for (Logger childLogger : childrenList) {
            childLogger.recursiveReset();
        }
    }

    /**
     * The default size of child list arrays. The JDK 1.5 default is 10. We use a
     * smaller value to save a little space.
     */

    Logger createChildByName(final String childName) {
        int i_index = LoggerNameUtil.getSeparatorIndexOf(childName, this.name.length() + 1);
        if (i_index != -1) {
            throw new IllegalArgumentException("For logger [" + this.name + "] child name [" + childName
                            + " passed as parameter, may not include '.' after index" + (this.name.length() + 1));
        }

        if (childrenList == null) {
            childrenList = new CopyOnWriteArrayList<Logger>();
        }
        Logger childLogger;
        childLogger = new Logger(childName, this, this.loggerContext);
        childrenList.add(childLogger);
        childLogger.effectiveLevelInt = this.effectiveLevelInt;
        return childLogger;
    }

    /**
     * The next methods are not merged into one because of the time we gain by not
     * creating a new Object[] with the params. This reduces the cost of not
     * logging by about 20 nanoseconds.
     */

    private void filterAndLog_0_Or3Plus(final String localFQCN, final Marker marker, final Level level, final String msg, final Object[] params,
                    final Throwable t) {

        final FilterReply decision = loggerContext.getTurboFilterChainDecision_0_3OrMore(marker, this, level, msg, params, t);

        if (decision == FilterReply.NEUTRAL) {
            if (effectiveLevelInt > level.levelInt) {
                return;
            }
        } else if (decision == FilterReply.DENY) {
            return;
        }

        buildLoggingEventAndAppend(localFQCN, marker, level, msg, params, t);
    }

    private void filterAndLog_1(final String localFQCN, final Marker marker, final Level level, final String msg, final Object param, final Throwable t) {

        final FilterReply decision = loggerContext.getTurboFilterChainDecision_1(marker, this, level, msg, param, t);

        if (decision == FilterReply.NEUTRAL) {
            if (effectiveLevelInt > level.levelInt) {
                return;
            }
        } else if (decision == FilterReply.DENY) {
            return;
        }

        buildLoggingEventAndAppend(localFQCN, marker, level, msg, new Object[] { param }, t);
    }

    private void filterAndLog_2(final String localFQCN, final Marker marker, final Level level, final String msg, final Object param1, final Object param2,
                    final Throwable t) {

        final FilterReply decision = loggerContext.getTurboFilterChainDecision_2(marker, this, level, msg, param1, param2, t);

        if (decision == FilterReply.NEUTRAL) {
            if (effectiveLevelInt > level.levelInt) {
                return;
            }
        } else if (decision == FilterReply.DENY) {
            return;
        }

        buildLoggingEventAndAppend(localFQCN, marker, level, msg, new Object[] { param1, param2 }, t);
    }

    private void buildLoggingEventAndAppend(final String localFQCN, final Marker marker, final Level level, final String msg, final Object[] params,
                    final Throwable t) {
        LoggingEvent le = new LoggingEvent(localFQCN, this, level, msg, t, params);
        le.addMarker(marker);
        callAppenders(le);
    }

    public void trace(String msg) {
        filterAndLog_0_Or3Plus(FQCN, null, Level.TRACE, msg, null, null);
    }

    public void trace(String format, Object arg) {
        filterAndLog_1(FQCN, null, Level.TRACE, format, arg, null);
    }

    public void trace(String format, Object arg1, Object arg2) {
        filterAndLog_2(FQCN, null, Level.TRACE, format, arg1, arg2, null);
    }

    public void trace(String format, Object... argArray) {
        filterAndLog_0_Or3Plus(FQCN, null, Level.TRACE, format, argArray, null);
    }

    public void trace(String msg, Throwable t) {
        filterAndLog_0_Or3Plus(FQCN, null, Level.TRACE, msg, null, t);
    }

    public void trace(Marker marker, String msg) {
        filterAndLog_0_Or3Plus(FQCN, marker, Level.TRACE, msg, null, null);
    }

    public void trace(Marker marker, String format, Object arg) {
        filterAndLog_1(FQCN, marker, Level.TRACE, format, arg, null);
    }

    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        filterAndLog_2(FQCN, marker, Level.TRACE, format, arg1, arg2, null);
    }

    public void trace(Marker marker, String format, Object... argArray) {
        filterAndLog_0_Or3Plus(FQCN, marker, Level.TRACE, format, argArray, null);
    }

    public void trace(Marker marker, String msg, Throwable t) {
        filterAndLog_0_Or3Plus(FQCN, marker, Level.TRACE, msg, null, t);
    }

    public boolean isDebugEnabled() {
        return isDebugEnabled(null);
    }

    public boolean isDebugEnabled(Marker marker) {
        final FilterReply decision = callTurboFilters(marker, Level.DEBUG);
        if (decision == FilterReply.NEUTRAL) {
            return effectiveLevelInt <= Level.DEBUG_INT;
        } else if (decision == FilterReply.DENY) {
            return false;
        } else if (decision == FilterReply.ACCEPT) {
            return true;
        } else {
            throw new IllegalStateException("Unknown FilterReply value: " + decision);
        }
    }

    public void debug(String msg) {
        filterAndLog_0_Or3Plus(FQCN, null, Level.DEBUG, msg, null, null);
    }

    public void debug(String format, Object arg) {
        filterAndLog_1(FQCN, null, Level.DEBUG, format, arg, null);
    }

    public void debug(String format, Object arg1, Object arg2) {
        filterAndLog_2(FQCN, null, Level.DEBUG, format, arg1, arg2, null);
    }

    public void debug(String format, Object... argArray) {
        filterAndLog_0_Or3Plus(FQCN, null, Level.DEBUG, format, argArray, null);
    }

    public void debug(String msg, Throwable t) {
        filterAndLog_0_Or3Plus(FQCN, null, Level.DEBUG, msg, null, t);
    }

    public void debug(Marker marker, String msg) {
        filterAndLog_0_Or3Plus(FQCN, marker, Level.DEBUG, msg, null, null);
    }

    public void debug(Marker marker, String format, Object arg) {
        filterAndLog_1(FQCN, marker, Level.DEBUG, format, arg, null);
    }

    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        filterAndLog_2(FQCN, marker, Level.DEBUG, format, arg1, arg2, null);
    }

    public void debug(Marker marker, String format, Object... argArray) {
        filterAndLog_0_Or3Plus(FQCN, marker, Level.DEBUG, format, argArray, null);
    }

    public void debug(Marker marker, String msg, Throwable t) {
        filterAndLog_0_Or3Plus(FQCN, marker, Level.DEBUG, msg, null, t);
    }

    public void error(String msg) {
        filterAndLog_0_Or3Plus(FQCN, null, Level.ERROR, msg, null, null);
    }

    public void error(String format, Object arg) {
        filterAndLog_1(FQCN, null, Level.ERROR, format, arg, null);
    }

    public void error(String format, Object arg1, Object arg2) {
        filterAndLog_2(FQCN, null, Level.ERROR, format, arg1, arg2, null);
    }

    public void error(String format, Object... argArray) {
        filterAndLog_0_Or3Plus(FQCN, null, Level.ERROR, format, argArray, null);
    }

    public void error(String msg, Throwable t) {
        filterAndLog_0_Or3Plus(FQCN, null, Level.ERROR, msg, null, t);
    }

    public void error(Marker marker, String msg) {
        filterAndLog_0_Or3Plus(FQCN, marker, Level.ERROR, msg, null, null);
    }

    public void error(Marker marker, String format, Object arg) {
        filterAndLog_1(FQCN, marker, Level.ERROR, format, arg, null);
    }

    public void error(Marker marker, String format, Object arg1, Object arg2) {
        filterAndLog_2(FQCN, marker, Level.ERROR, format, arg1, arg2, null);
    }

    public void error(Marker marker, String format, Object... argArray) {
        filterAndLog_0_Or3Plus(FQCN, marker, Level.ERROR, format, argArray, null);
    }

    public void error(Marker marker, String msg, Throwable t) {
        filterAndLog_0_Or3Plus(FQCN, marker, Level.ERROR, msg, null, t);
    }

    public boolean isInfoEnabled() {
        return isInfoEnabled(null);
    }

    public boolean isInfoEnabled(Marker marker) {
        FilterReply decision = callTurboFilters(marker, Level.INFO);
        if (decision == FilterReply.NEUTRAL) {
            return effectiveLevelInt <= Level.INFO_INT;
        } else if (decision == FilterReply.DENY) {
            return false;
        } else if (decision == FilterReply.ACCEPT) {
            return true;
        } else {
            throw new IllegalStateException("Unknown FilterReply value: " + decision);
        }
    }

    public void info(String msg) {
        filterAndLog_0_Or3Plus(FQCN, null, Level.INFO, msg, null, null);
    }

    public void info(String format, Object arg) {
        filterAndLog_1(FQCN, null, Level.INFO, format, arg, null);
    }

    public void info(String format, Object arg1, Object arg2) {
        filterAndLog_2(FQCN, null, Level.INFO, format, arg1, arg2, null);
    }

    public void info(String format, Object... argArray) {
        filterAndLog_0_Or3Plus(FQCN, null, Level.INFO, format, argArray, null);
    }

    public void info(String msg, Throwable t) {
        filterAndLog_0_Or3Plus(FQCN, null, Level.INFO, msg, null, t);
    }

    public void info(Marker marker, String msg) {
        filterAndLog_0_Or3Plus(FQCN, marker, Level.INFO, msg, null, null);
    }

    public void info(Marker marker, String format, Object arg) {
        filterAndLog_1(FQCN, marker, Level.INFO, format, arg, null);
    }

    public void info(Marker marker, String format, Object arg1, Object arg2) {
        filterAndLog_2(FQCN, marker, Level.INFO, format, arg1, arg2, null);
    }

    public void info(Marker marker, String format, Object... argArray) {
        filterAndLog_0_Or3Plus(FQCN, marker, Level.INFO, format, argArray, null);
    }

    public void info(Marker marker, String msg, Throwable t) {
        filterAndLog_0_Or3Plus(FQCN, marker, Level.INFO, msg, null, t);
    }

    public boolean isTraceEnabled() {
        return isTraceEnabled(null);
    }

    public boolean isTraceEnabled(Marker marker) {
        final FilterReply decision = callTurboFilters(marker, Level.TRACE);
        if (decision == FilterReply.NEUTRAL) {
            return effectiveLevelInt <= Level.TRACE_INT;
        } else if (decision == FilterReply.DENY) {
            return false;
        } else if (decision == FilterReply.ACCEPT) {
            return true;
        } else {
            throw new IllegalStateException("Unknown FilterReply value: " + decision);
        }
    }

    public boolean isErrorEnabled() {
        return isErrorEnabled(null);
    }

    public boolean isErrorEnabled(Marker marker) {
        FilterReply decision = callTurboFilters(marker, Level.ERROR);
        if (decision == FilterReply.NEUTRAL) {
            return effectiveLevelInt <= Level.ERROR_INT;
        } else if (decision == FilterReply.DENY) {
            return false;
        } else if (decision == FilterReply.ACCEPT) {
            return true;
        } else {
            throw new IllegalStateException("Unknown FilterReply value: " + decision);
        }
    }

    public boolean isWarnEnabled() {
        return isWarnEnabled(null);
    }

    public boolean isWarnEnabled(Marker marker) {
        FilterReply decision = callTurboFilters(marker, Level.WARN);
        if (decision == FilterReply.NEUTRAL) {
            return effectiveLevelInt <= Level.WARN_INT;
        } else if (decision == FilterReply.DENY) {
            return false;
        } else if (decision == FilterReply.ACCEPT) {
            return true;
        } else {
            throw new IllegalStateException("Unknown FilterReply value: " + decision);
        }

    }

    public boolean isEnabledFor(Marker marker, Level level) {
        FilterReply decision = callTurboFilters(marker, level);
        if (decision == FilterReply.NEUTRAL) {
            return effectiveLevelInt <= level.levelInt;
        } else if (decision == FilterReply.DENY) {
            return false;
        } else if (decision == FilterReply.ACCEPT) {
            return true;
        } else {
            throw new IllegalStateException("Unknown FilterReply value: " + decision);
        }
    }

    public boolean isEnabledFor(Level level) {
        return isEnabledFor(null, level);
    }

    public void warn(String msg) {
        filterAndLog_0_Or3Plus(FQCN, null, Level.WARN, msg, null, null);
    }

    public void warn(String msg, Throwable t) {
        filterAndLog_0_Or3Plus(FQCN, null, Level.WARN, msg, null, t);
    }

    public void warn(String format, Object arg) {
        filterAndLog_1(FQCN, null, Level.WARN, format, arg, null);
    }

    public void warn(String format, Object arg1, Object arg2) {
        filterAndLog_2(FQCN, null, Level.WARN, format, arg1, arg2, null);
    }

    public void warn(String format, Object... argArray) {
        filterAndLog_0_Or3Plus(FQCN, null, Level.WARN, format, argArray, null);
    }

    public void warn(Marker marker, String msg) {
        filterAndLog_0_Or3Plus(FQCN, marker, Level.WARN, msg, null, null);
    }

    public void warn(Marker marker, String format, Object arg) {
        filterAndLog_1(FQCN, marker, Level.WARN, format, arg, null);
    }

    public void warn(Marker marker, String format, Object... argArray) {
        filterAndLog_0_Or3Plus(FQCN, marker, Level.WARN, format, argArray, null);
    }

    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        filterAndLog_2(FQCN, marker, Level.WARN, format, arg1, arg2, null);
    }

    public void warn(Marker marker, String msg, Throwable t) {
        filterAndLog_0_Or3Plus(FQCN, marker, Level.WARN, msg, null, t);
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
     * Method that calls the attached TurboFilter objects based on the logger and
     * the level.
     * 
     * It is used by isYYYEnabled() methods.
     * 
     * It returns the typical FilterReply values: ACCEPT, NEUTRAL or DENY.
     * 
     * @param level
     * @return the reply given by the TurboFilters
     */
    private FilterReply callTurboFilters(Marker marker, Level level) {
        return loggerContext.getTurboFilterChainDecision_0_3OrMore(marker, this, level, null, null, null);
    }

    /**
     * Return the context for this logger.
     * 
     * @return the context
     */
    public LoggerContext getLoggerContext() {
        return loggerContext;
    }

    /**
     * Creates a {@link LoggingEventBuilder} of type {@link LogbackLoggingEventBuilder}.
     * 
     * @since 1.3
     */
    @Override
    public LoggingEventBuilder makeLoggingEventBuilder(org.slf4j.event.Level level) {
    	return new LogbackLoggingEventBuilder(this, level);
    }

    public void log(Marker marker, String fqcn, int levelInt, String message, Object[] argArray, Throwable t) {
        Level level = Level.fromLocationAwareLoggerInteger(levelInt);
        filterAndLog_0_Or3Plus(fqcn, marker, level, message, argArray, t);
    }

    /**
     * Support SLF4J interception during initialization as introduced in SLF4J version 1.7.15
     * @since 1.1.4 
     * @param slf4jEvent
     */
    public void log(org.slf4j.event.LoggingEvent slf4jEvent) {
    	org.slf4j.event.Level slf4jLevel = slf4jEvent.getLevel();
		Level logbackLevel = Level.convertAnSLF4JLevel(slf4jLevel);
		
		// By default, assume this class was the caller. This happens during initialization.
		// However, it is possible that the caller is some other library, e.g. 
		// slf4j-jdk-platform-logging
        
		String callerBoundary = slf4jEvent.getCallerBoundary();
        if(callerBoundary==null) {
        	callerBoundary = FQCN;
        }
        
		LoggingEvent lle = new LoggingEvent(callerBoundary, this, logbackLevel,  slf4jEvent.getMessage(), slf4jEvent.getThrowable(), 
				slf4jEvent.getArgumentArray());
		List<Marker> markers = slf4jEvent.getMarkers();
		
		if(markers != null) {
			markers.forEach(m -> lle.addMarker(m));
		}
		
		lle.setKeyValuePairs(slf4jEvent.getKeyValuePairs());
		

		// Note that at this point, any calls made with a logger disabled 
		// for a given level, will be already filtered out/in. TurboFilters cannot 
		// act at this point in the process.
		this.callAppenders(lle);
    }

    /**
     * After serialization, the logger instance does not know its LoggerContext.
     * The best we can do here, is to return a logger with the same name
     * returned by org.slf4j.LoggerFactory.
     * 
     * @return Logger instance with the same name
     * @throws ObjectStreamException
     */
    protected Object readResolve() throws ObjectStreamException {
        return LoggerFactory.getLogger(getName());
    }
}
