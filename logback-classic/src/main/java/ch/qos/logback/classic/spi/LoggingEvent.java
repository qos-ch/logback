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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.MDCAdapter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.spi.SequenceNumberGenerator;

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

    private List<Marker> markerList;

    private Map<String, String> mdcPropertyMap;

    /**
     * @since 1.3.0
     */
    List<KeyValuePair> keyValuePairs;

    /**
     * The number of milliseconds elapsed from 1/1/1970 until logging event was
     * created.
     */
    private long timeStamp;

    private long sequenceNumber;

    public LoggingEvent() {
    }

    public LoggingEvent(final String fqcn, final Logger logger, final Level level, final String message, Throwable throwable, final Object[] argArray) {
        fqnOfLoggerClass = fqcn;
        loggerName = logger.getName();
        loggerContext = logger.getLoggerContext();
        loggerContextVO = loggerContext.getLoggerContextRemoteView();
        this.level = level;

        this.message = message;
        argumentArray = argArray;
        // List<Object> l = Arrays.asList(argArray);

        timeStamp = System.currentTimeMillis();

        if (loggerContext != null) {
            final SequenceNumberGenerator sequenceNumberGenerator = loggerContext.getSequenceNumberGenerator();
            if (sequenceNumberGenerator != null) {
                sequenceNumber = sequenceNumberGenerator.nextSequenceNumber();
            }
        }

        if (throwable == null) {
            throwable = extractThrowableAnRearrangeArguments(argArray);
        }

        if (throwable != null) {
            throwableProxy = new ThrowableProxy(throwable);

            if (loggerContext != null && loggerContext.isPackagingDataEnabled()) {
                throwableProxy.calculatePackagingData();
            }
        }

    }

    private Throwable extractThrowableAnRearrangeArguments(final Object[] argArray) {
        final Throwable extractedThrowable = EventArgUtil.extractThrowable(argArray);
        if (EventArgUtil.successfulExtraction(extractedThrowable)) {
            argumentArray = EventArgUtil.trimmedCopy(argArray);
        }
        return extractedThrowable;
    }

    public void setArgumentArray(final Object[] argArray) {
        if (argumentArray != null) {
            throw new IllegalStateException("argArray has been already set");
        }
        argumentArray = argArray;
    }

    @Override
    public Object[] getArgumentArray() {
        return argumentArray;
    }

    public void addKeyValuePair(final KeyValuePair kvp) {
        if (keyValuePairs == null) {
            keyValuePairs = new ArrayList<>(4);
        }
        keyValuePairs.add(kvp);
    }

    public void setKeyValuePairs(final List<KeyValuePair> kvpList) {
        keyValuePairs = kvpList;
    }

    @Override
    public List<KeyValuePair> getKeyValuePairs() {
        return keyValuePairs;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(final String loggerName) {
        this.loggerName = loggerName;
    }

    @Override
    public String getThreadName() {
        if (threadName == null) {
            threadName = Thread.currentThread().getName();
        }
        return threadName;
    }

    /**
     * @param threadName The threadName to set.
     * @throws IllegalStateException If threadName has been already set.
     */
    public void setThreadName(final String threadName) throws IllegalStateException {
        if (this.threadName != null) {
            throw new IllegalStateException("threadName has been already set");
        }
        this.threadName = threadName;
    }

    /**
     * Returns the throwable information contained within this event. May be
     * <code>null</code> if there is no such information.
     */
    @Override
    public IThrowableProxy getThrowableProxy() {
        return throwableProxy;
    }

    /**
     * Set this event's throwable information.
     */
    public void setThrowableProxy(final ThrowableProxy tp) {
        if (throwableProxy != null) {
            throw new IllegalStateException("ThrowableProxy has been already set.");
        }
        throwableProxy = tp;
    }

    /**
     * This method should be called prior to serializing an event. It should also
     * be called when using asynchronous or deferred logging.
     * <p/>
     * <p/>
     * Note that due to performance concerns, this method does NOT extract caller
     * data. It is the responsibility of the caller to extract caller information.
     */
    @Override
    public void prepareForDeferredProcessing() {
        getFormattedMessage();
        getThreadName();
        // fixes http://jira.qos.ch/browse/LBCLASSIC-104
        getMDCPropertyMap();
    }

    public void setLoggerContext(final LoggerContext lc) {
        loggerContext = lc;
    }

    @Override
    public LoggerContextVO getLoggerContextVO() {
        return loggerContextVO;
    }

    public void setLoggerContextRemoteView(final LoggerContextVO loggerContextVO) {
        this.loggerContextVO = loggerContextVO;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        if (this.message != null) {
            throw new IllegalStateException("The message for this event has been set already.");
        }
        this.message = message;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(final long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSquenceNumber(final long sn) {
        sequenceNumber = sn;
    }

    public void setLevel(final Level level) {
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
    @Override
    public StackTraceElement[] getCallerData() {
        if (callerDataArray == null) {
            callerDataArray = CallerData.extract(new Throwable(), fqnOfLoggerClass, loggerContext.getMaxCallerDataDepth(),
                            loggerContext.getFrameworkPackages());
        }
        return callerDataArray;
    }

    @Override
    public boolean hasCallerData() {
        return callerDataArray != null;
    }

    public void setCallerData(final StackTraceElement[] callerDataArray) {
        this.callerDataArray = callerDataArray;
    }

    @Override
    public List<Marker> getMarkerList() {
        return markerList;
    }

    public void addMarker(final Marker marker) {
        if (marker == null) {
            return;
        }
        if (markerList == null) {
            markerList = new ArrayList<>(4);
        }
        markerList.add(marker);
    }

    public long getContextBirthTime() {
        return loggerContextVO.getBirthTime();
    }

    // lazy computation as suggested in LOGBACK-495
    @Override
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

    @Override
    public Map<String, String> getMDCPropertyMap() {
        // populate mdcPropertyMap if null
        if (mdcPropertyMap == null) {
            final MDCAdapter mdc = MDC.getMDCAdapter();
            if (mdc instanceof LogbackMDCAdapter) {
                mdcPropertyMap = ((LogbackMDCAdapter) mdc).getPropertyMap();
            } else {
                mdcPropertyMap = mdc.getCopyOfContextMap();
            }
        }
        // mdcPropertyMap still null, use emptyMap()
        if (mdcPropertyMap == null) {
            mdcPropertyMap = Collections.emptyMap();
        }

        return mdcPropertyMap;
    }

    /**
     * Set the MDC map for this event.
     *
     * @param map
     * @since 1.0.8
     */
    public void setMDCPropertyMap(final Map<String, String> map) {
        if (mdcPropertyMap != null) {
            throw new IllegalStateException("The MDCPropertyMap has been already set for this event.");
        }
        mdcPropertyMap = map;

    }

    /**
     * Synonym for [@link #getMDCPropertyMap}.
     *
     * @deprecated Replaced by [@link #getMDCPropertyMap}
     */
    @Deprecated
    @Override
    public Map<String, String> getMdc() {
        return getMDCPropertyMap();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
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
    private void writeObject(final ObjectOutputStream out) throws IOException {
        throw new UnsupportedOperationException(
                        this.getClass() + " does not support serialization. " + "Use LoggerEventVO instance instead. See also LoggerEventVO.build method.");
    }

}
