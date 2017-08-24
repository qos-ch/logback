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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.helpers.MarkerIgnoringBase;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.CoreConstants;

public class HLogger extends MarkerIgnoringBase {

    private static final long serialVersionUID = 1L;

    static int instanceCount = 0;

    /**
     * The name of this logger
     */
    private String name;

    // The assigned levelInt of this logger. Can be null.
    private Level level;

    // The effective levelInt is the assigned levelInt and if null, a levelInt is
    // inherited form a parent.
    private Level effectiveLevel;

    /**
     * The parent of this category. All categories have at least one ancestor
     * which is the root category.
     */
    HLogger parent;

    /**
     * The children of this logger. A logger may have zero or more children.
     */
    Map<String, HLogger> childrenMap;

    /**
     * Array of appenders.
     */
    private ArrayList<Appender<ILoggingEvent>> appenderList;

    /**
     * Additivity is set to true by default, that is children inherit the
     * appenders of their ancestors by default. If this variable is set to
     * <code>false</code> then the appenders located in the ancestors of this
     * logger will not be used. However, the children of this logger will inherit
     * its appenders, unless the children have their additivity flag set to
     * <code>false</code> too. See the user manual for more details.
     */
    protected boolean additive = true;

    HLogger(String name, HLogger parent) {
        this.name = name;
        this.parent = parent;
        instanceCount++;
    }

    Level getEffectiveLevel() {
        return effectiveLevel;
    }

    Level getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    private boolean isRootLogger() {
        // only the root logger has a null parent
        return parent == null;
    }

    /**
     * Get a child by its suffix.
     * 
     * <p>
     * IMPORTANT: Calls to this method must be within a syncronized block on this
     * logger!
     * 
     * @param suffix
     * @return
     */
    HLogger getChildBySuffix(final String suffix) {
        if (childrenMap == null) {
            return null;
        } else {
            return (HLogger) childrenMap.get(suffix);
        }
    }

    public synchronized void setLevel(Level newLevel) {
        if (level == newLevel) {
            // nothing to do;
            return;
        }

        level = newLevel;
        effectiveLevel = newLevel;
        if (childrenMap != null) {
            for (Iterator<HLogger> i = childrenMap.values().iterator(); i.hasNext();) {
                HLogger child = (HLogger) i.next();

                // tell child to handle parent levelInt change
                child.handleParentLevelChange(effectiveLevel);
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
            effectiveLevel = newParentLevel;

            // propagate the parent levelInt change to this logger's children
            if (childrenMap != null) {
                for (Iterator<HLogger> i = childrenMap.values().iterator(); i.hasNext();) {
                    HLogger child = (HLogger) i.next();
                    // tell child to handle parent levelInt change
                    child.handleParentLevelChange(effectiveLevel);
                }
            }
        }
    }

    /**
     * Remove all previously added appenders from this logger instance. <p/> This
     * is useful when re-reading configuration information.
     */
    public synchronized void removeAllAppenders() {
        if (appenderList != null) {
            int len = appenderList.size();
            for (int i = 0; i < len; i++) {
                Appender<ILoggingEvent> a = appenderList.get(i);
                a.stop();
            }
            appenderList.clear();
            appenderList = null;
        }
    }

    /**
     * Invoke all the appenders of this logger.
     * 
     * @param event
     *          The event to log
     */
    public void callAppenders(ILoggingEvent event) {
        int writes = 0;

        for (HLogger l = this; l != null; l = l.parent) {
            // Protected against simultaneous call to addAppender, removeAppender,...
            synchronized (l) {
                if (l.appenderList != null) {
                    writes += l.appendLoopOnAppenders(event);
                }
                if (!l.additive) {
                    break;
                }
            }
        }

        // No appenders in hierarchy, warn user only once.
        // if(!hierarchy.emittedNoAppenderWarning && writes == 0) {
        // LogLog.error("No appenders could be found for category (" +
        // this.getName() + ").");
        // LogLog.error("Please initialize the log4j system properly.");
        // hierarchy.emittedNoAppenderWarning = true;
        // }
    }

    private int appendLoopOnAppenders(ILoggingEvent event) {
        int size = 0;
        Appender<ILoggingEvent> appender;

        if (appenderList != null) {
            size = appenderList.size();
            for (int i = 0; i < size; i++) {
                appender = appenderList.get(i);
                appender.doAppend(event);
            }
        }
        return size;
    }

    /**
     * Remove the appender passed as parameter form the list of appenders.
     */
    public synchronized void removeAppender(Appender<ILoggingEvent> appender) {
        if ((appender == null) || (appenderList == null)) {
        }
        appenderList.remove(appender);
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
    HLogger createChildByLastNamePart(final String lastPart) {
        int i_index = lastPart.indexOf(CoreConstants.DOT);
        if (i_index != -1) {
            throw new IllegalArgumentException("Child name [" + lastPart + " passed as parameter, may not include [" + CoreConstants.DOT + "]");
        }

        if (childrenMap == null) {
            childrenMap = new HashMap<String, HLogger>(2);
        }
        HLogger childHLogger;
        if (this.isRootLogger()) {
            childHLogger = new HLogger(lastPart, this);
        } else {
            childHLogger = new HLogger(name + CoreConstants.DOT + lastPart, this);
        }
        childrenMap.put(lastPart, childHLogger);
        childHLogger.effectiveLevel = this.effectiveLevel;
        return childHLogger;
    }

    public final void trace(String msg) {
        if (effectiveLevel.levelInt <= Level.TRACE_INT) {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }

    public void trace(String msg, Throwable t) {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public void trace(Object parameterizedMsg, Object param1) {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public void trace(String parameterizedMsg, Object param1, Object param2) {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public final void debug(String msg) {
        if (effectiveLevel.levelInt <= Level.DEBUG_INT) {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }

    public void debug(String msg, Throwable t) {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public void debug(Object parameterizedMsg, Object param1) {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public void debug(String parameterizedMsg, Object param1, Object param2) {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public void error(String msg) {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public void error(String msg, Throwable t) {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public void error(String parameterizedMsg, Object param1) {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public void error(String parameterizedMsg, Object param1, Object param2) {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public void info(String msg) {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public void info(String msg, Throwable t) {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public void info(String parameterizedMsg, Object param1) {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public void info(String parameterizedMsg, Object param1, Object param2) {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public boolean isTraceEnabled() {
        return false;
    }

    public boolean isDebugEnabled() {
        return false;
    }

    public boolean isErrorEnabled() {
        return false; // To change body of implemented methods use File | Settings |
        // File Templates.
    }

    public boolean isInfoEnabled() {
        return false; // To change body of implemented methods use File | Settings |
        // File Templates.
    }

    public boolean isWarnEnabled() {
        return false; // To change body of implemented methods use File | Settings |
        // File Templates.
    }

    public void warn(String msg) {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public void warn(String msg, Throwable t) {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public void warn(String parameterizedMsg, Object param1) {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public void warn(String parameterizedMsg, Object param1, Object param2) {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public void trace(String format, Object arg) {
    }

    public void trace(String format, Object[] argArray) {
    }

    public void debug(String format, Object arg) {
    }

    public void debug(String format, Object[] argArray) {
    }

    public void info(String format, Object[] argArray) {
    }

    public void warn(String format, Object[] argArray) {
    }

    public void error(String format, Object[] argArray) {
    }
}
