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
 * Stream version 1 Custom event serializer.
 * Matches existing corpus streams (e.g. translator/*-custom.kryo):
 * {@link SingleItemCompressingStringSerializer} for logger names and absolute timestamps.
 *
 * @see LoggingEventKryoVOSerializerV2
 */
public class LoggingEventKryoVOSerializerV1 extends Serializer<LoggingEventKryoVO> {

    LoggerContextVOSerializer loggerContextVOSerializer = new LoggerContextVOSerializer(16);

    SingleItemCompressingStringSerializer loggerNameSerializer = new SingleItemCompressingStringSerializer(1024);
    SingleItemCompressingStringSerializer threadNameSerializer = new SingleItemCompressingStringSerializer(1024);
    SingleItemCompressingStringSerializer messageSerializer = new SingleItemCompressingStringSerializer(SerializerBase.BiMapType.FREQUENCY_GATED, 1024);


    @Override
    public void write(Kryo kryo, Output output, LoggingEventKryoVO event) {
        LoggerContextVO loggerContextVO = event.getLoggerContextVO();
        kryo.writeObject(output, loggerContextVO, loggerContextVOSerializer);
        kryo.writeObject(output, event.getLoggerName(), loggerNameSerializer);
        output.writeByte(LevelSerializer.levelIntToByte(event.levelInt));
        kryo.writeObject(output, event.getThreadName(), threadNameSerializer);
        kryo.writeObjectOrNull(output, event.getMessage(), messageSerializer);
        kryo.writeObjectOrNull(output, event.getArgumentArray(), Object[].class);
        kryo.writeObjectOrNull(output, event.getMarkerList(), ArrayList.class);
        kryo.writeObjectOrNull(output, event.getKeyValuePairs(), ArrayList.class);
        kryo.writeObjectOrNull(output, event.getMDCPropertyMap(), HashMap.class);

        output.writeLong(event.getTimeStamp());
        output.writeInt(event.getNanoseconds());
        output.writeLong(event.getSequenceNumber());
        kryo.writeObjectOrNull(output, event.getThrowableProxy(), ThrowableProxyVO.class);

    }

    @Override
    public LoggingEventKryoVO read(Kryo kryo, Input input, Class<? extends LoggingEventKryoVO> type) {

        LoggerContextVO loggerContextVO = kryo.readObject(input, LoggerContextVO.class, loggerContextVOSerializer);
        String loggerName = kryo.readObject(input, String.class, loggerNameSerializer);
        int levelInt = LevelSerializer.byteToLevelInt(input.readByte());
        String threadName = kryo.readObject(input, String.class, threadNameSerializer);
        String message = kryo.readObjectOrNull(input, String.class, messageSerializer);
        Object[] argArray = kryo.readObjectOrNull(input, Object[].class);
        List<Marker> markerList = kryo.readObjectOrNull(input, ArrayList.class);
        List<KeyValuePair> keyValuePairs = kryo.readObjectOrNull(input, ArrayList.class);
        Map<String, String> mdcPropertyMap = kryo.readObjectOrNull(input, HashMap.class);
        long timestamp = input.readLong();
        long nanoseconds = input.readInt();
        long sequenceNumber = input.readLong();
        ThrowableProxyVO throwableProxy = kryo.readObjectOrNull(input, ThrowableProxyVO.class);

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
        eventBuilder.withNanoseconds((int) nanoseconds);
        eventBuilder.withSequenceNumber(sequenceNumber);
        eventBuilder.withThrowableProxy(throwableProxy);
        return eventBuilder.build();

    }

}
