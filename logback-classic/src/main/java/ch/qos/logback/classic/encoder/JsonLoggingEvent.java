/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.classic.encoder;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.helpers.MessageFormatter;

import java.util.List;
import java.util.Map;

/**
 * A logging event read back from the output of {@link JsonEncoder}, as returned by
 * {@link JsonEventReader}.
 *
 * <p>Each getter returns what the getter of the same name returned on the event that was written,
 * for the members that were written. A member that was not written gives {@code null}, or 0 for a
 * number.</p>
 *
 * <p>This class only holds values; {@link #prepareForDeferredProcessing()} does nothing.</p>
 *
 * @since 1.6.8
 */
public class JsonLoggingEvent implements ILoggingEvent {

    private long sequenceNumber;
    private long timeStamp;
    private int nanoseconds;
    private Level level;
    private String threadName;
    private String loggerName;
    private LoggerContextVO loggerContextVO;
    private List<Marker> markerList;
    private Map<String, String> mdcPropertyMap;
    private List<KeyValuePair> keyValuePairs;
    private String message;
    private String formattedMessage;
    private Object[] argumentArray;
    private StackTraceElement[] callerData;
    private IThrowableProxy throwableProxy;

    @Override
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public int getNanoseconds() {
        return nanoseconds;
    }

    public void setNanoseconds(int nanoseconds) {
        this.nanoseconds = nanoseconds;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    @Override
    public LoggerContextVO getLoggerContextVO() {
        return loggerContextVO;
    }

    public void setLoggerContextVO(LoggerContextVO loggerContextVO) {
        this.loggerContextVO = loggerContextVO;
    }

    @Override
    public List<Marker> getMarkerList() {
        return markerList;
    }

    public void setMarkerList(List<Marker> markerList) {
        this.markerList = markerList;
    }

    @Override
    public Map<String, String> getMDCPropertyMap() {
        return mdcPropertyMap;
    }

    @Override
    public Map<String, String> getMdc() {
        return mdcPropertyMap;
    }

    public void setMDCPropertyMap(Map<String, String> mdcPropertyMap) {
        this.mdcPropertyMap = mdcPropertyMap;
    }

    @Override
    public List<KeyValuePair> getKeyValuePairs() {
        return keyValuePairs;
    }

    public void setKeyValuePairs(List<KeyValuePair> keyValuePairs) {
        this.keyValuePairs = keyValuePairs;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Return the formatted message: the {@code formattedMessage} member if it was written,
     * otherwise the message formatted with the arguments, as
     * {@link ch.qos.logback.classic.spi.LoggingEvent#getFormattedMessage()} does.
     *
     * @return the formatted message
     */
    @Override
    public String getFormattedMessage() {
        if (formattedMessage != null) {
            return formattedMessage;
        }
        if (argumentArray != null) {
            if (throwableProxy == null) {
                formattedMessage = MessageFormatter.arrayFormat(message, argumentArray).getMessage();
            } else {
                formattedMessage = MessageFormatter.arrayFormat(message, argumentArray, null).getMessage();
            }
        } else {
            formattedMessage = message;
        }
        return formattedMessage;
    }

    public void setFormattedMessage(String formattedMessage) {
        this.formattedMessage = formattedMessage;
    }

    @Override
    public Object[] getArgumentArray() {
        return argumentArray;
    }

    public void setArgumentArray(Object[] argumentArray) {
        this.argumentArray = argumentArray;
    }

    @Override
    public StackTraceElement[] getCallerData() {
        return callerData;
    }

    @Override
    public boolean hasCallerData() {
        return callerData != null;
    }

    public void setCallerData(StackTraceElement[] callerData) {
        this.callerData = callerData;
    }

    @Override
    public IThrowableProxy getThrowableProxy() {
        return throwableProxy;
    }

    public void setThrowableProxy(IThrowableProxy throwableProxy) {
        this.throwableProxy = throwableProxy;
    }

    @Override
    public void prepareForDeferredProcessing() {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(timeStamp);
        sb.append(" ");
        sb.append(level);
        sb.append(" [");
        sb.append(threadName);
        sb.append("] ");
        sb.append(loggerName);
        sb.append(" - ");
        sb.append(getFormattedMessage());
        return sb.toString();
    }
}
