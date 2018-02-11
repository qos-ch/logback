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

import static ch.qos.logback.core.CoreConstants.EVALUATOR_MAP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.ILoggerFactory;
import org.slf4j.Marker;

import ch.qos.logback.classic.spi.LoggerComparator;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.TurboFilterList;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.classic.util.LoggerNameUtil;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.spi.SequenceNumberGenerator;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.WarnStatus;

/**
 * LoggerContext glues many of the logback-classic components together. In
 * principle, every logback-classic component instance is attached either
 * directly or indirectly to a LoggerContext instance. Just as importantly
 * LoggerContext implements the {@link ILoggerFactory} acting as the
 * manufacturing source of {@link Logger} instances.
 *
 * @author Ceki Gulcu
 */
public class LoggerContext extends ContextBase implements ILoggerFactory, LifeCycle {

    /** Default setting of packaging data in stack traces */
    public static final boolean DEFAULT_PACKAGING_DATA = false;

    final Logger root;
    private int size;
    private int noAppenderWarning = 0;
    final private List<LoggerContextListener> loggerContextListenerList = new ArrayList<LoggerContextListener>();

    private Map<String, Logger> loggerCache;

    private LoggerContextVO loggerContextRemoteView;
    private final TurboFilterList turboFilterList = new TurboFilterList();
    private boolean packagingDataEnabled = DEFAULT_PACKAGING_DATA;
    SequenceNumberGenerator sequenceNumberGenerator = null; // by default there is no SequenceNumberGenerator
    
    private int maxCallerDataDepth = ClassicConstants.DEFAULT_MAX_CALLEDER_DATA_DEPTH;

    int resetCount = 0;
    private List<String> frameworkPackages;

    public LoggerContext() {
        super();
        this.loggerCache = new ConcurrentHashMap<String, Logger>();

        this.loggerContextRemoteView = new LoggerContextVO(this);
        this.root = new Logger(Logger.ROOT_LOGGER_NAME, null, this);
        this.root.setLevel(Level.DEBUG);
        loggerCache.put(Logger.ROOT_LOGGER_NAME, root);
        initEvaluatorMap();
        size = 1;
        this.frameworkPackages = new ArrayList<String>();
    }

    void initEvaluatorMap() {
        putObject(EVALUATOR_MAP, new HashMap<String, EventEvaluator<?>>());
    }

    /**
     * A new instance of LoggerContextRemoteView needs to be created each time the
     * name or propertyMap (including keys or values) changes.
     */
    private void updateLoggerContextVO() {
        loggerContextRemoteView = new LoggerContextVO(this);
    }

    @Override
    public void putProperty(String key, String val) {
        super.putProperty(key, val);
        updateLoggerContextVO();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        updateLoggerContextVO();
    }

    public final Logger getLogger(final Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    @Override
    public Logger getLogger(final String name) {

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
            int h = LoggerNameUtil.getSeparatorIndexOf(name, i);
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
     * @param name the name of the logger to search for.
     */
    public Logger exists(String name) {
        return (Logger) loggerCache.get(name);
    }

    final void noAppenderDefinedWarning(final Logger logger) {
        if (noAppenderWarning++ == 0) {
            getStatusManager().add(new WarnStatus("No appenders present in context [" + getName() + "] for logger [" + logger.getName() + "].", logger));
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
     * This method clears all internal properties, except internal status messages,
     * closes all appenders, removes any turboFilters, fires an OnReset event,
     * removes all status listeners, removes all context listeners
     * (except those which are reset resistant).
     * <p/>
     * As mentioned above, internal status messages survive resets.
     */
    @Override
    public void reset() {
        resetCount++;
        super.reset();
        initEvaluatorMap();
        initCollisionMaps();
        root.recursiveReset();
        resetTurboFilterList();
        cancelScheduledTasks();
        fireOnReset();
        resetListenersExceptResetResistant();
        resetStatusListeners();
    }

    private void cancelScheduledTasks() {
        for(ScheduledFuture<?> sf: scheduledFutures) {
            sf.cancel(false);
        }
        scheduledFutures.clear();
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
     * First processPriorToRemoval all registered turbo filters and then clear the registration
     * list.
     */
    public void resetTurboFilterList() {
        for (TurboFilter tf : turboFilterList) {
            tf.stop();
        }
        turboFilterList.clear();
    }

    final FilterReply getTurboFilterChainDecision_0_3OrMore(final Marker marker, final Logger logger, final Level level, final String format,
                    final Object[] params, final Throwable t) {
        if (turboFilterList.size() == 0) {
            return FilterReply.NEUTRAL;
        }
        return turboFilterList.getTurboFilterChainDecision(marker, logger, level, format, params, t);
    }

    final FilterReply getTurboFilterChainDecision_1(final Marker marker, final Logger logger, final Level level, final String format, final Object param,
                    final Throwable t) {
        if (turboFilterList.size() == 0) {
            return FilterReply.NEUTRAL;
        }
        return turboFilterList.getTurboFilterChainDecision(marker, logger, level, format, new Object[] { param }, t);
    }

    final FilterReply getTurboFilterChainDecision_2(final Marker marker, final Logger logger, final Level level, final String format, final Object param1,
                    final Object param2, final Throwable t) {
        if (turboFilterList.size() == 0) {
            return FilterReply.NEUTRAL;
        }
        return turboFilterList.getTurboFilterChainDecision(marker, logger, level, format, new Object[] { param1, param2 }, t);
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

    void fireOnLevelChange(Logger logger, Level level) {
        for (LoggerContextListener listener : loggerContextListenerList) {
            listener.onLevelChange(logger, level);
        }
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

    public void start() {
        super.start();
        fireOnStart();
    }

    public void stop() {
        reset();
        fireOnStop();
        resetAllListeners();
        super.stop();
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

    /**
     * List of packages considered part of the logging framework such that they are never considered
     * as callers of the logging framework. This list used to compute the caller for logging events.
     * <p/>
     * To designate package "com.foo" as well as all its subpackages as being part of the logging framework, simply add
     * "com.foo" to this list.
     *
     * @return list of framework packages
     */
    public List<String> getFrameworkPackages() {
        return frameworkPackages;
    }

    public SequenceNumberGenerator getSequenceNumberGenerator() {
        return null;
    }
}
