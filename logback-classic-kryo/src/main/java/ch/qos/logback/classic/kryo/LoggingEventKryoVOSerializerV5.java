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

import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.ThrowableProxyVO;
import ch.qos.logback.core.kryo.SerializerBase;
import ch.qos.logback.core.kryo.SerializerBase.BiMapType;
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stream version 5 event serializer.
 *
 * <p>Same nested codecs as {@link LoggingEventKryoVOSerializerV4}, except MDC values
 * use {@link MDCPropertyMapSerializerV5} so they share {@link #longUniqueStrings()} with
 * message, string arguments, and KVP string values (MDC keys stay on
 * {@link BasicStringSerializer}). After {@code threadName}, a 1-byte presence bitmap
 * replaces the six {@code writeObjectOrNull} flags used by V3/V4 for message, arguments,
 * markers, key-value pairs, MDC, and throwable, and adds a seventh bit for
 * {@code callerData} (usually absent). Message may be null (fluent {@code .log()}
 * with no message). Present fields are written with {@code writeObject} (no per-field
 * null byte). Timestamp and nanoseconds are one {@link LongDeltaSerializer} field:
 * {@code (epochSecond << 30) | nanos} (E3). Caller data follows throwable when present.
 *
 * <p>Logger names and stack-frame class names (caller data and throwable frames) use
 * {@link BasicStringSerializer} instead of {@link ClassNameSerializer}. Each of the three
 * has its own instance.
 *
 * <p>Selected when the stream header version is
 * {@link KryoClassicConstants#STREAM_VERSION_5}.
 */
public class LoggingEventKryoVOSerializerV5 extends LoggingEventKryoVOSerializerV4 {

    static final int PRESENT_MESSAGE = 1 << 0;
    static final int PRESENT_ARGUMENTS = 1 << 1;
    static final int PRESENT_MARKERS = 1 << 2;
    static final int PRESENT_KVP = 1 << 3;
    static final int PRESENT_MDC = 1 << 4;
    static final int PRESENT_THROWABLE = 1 << 5;
    static final int PRESENT_CALLER_DATA = 1 << 6;

    /** Bits of {@code Instant.getNano()} in the packed epoch field. {@code 2^30 > 10^9-1}. */
    static final int NANO_BITS = 30;
    static final int NANO_MASK = 0x3FFFFFFF;

    @Override
    protected MDCPropertyMapSerializer newMdcPropertyMapSerializer() {
        return new MDCPropertyMapSerializerV5(1024, longUniqueStrings());
    }

    /**
     * Logger names are cached whole; no package-prefix table.
     */
    @Override
    protected SerializerBase<String> newClassNameSerializer() {
        return new BasicStringSerializer(BiMapType.DIRECT_MAP, 2048);
    }

    /**
     * Caller-data frames. Class names have their own {@link BasicStringSerializer},
     * separate from logger names and throwable frames.
     */
    @Override
    protected Serializer<StackTraceElement> newStackTraceElementSerializer() {
        return new StackTraceElementSerializer(1024, new BasicStringSerializer(BiMapType.DIRECT_MAP, 1024));
    }

    /**
     * Same as V4, except frame class names use their own {@link BasicStringSerializer}.
     */
    @Override
    protected Serializer<ThrowableProxyVO> newThrowableProxyVOSerializer() {
        return new ThrowableProxyVOSerializerV4(1024,
                new StackTraceElementProxySerializerV3(1024, new BasicStringSerializer(BiMapType.DIRECT_MAP, 1024)));
    }

    @Override
    public void write(Kryo kryo, Output output, LoggingEventKryoVO event) {
        LoggerContextVO loggerContextVO = event.getLoggerContextVO();
        kryo.writeObject(output, loggerContextVO, loggerContextVOSerializer);
        kryo.writeObject(output, event.getLoggerName(), classNameSerializer);
        output.writeByte(LevelSerializer.levelIntToByte(event.levelInt));
        kryo.writeObject(output, event.getThreadName(), threadNameSerializer);

        String message = event.getMessage();
        Object[] argumentArray = event.getArgumentArray();
        List<Marker> markerList = event.getMarkerList();
        List<KeyValuePair> keyValuePairs = event.getKeyValuePairs();
        Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
        ThrowableProxyVO throwableProxy = (ThrowableProxyVO) event.getThrowableProxy();
        StackTraceElement[] callerData = event.hasCallerData() ? event.getCallerData() : null;

        int presence = 0;
        if (message != null) {
            presence |= PRESENT_MESSAGE;
        }
        if (argumentArray != null) {
            presence |= PRESENT_ARGUMENTS;
        }
        if (markerList != null) {
            presence |= PRESENT_MARKERS;
        }
        if (keyValuePairs != null) {
            presence |= PRESENT_KVP;
        }
        if (mdcPropertyMap != null) {
            presence |= PRESENT_MDC;
        }
        if (throwableProxy != null) {
            presence |= PRESENT_THROWABLE;
        }
        if (callerData != null) {
            presence |= PRESENT_CALLER_DATA;
        }
        output.writeByte(presence);

        if ((presence & PRESENT_MESSAGE) != 0) {
            kryo.writeObject(output, message, messageSerializer);
        }
        if ((presence & PRESENT_ARGUMENTS) != 0) {
            kryo.writeObject(output, argumentArray, argumentArraySerializerV3);
        }
        if ((presence & PRESENT_MARKERS) != 0) {
            kryo.writeObject(output, markerList, kryo.getSerializer(ArrayList.class));
        }
        if ((presence & PRESENT_KVP) != 0) {
            kryo.writeObject(output, keyValuePairs, kvpCollectionSerializer);
        }
        if ((presence & PRESENT_MDC) != 0) {
            kryo.writeObject(output, mdcPropertyMap, mdcPropertyMapSerializer);
        }

        timestampDeltaSerializer.writeLong(output, packEpoch(event.getTimeStamp(), event.getNanoseconds()));
        sequenceNumberDeltaSerializer.writeLong(output, event.getSequenceNumber());

        if ((presence & PRESENT_THROWABLE) != 0) {
            kryo.writeObject(output, throwableProxy, throwableProxyVOSerializer);
        }
        if ((presence & PRESENT_CALLER_DATA) != 0) {
            writeCallerData(kryo, output, callerData);
        }
    }

    @Override
    public LoggingEventKryoVO read(Kryo kryo, Input input, Class<? extends LoggingEventKryoVO> type) {
        LoggerContextVO loggerContextVO = fieldByteAccumulator.read(input, "loggerContext",
                () -> kryo.readObject(input, LoggerContextVO.class, loggerContextVOSerializer));
        String loggerName = fieldByteAccumulator.read(input, "loggerName",
                () -> kryo.readObject(input, String.class, classNameSerializer));
        int levelInt = fieldByteAccumulator.read(input, "level",
                () -> LevelSerializer.byteToLevelInt(input.readByte()));
        String threadName = fieldByteAccumulator.read(input, "threadName",
                () -> kryo.readObject(input, String.class, threadNameSerializer));

        int presence = fieldByteAccumulator.read(input, "presence",
                () -> input.readByte() & 0xFF);

        String message = ((presence & PRESENT_MESSAGE) != 0)
                ? fieldByteAccumulator.read(input, "message",
                () -> kryo.readObject(input, String.class, messageSerializer))
                : null;
        Object[] argArray = ((presence & PRESENT_ARGUMENTS) != 0)
                ? fieldByteAccumulator.read(input, "argumentArray",
                () -> kryo.readObject(input, Object[].class, argumentArraySerializerV3))
                : null;
        List<Marker> markerList = ((presence & PRESENT_MARKERS) != 0)
                ? fieldByteAccumulator.read(input, "markers",
                () -> kryo.readObject(input, ArrayList.class))
                : null;
        List<KeyValuePair> keyValuePairs = ((presence & PRESENT_KVP) != 0)
                ? fieldByteAccumulator.read(input, "keyValuePairs",
                () -> kryo.readObject(input, ArrayList.class, kvpCollectionSerializer))
                : null;
        Map<String, String> mdcPropertyMap = ((presence & PRESENT_MDC) != 0)
                ? fieldByteAccumulator.read(input, "mdc",
                () -> kryo.readObject(input, Map.class, mdcPropertyMapSerializer))
                : null;

        long packed = fieldByteAccumulator.readLong(input, "timestamp",
                () -> timestampDeltaSerializer.readLong(input));
        long timestamp = unpackTimestamp(packed);
        int nanoseconds = unpackNanoseconds(packed);
        long sequenceNumber = fieldByteAccumulator.readLong(input, "sequenceNumber",
                () -> sequenceNumberDeltaSerializer.readLong(input));
        ThrowableProxyVO throwableProxy = ((presence & PRESENT_THROWABLE) != 0)
                ? fieldByteAccumulator.read(input, "throwable",
                () -> kryo.readObject(input, ThrowableProxyVO.class, throwableProxyVOSerializer))
                : null;
        StackTraceElement[] callerData = ((presence & PRESENT_CALLER_DATA) != 0)
                ? fieldByteAccumulator.read(input, "callerData",
                () -> readCallerData(kryo, input))
                : null;

        LoggingEventKryoVO.Builder eventBuilder = new LoggingEventKryoVO.Builder();
        eventBuilder.withLoggerContextVO(loggerContextVO);
        eventBuilder.withLoggerName(loggerName);
        eventBuilder.withLevelInt(levelInt);
        eventBuilder.withThreadName(threadName);
        eventBuilder.withMessage(message);
        eventBuilder.withArgumentArray(argArray);
        eventBuilder.withMarkerList(markerList);
        eventBuilder.withKeyValuePairList(keyValuePairs);
        eventBuilder.withMdcPropertyMap(mdcPropertyMap);
        eventBuilder.withTimestamp(timestamp);
        eventBuilder.withNanoseconds(nanoseconds);
        eventBuilder.withSequenceNumber(sequenceNumber);
        eventBuilder.withThrowableProxy(throwableProxy);
        eventBuilder.withCallerData(callerData);
        return eventBuilder.build();
    }

    public void writeCallerData(Kryo kryo, Output output, StackTraceElement[] callerData) {
        output.writeVarInt(callerData.length, true);
        for (StackTraceElement ste : callerData) {
            kryo.writeObject(output, ste, stackTraceElementSerializer);
        }
    }

    public StackTraceElement[] readCallerData(Kryo kryo, Input input) {
        int length = input.readVarInt(true);
        StackTraceElement[] callerData = new StackTraceElement[length];
        for (int i = 0; i < length; i++) {
            callerData[i] = kryo.readObject(input, StackTraceElement.class, stackTraceElementSerializer);
        }
        return callerData;
    }

    /**
     * Packs epoch-millisecond timestamp and nano-of-second. {@code timestamp / 1000}
     * is {@link java.time.Instant#getEpochSecond()} for non-negative timestamps
     * (these streams are post-1970).
     */
    static long packEpoch(long timestamp, int nanoseconds) {
        long epochSecond = timestamp / 1000;
        return (epochSecond << NANO_BITS) | (nanoseconds & NANO_MASK);
    }

    static long unpackTimestamp(long packed) {
        long epochSecond = packed >>> NANO_BITS;
        int nanoseconds = unpackNanoseconds(packed);
        return epochSecond * 1000 + nanoseconds / 1_000_000;
    }

    static int unpackNanoseconds(long packed) {
        return (int) (packed & NANO_MASK);
    }
}
