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
import ch.qos.logback.core.kryo.SerializerBase;
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.minlog.Log;

import java.util.HashMap;

public class LoggerContextVOSerializer extends SerializerBase<LoggerContextVO> {


    public LoggerContextVOSerializer(int capacity) {
        super(BiMapType.DIRECT_MAP, capacity);
        setAcceptsNull(false);
    }

    @Override
    protected void writeNewEntry(Kryo kryo, Output output, LoggerContextVO loggerContextVO) {
        //Log.info("writeNewEntry for LoggerContextVO: " + loggerContextVO);
        kryo.writeObject(output, loggerContextVO.getName());
        output.writeLong(loggerContextVO.getBirthTime());
        kryo.writeObject(output, loggerContextVO.getPropertyMap());
    }

    @Override
    protected LoggerContextVO readNewEntry(Kryo kryo, Input input) {
        //Log.info("readNewEntry for LoggerContextVO: ");
        String name = kryo.readObject(input, String.class);
        long birthTime = input.readLong();
        HashMap<?, ?> map = kryo.readObject(input, java.util.HashMap.class);
        LoggerContextVO deserialized = new LoggerContextVO(name, (HashMap<String, String>) map, birthTime);
        //Log.info("readNewEntry for LoggerContextVO named " + name);
        return deserialized;
    }

}
