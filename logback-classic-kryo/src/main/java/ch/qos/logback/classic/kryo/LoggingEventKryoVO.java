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
package ch.qos.logback.classic.kryo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.*;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.helpers.MessageFormatter;

import java.util.*;

final public class LoggingEventKryoVO implements ILoggingEvent {

    private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();

    /**
     * Runtime class of {@link Collections#unmodifiableMap}. Logback's MDC snapshot
     * is one of these wrapping a {@code HashMap}.
     */
    private static final Class<?> UNMODIFIABLE_MAP_CLASS =
            Collections.unmodifiableMap(new HashMap<String, String>()).getClass();
    String threadName;
    String loggerName;
    int levelInt;
    String message;
    Object[] argumentArray;

    List<Marker> markerList;
    List<KeyValuePair> keyValuePairList;
    Map<String, String> mdcPropertyMap;
    long timestamp;
    int nanoseconds;
    long sequenceNumber;
    ThrowableProxyVO throwableProxyVO;

    LoggerContextVO loggerContextVO;

    StackTraceElement[] callerData;

    public LoggingEventKryoVO() {
    }

    public LoggingEventKryoVO(ILoggingEvent le) {
        this.loggerName = le.getLoggerName();
        this.loggerContextVO = le.getLoggerContextVO();

        this.threadName = le.getThreadName();
        this.levelInt = le.getLevel().levelInt;
        this.message = le.getMessage();
        this.argumentArray = safeObjectArray(le.getArgumentArray());
        this.markerList = le.getMarkerList();
        this.keyValuePairList = le.getKeyValuePairs();
        Map<String, String> mdcMap = le.getMDCPropertyMap();
        // LoggingEvent substitutes Collections.emptyMap() when the adapter has no
        // map. Vanilla Kryo has no registration for that class. An empty map is
        // stored as null, which is what the event codecs already write for absence.
        this.mdcPropertyMap = (mdcMap == null || mdcMap.isEmpty()) ? null : mdcMap;
        this.timestamp = le.getTimeStamp();
        this.nanoseconds = le.getNanoseconds();
        this.sequenceNumber = le.getSequenceNumber();
        this.throwableProxyVO = ThrowableProxyVO.build(le.getThrowableProxy());
        if(le.hasCallerData()) {
            this.callerData = le.getCallerData();
        } else {
             this.callerData = null;
        }
    }

//    /**
//     * Empty maps serialize as {@link #EMPTY_MAP}. Logback's snapshot is a
//     * {@link Collections#unmodifiableMap} around a {@code HashMap}, which may
//     * still hold null values.
//     */
//    private static Map<String, String> getSafeMdcPropertyMap(ILoggingEvent le) {
//        Map<String, String> mdcMap = le.getMDCPropertyMap();
//
//        if (mdcMap == null)
//            return null;
//
//        if(mdcMap.isEmpty())
//            return EMPTY_MAP;
//
//        if (UNMODIFIABLE_MAP_CLASS.isInstance(mdcMap)) {
//            return mdcMap;
//        }
//
//        if (hasNullKeyOrValue(mdcMap)) {
//            return copyOfWithoutNullEntries(mdcMap);
//        }
//
//        return Map.copyOf(mdcMap);
//    }

    private static boolean hasNullKeyOrValue(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, String> copyOfWithoutNullEntries(Map<String, String> map) {
        Map<String, String> kept = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key != null && value != null) {
                kept.put(key, value);
            }
        }
        if (kept.isEmpty()) {
            return null;
        }
        return Map.copyOf(kept);
    }

    /**
     * Returns {@code objects} as-is when every element is already null or a
     * {@link SafeObjectTransformer} type. Otherwise a copy with unsafe values
     * reduced via {@link SafeObjectTransformer#asAllowedObject(Object)}.
     * {@link SafeObjectCodec} still applies the same reduction on write.
     */
    static Object[] safeObjectArray(Object[] objects) {
        if (objects == null) {
            return null;
        }
        int n = objects.length;
        for (int i = 0; i < n; i++) {
            Object obj = objects[i];
            if (obj != null && !SafeObjectTransformer.isAllowedType(obj)) {
                Object[] copy = new Object[n];
                System.arraycopy(objects, 0, copy, 0, i);
                copy[i] = SafeObjectTransformer.asAllowedObject(obj);
                for (int j = i + 1; j < n; j++) {
                    copy[j] = safeObject(objects[j]);
                }
                return copy;
            }
        }
        return objects;
    }

    static Object safeObject(Object o) {
        if (o == null)
            return null;

        return SafeObjectTransformer.asAllowedObject(o);

    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    public Level getLevel() {
        return Level.toLevel(levelInt);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Object[] getArgumentArray() {
        return argumentArray;
    }

    @Override
    public String getFormattedMessage() {
        String formattedMessage;
        if (argumentArray != null) {
            if (throwableProxyVO == null) {
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


    @Override
    public String getLoggerName() {
        return loggerName;
    }

    @Override
    public LoggerContextVO getLoggerContextVO() {
        return loggerContextVO;
    }

    @Override
    public IThrowableProxy getThrowableProxy() {
        return throwableProxyVO;
    }

    @Override
    public StackTraceElement[] getCallerData() {
        return callerData;
    }

    @Override
    public boolean hasCallerData() {
        return callerData != null;
    }

    @Override
    public List<Marker> getMarkerList() {
        return markerList;
    }

    @Override
    public Map<String, String> getMDCPropertyMap() {
        return mdcPropertyMap;
    }

    @Override
    public Map<String, String> getMdc() {
        return Map.of();
    }

    @Override
    public long getTimeStamp() {
        return timestamp;
    }

    @Override
    public int getNanoseconds() {
        return nanoseconds;
    }

    @Override
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public List<KeyValuePair> getKeyValuePairs() {
        return keyValuePairList;
    }

    @Override
    public void prepareForDeferredProcessing() {
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LoggingEventKryoVO that = (LoggingEventKryoVO) o;
        return levelInt == that.levelInt && timestamp == that.timestamp
                && nanoseconds == that.nanoseconds && sequenceNumber == that.sequenceNumber
                && loggerContextVO.equals(that.loggerContextVO)
                && Objects.equals(threadName, that.threadName)
                && Objects.equals(loggerName, that.loggerName)
                && Objects.equals(message, that.message) && Objects.deepEquals(argumentArray, that.argumentArray)
                && Objects.equals(markerList, that.markerList)
                && Objects.equals(keyValuePairList, that.keyValuePairList)
                && Objects.equals(mdcPropertyMap, that.mdcPropertyMap)
                && Objects.equals(throwableProxyVO, that.throwableProxyVO)
                && Arrays.equals(callerData, that.callerData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, nanoseconds, loggerContextVO, threadName, loggerName, levelInt, message, Arrays.hashCode(argumentArray), markerList, keyValuePairList, mdcPropertyMap, Arrays.hashCode(callerData));
    }

    @Override
    public String toString() {
        return "LoggingEventKryoVO{" +
                "threadName='" + threadName + '\'' +
                ", loggerName='" + loggerName + '\'' +
                ", level=" + getLevel() +
                ", message='" + message + '\'' +
                ", argumentArray=" + Arrays.toString(argumentArray) +
                ", markerList=" + markerList +
                ", keyValuePairList=" + keyValuePairList +
                ", mdcPropertyMap=" + mdcPropertyMap +
                ", timestamp=" + timestamp +
                ", nanoseconds=" + nanoseconds +
                ", sequenceNumber=" + sequenceNumber +
                ", throwableProxyVO=" + throwableProxyVO +
                ", callerData=" + Arrays.toString(callerData) +
                ", loggerContextVO='" + loggerContextVO + '}';
    }

    /**
     * A builder to create {@link LoggingEventVO}.
     *
     * @since 1.5.22
     */
    public static class Builder {

        LoggingEventKryoVO loggingEventKryoVO = new LoggingEventKryoVO();

        public LoggingEventKryoVO.Builder withThreadName(String threadName) {
            loggingEventKryoVO.threadName = threadName;
            return this;
        }

        public LoggingEventKryoVO.Builder withLoggerName(String loggerName) {
            loggingEventKryoVO.loggerName = loggerName;
            return this;
        }

        public LoggingEventKryoVO.Builder withLoggerContextVO(LoggerContextVO loggerContextVO) {
            loggingEventKryoVO.loggerContextVO = loggerContextVO;
            return this;
        }

        public LoggingEventKryoVO.Builder withLevelInt(int levelInt) {
            loggingEventKryoVO.levelInt = levelInt;
            return this;
        }

        public LoggingEventKryoVO.Builder withMessage(String message) {
            loggingEventKryoVO.message = message;
            return this;
        }

        public LoggingEventKryoVO.Builder withArgumentArray(Object[] argumentArray) {
            loggingEventKryoVO.argumentArray = argumentArray;
            return this;
        }

        public LoggingEventKryoVO.Builder withThrowableProxy(ThrowableProxyVO throwableProxy) {
            loggingEventKryoVO.throwableProxyVO = throwableProxy;
            return this;
        }

        public LoggingEventKryoVO.Builder withMarkerList(List<Marker> markerList) {
            loggingEventKryoVO.markerList = markerList;
            return this;
        }

        public LoggingEventKryoVO.Builder withKeyValuePairList(List<KeyValuePair> keyValuePairList) {
            loggingEventKryoVO.keyValuePairList = keyValuePairList;
            return this;
        }

        public LoggingEventKryoVO.Builder withMdcPropertyMap(Map<String, String> mdcPropertyMap) {
            loggingEventKryoVO.mdcPropertyMap = mdcPropertyMap;
            return this;
        }

        public LoggingEventKryoVO.Builder withTimestamp(long timestamp) {
            loggingEventKryoVO.timestamp = timestamp;
            return this;
        }

        public LoggingEventKryoVO.Builder withNanoseconds(int nanoseconds) {
            loggingEventKryoVO.nanoseconds = nanoseconds;
            return this;
        }

        public LoggingEventKryoVO.Builder withSequenceNumber(long sequenceNumber) {
            loggingEventKryoVO.sequenceNumber = sequenceNumber;
            return this;
        }

        public LoggingEventKryoVO.Builder withCallerData(StackTraceElement[] callerData) {
            loggingEventKryoVO.callerData = callerData;
            return this;
        }

        public LoggingEventKryoVO build() {
            return loggingEventKryoVO;
        }
    }

}
