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
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

import java.util.Map;

/**
 * Serializer for {@link LoggerContextVO} used by the vanilla stream format.
 *
 * <p>Relies only on the public API of {@link LoggerContextVO}. Kryo's reflection based
 * FieldSerializer would require module ch.qos.logback.classic to open package
 * ch.qos.logback.classic.spi.
 */
public class VanillaLoggerContextVOSerializer extends Serializer<LoggerContextVO> {

    @Override
    public void write(Kryo kryo, Output output, LoggerContextVO loggerContextVO) {
        output.writeString(loggerContextVO.getName());
        output.writeVarLong(loggerContextVO.getBirthTime(), false);
        kryo.writeClassAndObject(output, loggerContextVO.getPropertyMap());
    }

    @Override
    @SuppressWarnings("unchecked")
    public LoggerContextVO read(Kryo kryo, Input input, Class<? extends LoggerContextVO> type) {
        String name = input.readString();
        long birthTime = input.readVarLong(false);
        Map<String, String> propertyMap = (Map<String, String>) kryo.readClassAndObject(input);
        return new LoggerContextVO(name, propertyMap, birthTime);
    }
}
