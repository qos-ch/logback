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
package ch.qos.logback.classic.spi;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Map;

import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.LogbackMDCAdapter;

import org.slf4j.spi.MDCAdapter;

/**
 * The internal representation of logging events. When an affirmative decision
 * is made to log then a <code>LoggingEvent</code> instance is created. This
 * instance is passed around to the different logback-classic components.
 * <p/>
 * <p>
 * Writers of logback-classic components such as appenders should be aware of
 * that some of the LoggingEvent fields are initialized lazily. Therefore, an
 * appender wishing to output data to be later correctly read by a receiver,
 * must initialize "lazy" fields prior to writing them out. See the
 * {@link #prepareForDeferredProcessing()} method for the exact list.
 * </p>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class LoggingEvent implements ILoggingEvent {

    /**
     * Fully qualified name of the calling Logger class. This field does not
     * survive serialization.
     * <p/>
     * <p/>
     * Note that the getCallerInformation() method relies on this fact.
     */
    transient String fqnOfLoggerClass;

    /**
     * The name of thread in which this logging event was generated.
     */
    private String threadName;

    private String loggerName;
    private LoggerContext loggerContext;
    private LoggerContextVO loggerContextVO;

    /**
     * Level of logging event.
     * <p/>
     * <p>
     * This field should not be accessed directly. You should use the
     * {@link #getLevel} method instead.
     * </p>
     */
    private transient Level level;

    private String message;

    // we gain significant space at serialization time by marking
    // formattedMessage as transient and constructing it lazily in
    // getFormattedMessage()
    transient String formattedMessage;

    private transient Object[] argumentArray;

    private ThrowableProxy throwableProxy;

    private StackTraceElement[] callerDataArray;

    private Marker marker;

    private Map<String, String> mdcPropertyMap;

    /**
     * The number of milliseconds elapsed from 1/1/1970 until logging event was
     * created.
     */
    private long timeStamp;

    public LoggingEvent() {
    }

    public LoggingEvent(String fqcn, Logger logger, Level level, String message, Throwable throwable, Object[] argArray) {
        this.fqnOfLoggerClass = fqcn;
        this.loggerName = logger.getName();
        this.loggerContext = logger.getLoggerContext();
        this.loggerContextVO = loggerContext.getLoggerContextRemoteView();
        this.level = level;

        this.message = message;
        this.argumentArray = argArray;

        if (throwable == null) {
            throwable = extractThrowableAnRearrangeArguments(argArray);
        }

        if (throwable != null) {
            this.throwableProxy = new ThrowableProxy(throwable);
            LoggerContext lc = logger.getLoggerContext();
            if (lc.isPackagingDataEnabled()) {
                this.throwableProxy.calculatePackagingData();
            }
        }

        timeStamp = System.currentTimeMillis();
    }

    private Throwable extractThrowableAnRearrangeArguments(Object[] argArray) {
        Throwable extractedThrowable = EventArgUtil.extractThrowable(argArray);
        if (EventArgUtil.successfulExtraction(extractedThrowable)) {
            this.argumentArray = EventArgUtil.trimmedCopy(argArray);
        }
        return extractedThrowable;
    }

    public void setArgumentArray(Object[] argArray) {
        if (this.argumentArray != null) {
            throw new IllegalStateException("argArray has been already set");
        }
        this.argumentArray = argArray;
    }

    public Object[] getArgumentArray() {
        return this.argumentArray;
    }

    public Level getLevel() {
        return level;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getThreadName() {
        if (threadName == null) {
            threadName = (Thread.currentThread()).getName();
        }
        return threadName;
    }

    /**
     * @param threadName The threadName to set.
     * @throws IllegalStateException If threadName has been already set.
     */
    public void setThreadName(String threadName) throws IllegalStateException {
        if (this.threadName != null) {
            throw new IllegalStateException("threadName has been already set");
        }
        this.threadName = threadName;
    }

    /**
     * Returns the throwable information contained within this event. May be
     * <code>null</code> if there is no such information.
     */
    public IThrowableProxy getThrowableProxy() {
        return throwableProxy;
    }

    /**
     * Set this event's throwable information.
     */
    public void setThrowableProxy(ThrowableProxy tp) {
        if (throwableProxy != null) {
            throw new IllegalStateException("ThrowableProxy has been already set.");
        } else {
            throwableProxy = tp;
        }
    }

    /**
     * This method should be called prior to serializing an event. It should also
     * be called when using asynchronous or deferred logging.
     * <p/>
     * <p/>
     * Note that due to performance concerns, this method does NOT extract caller
     * data. It is the responsibility of the caller to extract caller information.
     */
    public void prepareForDeferredProcessing() {
        this.getFormattedMessage();
        this.getThreadName();
        // fixes http://jira.qos.ch/browse/LBCLASSIC-104
        this.getMDCPropertyMap();
    }

    public LoggerContextVO getLoggerContextVO() {
        return loggerContextVO;
    }

    public void setLoggerContextRemoteView(LoggerContextVO loggerContextVO) {
        this.loggerContextVO = loggerContextVO;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        if (this.message != null) {
            throw new IllegalStateException("The message for this event has been set already.");
        }
        this.message = message;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setLevel(Level level) {
        if (this.level != null) {
            throw new IllegalStateException("The level has been already set for this event.");
        }
        this.level = level;
    }

    /**
     * Get the caller information for this logging event. If caller information is
     * null at the time of its invocation, this method extracts location
     * information. The collected information is cached for future use.
     * <p/>
     * <p>
     * Note that after serialization it is impossible to correctly extract caller
     * information.
     * </p>
     */
    public StackTraceElement[] getCallerData() {
        if (callerDataArray == null) {
            callerDataArray = CallerData
                            .extract(new Throwable(), fqnOfLoggerClass, loggerContext.getMaxCallerDataDepth(), loggerContext.getFrameworkPackages());
        }
        return callerDataArray;
    }

    public boolean hasCallerData() {
        return (callerDataArray != null);
    }

    public void setCallerData(StackTraceElement[] callerDataArray) {
        this.callerDataArray = callerDataArray;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        if (this.marker != null) {
            throw new IllegalStateException("The marker has been already set for this event.");
        }
        this.marker = marker;
    }

    public long getContextBirthTime() {
        return loggerContextVO.getBirthTime();
    }

    // lazy computation as suggested in LOGBACK-495
    public String getFormattedMessage() {
        if (formattedMessage != null) {
            return formattedMessage;
        }
        if (argumentArray != null) {
            formattedMessage = MessageFormatter.arrayFormat(message, argumentArray).getMessage();
        } else {
            formattedMessage = message;
        }

        return formattedMessage;
    }

    public Map<String, String> getMDCPropertyMap() {
        // populate mdcPropertyMap if null
        if (mdcPropertyMap == null) {
            MDCAdapter mdc = MDC.getMDCAdapter();
            if (mdc instanceof LogbackMDCAdapter)
                mdcPropertyMap = ((LogbackMDCAdapter) mdc).getPropertyMap();
            else
                mdcPropertyMap = mdc.getCopyOfContextMap();
        }
        // mdcPropertyMap still null, use emptyMap()
        if (mdcPropertyMap == null)
            mdcPropertyMap = Collections.emptyMap();

        return mdcPropertyMap;
    }

    /**
     * Set the MDC map for this event.
     *
     * @param map
     * @since 1.0.8
     */
    public void setMDCPropertyMap(Map<String, String> map) {
        if (mdcPropertyMap != null) {
            throw new IllegalStateException("The MDCPropertyMap has been already set for this event.");
        }
        this.mdcPropertyMap = map;

    }

    /**
     * Synonym for [@link #getMDCPropertyMap}.
     *
     * @deprecated Replaced by [@link #getMDCPropertyMap}
     */
    public Map<String, String> getMdc() {
        return getMDCPropertyMap();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(level).append("] ");
        sb.append(getFormattedMessage());
        return sb.toString();
    }

    /**
     * LoggerEventVO instances should be used for serialization. Use
     * {@link LoggingEventVO#build(ILoggingEvent) build} method to create the LoggerEventVO instance.
     *
     * @since 1.0.11
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new UnsupportedOperationException(this.getClass() + " does not support serialization. "
                        + "Use LoggerEventVO instance instead. See also LoggerEventVO.build method.");
    }

}
