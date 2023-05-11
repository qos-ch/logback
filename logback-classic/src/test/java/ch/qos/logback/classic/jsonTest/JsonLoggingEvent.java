/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2023, QOS.ch. All rights reserved.
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

package ch.qos.logback.classic.jsonTest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.JsonEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.PubLoggerContextVO;
import ch.qos.logback.classic.spi.PubThrowableProxy;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.helpers.MessageFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties({ })
public class JsonLoggingEvent implements ILoggingEvent {
    public String threadName;
    public String loggerName;


    @JsonAlias({"context"})
    public LoggerContextVO loggerContextVO;

    public Level level;
    public String message;

    private transient String formattedMessage;

    @JsonAlias({"arguments"})
    public Object[] argumentArray;

    @JsonAlias({"throwable"})
    public PubThrowableProxy throwableProxy;

    @JsonIgnore
    public StackTraceElement[] callerDataArray;

    @JsonAlias({"markers"})
    public List<Marker> markerList;

    @JsonAlias({"kvp"})
    public List<KeyValuePair> kvpList;

    @JsonAlias({"mdc"})
    public Map<String, String> mdcPropertyMap;

    public long timestamp;
    public int nanoseconds;
    public long sequenceNumber;

    public String getThreadName() {
        return threadName;
    }


    public LoggerContextVO getLoggerContextVO() {
        return loggerContextVO;
    }

    public String getLoggerName() {
        return loggerName;
    }


    //@JsonIgnore
    public Level getLevel() {
        return level;
    }

    //@JsonIgnore
    public void setLevel(Level aLavel) {
         level = aLavel;
    }

    public String getMessage() {
        return message;
    }

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

    public Object[] getArgumentArray() {
        return argumentArray;
    }

    public IThrowableProxy getThrowableProxy() {
        return throwableProxy;
    }

    public StackTraceElement[] getCallerData() {
        return callerDataArray;
    }

    public boolean hasCallerData() {
        return callerDataArray != null;
    }

    public List<Marker> getMarkerList() {
        return markerList;
    }

    public long getTimeStamp() {
        return timestamp;
    }

    @Override
    public int getNanoseconds() {
        return nanoseconds;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public long getContextBirthTime() {
        return loggerContextVO.getBirthTime();
    }

    public LoggerContextVO getContextLoggerRemoteView() {
        return loggerContextVO;
    }

    public Map<String, String> getMDCPropertyMap() {
        return mdcPropertyMap;
    }

    public Map<String, String> getMdc() {
        return mdcPropertyMap;
    }

    public void setMdc( Map<String, String> map) {
         mdcPropertyMap = map;
    }

    public void prepareForDeferredProcessing() {
    }

    @Override
    public List<KeyValuePair> getKeyValuePairs() {
        return kvpList;
    }

    public void setKeyValuePairs( List<KeyValuePair> aList) {
        kvpList = aList;
    }



    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(timestamp);
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
