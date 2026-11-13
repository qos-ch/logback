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
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stream version 2 event serializer (from commit b888265).
 * <ul>
 *   <li>{@link ClassNameSerializer} for hierarchical logger-name compression</li>
 *   <li>Delta-encoded timestamps via {@code writeVarLong}/{@code readVarLong}</li>
 * </ul>
 */
public class LoggingEventKryoVOSerializerV2 extends Serializer<LoggingEventKryoVO> {

    public static final FieldByteAccumulator fieldByteAccumulator = new FieldByteAccumulator();

    LoggerContextVOSerializer loggerContextVOSerializer = new LoggerContextVOSerializer(16);

    ClassNameSerializer classNameSerializer = new ClassNameSerializer(2048);
    SingleItemCompressingStringSerializer threadNameSerializer = new SingleItemCompressingStringSerializer(1024);
    SingleItemCompressingStringSerializer messageSerializer = new SingleItemCompressingStringSerializer(SerializerBase.BiMapType.FREQUENCY_GATED, 1024);

    private long lastTimestamp = 0;
    private long lastReadTimestamp = 0;


    @Override
    public void write(Kryo kryo, Output output, LoggingEventKryoVO event) {
        LoggerContextVO loggerContextVO = event.getLoggerContextVO();
        kryo.writeObject(output, loggerContextVO, loggerContextVOSerializer);
        kryo.writeObject(output, event.getLoggerName(), classNameSerializer);
        output.writeByte(LevelSerializer.levelIntToByte(event.levelInt));
        kryo.writeObject(output, event.getThreadName(), threadNameSerializer);
        kryo.writeObjectOrNull(output, event.getMessage(), messageSerializer);
        kryo.writeObjectOrNull(output, event.getArgumentArray(), Object[].class);
        kryo.writeObjectOrNull(output, event.getMarkerList(), ArrayList.class);
        kryo.writeObjectOrNull(output, event.getKeyValuePairs(), ArrayList.class);
        kryo.writeObjectOrNull(output, event.getMDCPropertyMap(), HashMap.class);

        long currentTimestamp = event.getTimeStamp();
        long deltaTimestamp = currentTimestamp - lastTimestamp;
        lastTimestamp = currentTimestamp;
        output.writeVarLong(deltaTimestamp, true);
        output.writeInt(event.getNanoseconds());
        output.writeLong(event.getSequenceNumber());
        kryo.writeObjectOrNull(output, event.getThrowableProxy(), ThrowableProxyVO.class);

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
                () -> kryo.readObjectOrNull(input, Object[].class));
        List<Marker> markerList = fieldByteAccumulator.read(input, "markers",
                () -> kryo.readObjectOrNull(input, ArrayList.class));
        List<KeyValuePair> keyValuePairs = fieldByteAccumulator.read(input, "keyValuePairs",
                () -> kryo.readObjectOrNull(input, ArrayList.class));
        Map<String, String> mdcPropertyMap = fieldByteAccumulator.read(input, "mdc",
                () -> kryo.readObjectOrNull(input, HashMap.class));

        System.out.println("LoggingEventKryoVOSerializerV2.readmdcPropertyMap: " + mdcPropertyMap);

        long deltaTimestamp = fieldByteAccumulator.read(input, "timestampDelta",
                () -> input.readVarLong(true));
        long timestamp = lastReadTimestamp + deltaTimestamp;
        lastReadTimestamp = timestamp;
        int nanoseconds = fieldByteAccumulator.read(input, "nanoseconds",
                () -> input.readInt());
        long sequenceNumber = fieldByteAccumulator.read(input, "sequenceNumber",
                () -> input.readLong());
        ThrowableProxyVO throwableProxy = fieldByteAccumulator.read(input, "throwable",
                () -> kryo.readObjectOrNull(input, ThrowableProxyVO.class));

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
        return eventBuilder.build();

    }

}
