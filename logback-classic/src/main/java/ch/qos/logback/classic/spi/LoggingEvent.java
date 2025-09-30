/**
 * Logback: the reliable, generic, fast and flexible logging framework. Copyright (C) 1999-2015, QOS.ch. All rights
 * reserved.
 *
 * This program and the accompanying materials are dual-licensed under either the terms of the Eclipse Public License
 * v1.0 as published by the Eclipse Foundation
 *
 * or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1 as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.spi;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.util.EnvUtil;
import ch.qos.logback.core.util.StringUtil;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
 * The internal representation of logging events. When an affirmative decision is made to log then a
 * <code>LoggingEvent</code> instance is created. This instance is passed around to the different logback-classic
 * components.
 * <p/>
 * <p>
 * Writers of logback-classic components such as appenders should be aware of that some of the LoggingEvent fields are
 * initialized lazily. Therefore, an appender wishing to output data to be later correctly read by a receiver, must
 * initialize "lazy" fields prior to writing them out. See the {@link #prepareForDeferredProcessing()} method for the
 * exact list.
 * </p>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class LoggingEvent implements ILoggingEvent {

    public static final String VIRTUAL_THREAD_NAME_PREFIX = "virtual-";
    public static final String REGULAR_UNNAMED_THREAD_PREFIX = "unnamed-";

    /**
     * Fully qualified name of the calling Logger class. This field does not survive serialization.
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
     * This field should not be accessed directly. You should use the {@link #getLevel} method instead.
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
     * The number of milliseconds elapsed from 1/1/1970 until logging event was created.
     */
    private Instant instant;

    private long timeStamp;
    private int nanoseconds;

    private long sequenceNumber;

    public LoggingEvent() {
    }

    public LoggingEvent(String fqcn, Logger logger, Level level, String message, Throwable throwable,
            Object[] argArray) {
        this.fqnOfLoggerClass = fqcn;
        this.loggerName = logger.getName();
        this.loggerContext = logger.getLoggerContext();
        this.loggerContextVO = loggerContext.getLoggerContextRemoteView();
        this.level = level;

        this.message = message;
        this.argumentArray = argArray;

        Instant instant = Clock.systemUTC().instant();
        initTmestampFields(instant);

        if (loggerContext != null) {
            SequenceNumberGenerator sequenceNumberGenerator = loggerContext.getSequenceNumberGenerator();
            if (sequenceNumberGenerator != null)
                sequenceNumber = sequenceNumberGenerator.nextSequenceNumber();
        }

        if (throwable == null) {
            throwable = extractThrowableAnRearrangeArguments(argArray);
        }

        if (throwable != null) {
            this.throwableProxy = new ThrowableProxy(throwable);

            if (loggerContext != null && loggerContext.isPackagingDataEnabled()) {
                this.throwableProxy.calculatePackagingData();
            }
        }
    }

    void initTmestampFields(@NonNull Instant instant) {
        this.instant = instant;
        long epochSecond = instant.getEpochSecond();
        this.nanoseconds = instant.getNano();
        long milliseconds = nanoseconds / 1000_000;
        this.timeStamp = (epochSecond * 1000) + (milliseconds);
    }

    private @Nullable Throwable extractThrowableAnRearrangeArguments(@Nullable Object[] argArray) {
        Throwable extractedThrowable = EventArgUtil.extractThrowable(argArray);
        if (EventArgUtil.successfulExtraction(extractedThrowable)) {
            this.argumentArray = EventArgUtil.trimmedCopy(argArray);
        }
        return extractedThrowable;
    }

    public void setArgumentArray(@Nullable Object[] argArray) {
        if (this.argumentArray != null) {
            throw new IllegalStateException("argArray has been already set");
        }
        this.argumentArray = argArray;
    }

    public @Nullable Object[] getArgumentArray() {
        return this.argumentArray;
    }

    public void addKeyValuePair(@NonNull KeyValuePair kvp) {
        if (keyValuePairs == null) {
            keyValuePairs = new ArrayList<>(4);
        }
        keyValuePairs.add(kvp);
    }

    public void setKeyValuePairs(@Nullable List<KeyValuePair> kvpList) {
        this.keyValuePairs = kvpList;
    }

    @Override
    public @Nullable List<KeyValuePair> getKeyValuePairs() {
        return this.keyValuePairs;
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

    public @NonNull String getThreadName() {
        if (threadName == null) {
            threadName = extractThreadName(Thread.currentThread());
        }
        return threadName;
    }

    /**
     * Extracts the name of aThread by calling {@link Thread#getName()}. If the value is null, then use the value
     * returned by {@link Thread#getId()} prefixing with {@link #VIRTUAL_THREAD_NAME_PREFIX} if thread is virtual or
     * with {@link #REGULAR_UNNAMED_THREAD_PREFIX} if regular.
     *
     * @param aThread
     * @return
     * @since 1.5.0
     */
    private @NonNull String extractThreadName(@Nullable Thread aThread) {
        if (aThread == null) {
            return CoreConstants.NA;
        }
        String threadName = aThread.getName();
        if (StringUtil.notNullNorEmpty(threadName))
            return threadName;
        Long virtualThreadId = getVirtualThreadId(aThread);
        if (virtualThreadId != null) {
            return VIRTUAL_THREAD_NAME_PREFIX + virtualThreadId;
        } else {
            return REGULAR_UNNAMED_THREAD_PREFIX + aThread.getId();
        }
    }
    // +

    /**
     * Return the threadId if running under JDK 21+ and the thread is a virtual thread, return null otherwise.
     *
     * @param aThread
     * @return Return the threadId if the thread is a virtual thread, return null otherwise.
     */
    Long getVirtualThreadId(@NonNull Thread aThread) {
        if (EnvUtil.isJDK21OrHigher()) {
            try {
                Method isVirtualMethod = Thread.class.getMethod("isVirtual");
                boolean isVirtual = (boolean) isVirtualMethod.invoke(aThread);
                if (isVirtual)
                    return aThread.getId();
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                return null;
            }
        }
        return null;
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
     * This method should be called prior to serializing an event. It should also be called when using asynchronous or
     * deferred logging.
     * <p/>
     * <p/>
     * Note that due to performance concerns, this method does NOT extract caller data. It is the responsibility of the
     * caller to extract caller information.
     */
    public void prepareForDeferredProcessing() {
        this.getFormattedMessage();
        this.getThreadName();
        // fixes http://jira.qos.ch/browse/LBCLASSIC-104
        this.getMDCPropertyMap();
    }

    public void setLoggerContext(LoggerContext lc) {
        this.loggerContext = lc;
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

    /**
     * Return the {@link Instant} corresponding to the creation of this event.
     *
     * @see {@link #getTimeStamp()}
     * @since 1.3
     */
    public Instant getInstant() {
        return instant;
    }

    /**
     * Set {@link Instant} corresponding to the creation of this event.
     *
     * The value of {@link #getTimeStamp()} will be overridden as well.
     */
    public void setInstant(Instant instant) {
        initTmestampFields(instant);
    }

    /**
     * Return the number of elapsed milliseconds since epoch in UTC.
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Return the number of nanoseconds past the {@link #getTimeStamp() timestamp in seconds}.
     *
     * @since 1.3.0
     */
    @Override
    public int getNanoseconds() {
        return nanoseconds;
    }

    /**
     * Set the number of elapsed milliseconds since epoch in UTC.
     *
     * Setting the timestamp will override the value contained in {@link #getInstant}. Nanoseconds value will be
     * computed form the provided millisecond value.
     */
    public void setTimeStamp(long timeStamp) {
        Instant instant = Instant.ofEpochMilli(timeStamp);
        setInstant(instant);
    }

    @Override
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sn) {
        sequenceNumber = sn;
    }

    public void setLevel(Level level) {
        if (this.level != null) {
            throw new IllegalStateException("The level has been already set for this event.");
        }
        this.level = level;
    }

    /**
     * Get the caller information for this logging event. If caller information is null at the time of its invocation,
     * this method extracts location information. The collected information is cached for future use.
     * <p/>
     * <p>
     * Note that after serialization it is impossible to correctly extract caller information.
     * </p>
     */
    public @NonNull StackTraceElement[] getCallerData() {
        if (callerDataArray == null) {
            callerDataArray = CallerData.extract(new Throwable(), fqnOfLoggerClass,
                    loggerContext.getMaxCallerDataDepth(), loggerContext.getFrameworkPackages());
        }
        return callerDataArray;
    }

    public boolean hasCallerData() {
        return (callerDataArray != null);
    }

    public void setCallerData(@Nullable StackTraceElement[] callerDataArray) {
        this.callerDataArray = callerDataArray;
    }

    public @Nullable List<Marker> getMarkerList() {
        return markerList;
    }

    public void addMarker(@Nullable Marker marker) {
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
    public String getFormattedMessage() {
        if (formattedMessage != null) {
            return formattedMessage;
        }
        if (argumentArray != null) {
            if(throwableProxy == null) {
                formattedMessage = MessageFormatter.arrayFormat(message, argumentArray).getMessage();
            } else {
                // very rare case where the argument array ends with two exceptions
                // See https://github.com/qos-ch/logback/issues/876
                formattedMessage = MessageFormatter.arrayFormat(message, argumentArray, null).getMessage();
            }
        } else {
            formattedMessage = message;
        }

        return formattedMessage;
    }

    public @NonNull Map<String, String> getMDCPropertyMap() {
        // populate mdcPropertyMap if null
        if (mdcPropertyMap == null) {
            MDCAdapter mdcAdapter = loggerContext.getMDCAdapter();
            if (mdcAdapter instanceof LogbackMDCAdapter)
                mdcPropertyMap = ((LogbackMDCAdapter) mdcAdapter).getPropertyMap();
            else
                mdcPropertyMap = mdcAdapter.getCopyOfContextMap();
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
    public void setMDCPropertyMap(@Nullable Map<String, String> map) {
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
    public @NonNull Map<String, String> getMdc() {
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
     * LoggerEventVO instances should be used for serialization. Use {@link LoggingEventVO#build(ILoggingEvent) build}
     * method to create the LoggerEventVO instance.
     *
     * @since 1.0.11
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new UnsupportedOperationException(this.getClass() + " does not support serialization. "
                + "Use LoggerEventVO instance instead. See also LoggerEventVO.build method.");
    }

}
