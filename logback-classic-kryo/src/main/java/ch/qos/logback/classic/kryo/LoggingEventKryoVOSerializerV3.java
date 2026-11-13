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
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.CollectionSerializer;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stream version 3 event serializer.
 * <ul>
 *   <li>{@link ClassNameSerializer} for hierarchical logger-name compression</li>
 *   <li>{@link MDCPropertyMapSerializer} for MDC map compression</li>
 *   <li>{@link ArgumentArraySerializerV3} for message args (tag + payload after
 *       {@link SafeObjectTransformer})</li>
 *   <li>{@link KeyValuePairCollectionSerializerV3} / {@link KeyValuePairSerializerV3}
 *       for typed KVP values (tag + payload after {@link SafeObjectTransformer})</li>
 *   <li>{@link ThrowableProxyVOSerializer} / {@link StackTraceElementProxySerializer}
 *       for throwable and stack-frame compression</li>
 *   <li>Delta-encoded timestamps via {@link LongDeltaSerializer}</li>
 *   <li>{@code callerData} via {@code writeObjectOrNull} (no presence mask)</li>
 * </ul>
 *
 * <p>Nested codecs are created via {@code newXxx()} factory hooks so later stream versions
 * (e.g. {@link LoggingEventKryoVOSerializerV4}) can subclass and replace only the serializers
 * that change wire format, while reusing this write/read field order.
 */
public class LoggingEventKryoVOSerializerV3 extends Serializer<LoggingEventKryoVO> implements FieldByteAccumulatorAware {

    protected final FieldByteAccumulator fieldByteAccumulator;

    protected final LoggerContextVOSerializer loggerContextVOSerializer;
    protected final SerializerBase<String> classNameSerializer;
    protected final Serializer<String> threadNameSerializer;
    protected final SerializerBase<String> messageSerializer;
    protected final MDCPropertyMapSerializer mdcPropertyMapSerializer;
    protected final Serializer<ThrowableProxyVO>  throwableProxyVOSerializer;
    protected final LongDeltaSerializer timestampDeltaSerializer;
    protected final LongDeltaSerializer sequenceNumberDeltaSerializer;
    protected final ArgumentArraySerializerV3 argumentArraySerializerV3;
    protected final CollectionSerializer<ArrayList<KeyValuePair>>  kvpCollectionSerializer;
    protected final Serializer<StackTraceElement> stackTraceElementSerializer;

    public LoggingEventKryoVOSerializerV3() {
        this.fieldByteAccumulator = newFieldByteAccumulator();
        this.loggerContextVOSerializer = newLoggerContextVOSerializer();
        this.classNameSerializer = newClassNameSerializer();
        this.threadNameSerializer = newThreadNameSerializer();
        this.messageSerializer = newMessageSerializer();
        this.mdcPropertyMapSerializer = newMdcPropertyMapSerializer();
        this.throwableProxyVOSerializer = newThrowableProxyVOSerializer();
        this.timestampDeltaSerializer = newTimestampDeltaSerializer();
        this.sequenceNumberDeltaSerializer = newSequenceNumberDeltaSerializer();
        this.argumentArraySerializerV3 = newArgumentArraySerializer();
        this.kvpCollectionSerializer = newKeyValuePairCollectionSerializer();
        this.stackTraceElementSerializer = newStackTraceElementSerializer();
    }

    // -------------------------------------------------------------------------
    // Factory hooks — override in subclasses (e.g. V4) to swap individual codecs
    // -------------------------------------------------------------------------

    protected FieldByteAccumulator newFieldByteAccumulator() {
        return new FieldByteAccumulator();
    }

    protected LoggerContextVOSerializer newLoggerContextVOSerializer() {
        return new LoggerContextVOSerializer(1024);
    }

    /**
     * Logger-name codec. V3 and V4 use {@link ClassNameSerializer}.
     */
    protected SerializerBase<String> newClassNameSerializer() {
        return new ClassNameSerializer(2048);
    }

    protected Serializer<String> newThreadNameSerializer() {
        return new SingleItemCompressingStringSerializer(1024);
    }

    /**
     * Message string codec. V3 uses a direct-map {@link SingleItemCompressingStringSerializer}.
     */
    protected SerializerBase<String> newMessageSerializer() {
        return new SingleItemCompressingStringSerializer(1024);
    }

    protected MDCPropertyMapSerializer newMdcPropertyMapSerializer() {
        return new MDCPropertyMapSerializer(1024);
    }

    protected Serializer<ThrowableProxyVO>  newThrowableProxyVOSerializer() {
        return new ThrowableProxyVOSerializer(1024);
    }

    protected LongDeltaSerializer newTimestampDeltaSerializer() {
        return new LongDeltaSerializer();
    }

    protected LongDeltaSerializer newSequenceNumberDeltaSerializer() {
        return new LongDeltaSerializer();
    }

    protected ArgumentArraySerializerV3 newArgumentArraySerializer() {
        return new ArgumentArraySerializerV3(1024);
    }

    protected CollectionSerializer<ArrayList<KeyValuePair>> newKeyValuePairCollectionSerializer() {
        return new KeyValuePairCollectionSerializerV3(1024);
    }

    protected Serializer<StackTraceElement> newStackTraceElementSerializer() {
        return new StackTraceElementSerializer();
    }

    // -------------------------------------------------------------------------
    // Wire layout (field order) — shared by subclasses that only swap codecs
    // -------------------------------------------------------------------------

    @Override
    public void write(Kryo kryo, Output output, LoggingEventKryoVO event) {
        LoggerContextVO loggerContextVO = event.getLoggerContextVO();
        kryo.writeObject(output, loggerContextVO, loggerContextVOSerializer);
        kryo.writeObject(output, event.getLoggerName(), classNameSerializer);
        output.writeByte(LevelSerializer.levelIntToByte(event.levelInt));
        kryo.writeObject(output, event.getThreadName(), threadNameSerializer);
        kryo.writeObjectOrNull(output, event.getMessage(), messageSerializer);
        kryo.writeObjectOrNull(output, event.getArgumentArray(), argumentArraySerializerV3);
        kryo.writeObjectOrNull(output, event.getMarkerList(), ArrayList.class);

        List<KeyValuePair> keyValuePairs = event.getKeyValuePairs();

        int oldKVPPosition = output.position();

        kryo.writeObjectOrNull(output, event.getKeyValuePairs(), kvpCollectionSerializer);
//        if(keyValuePairs == null) {
//            int newKVPPosition = output.position();
//            System.out.println(this.getClass() +" null keyValuePairs, " + "written bytes: " + (newKVPPosition - oldKVPPosition));
//        }


        kryo.writeObjectOrNull(output, event.getMDCPropertyMap(), mdcPropertyMapSerializer);

        timestampDeltaSerializer.writeLong(output, event.getTimeStamp());
        output.writeInt(event.getNanoseconds());
        sequenceNumberDeltaSerializer.writeLong(output, event.getSequenceNumber());
        kryo.writeObjectOrNull(output, event.getThrowableProxy(), throwableProxyVOSerializer);
        writeCallerData(kryo, output, event.getCallerData());
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
        String message = fieldByteAccumulator.read(input, "message",
                () -> kryo.readObjectOrNull(input, String.class, messageSerializer));
        Object[] argArray = fieldByteAccumulator.read(input, "argumentArray",
                () -> kryo.readObjectOrNull(input, Object[].class, argumentArraySerializerV3));
        List<Marker> markerList = fieldByteAccumulator.read(input, "markers",
                () -> kryo.readObjectOrNull(input, ArrayList.class));
        List<KeyValuePair> keyValuePairs = fieldByteAccumulator.read(input, "keyValuePairs",
                () -> kryo.readObjectOrNull(input, ArrayList.class, kvpCollectionSerializer));
        Map<String, String> mdcPropertyMap = fieldByteAccumulator.read(input, "mdc",
                () -> kryo.readObjectOrNull(input, Map.class, mdcPropertyMapSerializer));

        long timestamp = fieldByteAccumulator.readLong(input, "timestamp",
                () -> timestampDeltaSerializer.readLong(input));
        int nanoseconds = fieldByteAccumulator.read(input, "nanoseconds",
                () -> input.readInt());
        long sequenceNumber = fieldByteAccumulator.readLong(input, "sequenceNumber",
                () -> sequenceNumberDeltaSerializer.readLong(input));
        ThrowableProxyVO throwableProxy = fieldByteAccumulator.read(input, "throwable",
                () -> kryo.readObjectOrNull(input, ThrowableProxyVO.class, throwableProxyVOSerializer));
        StackTraceElement[] callerData = fieldByteAccumulator.read(input, "callerData",
                () -> readCallerData(kryo, input));

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

    /**
     * Null {@code callerData} is one {@code writeObjectOrNull}-style flag byte.
     * Present arrays are that flag, a length, and {@link StackTraceElement} frames.
     */
    protected void writeCallerData(Kryo kryo, Output output, StackTraceElement[] callerData) {
        if (callerData == null) {
            output.writeByte(0);
            return;
        }
        output.writeByte(1);
        output.writeVarInt(callerData.length, true);
        for (StackTraceElement ste : callerData) {
            kryo.writeObject(output, ste, stackTraceElementSerializer);
        }
    }

    protected StackTraceElement[] readCallerData(Kryo kryo, Input input) {
        if (input.readByte() == 0) {
            return null;
        }
        int length = input.readVarInt(true);
        StackTraceElement[] callerData = new StackTraceElement[length];
        for (int i = 0; i < length; i++) {
            callerData[i] = kryo.readObject(input, StackTraceElement.class, stackTraceElementSerializer);
        }
        return callerData;
    }

    @Override
    public FieldByteAccumulator getFieldByteAccumulator() {
        return fieldByteAccumulator;
    }
}
