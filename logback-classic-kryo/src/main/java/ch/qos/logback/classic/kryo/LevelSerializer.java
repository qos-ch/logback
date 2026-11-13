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
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

import static ch.qos.logback.classic.Level.*;

public class LevelSerializer extends Serializer<Level> {

    static final int ALL_INT_BYTE_VALUE = 0;
    static final int TRACE_INT_BYTE_VALUE = 1;
    static final int DEBUG_INT_BYTE_VALUE = 2;
    static final int INFO_INT_BYTE_VALUE = 3;
    static final int WARN_INT_BYTE_VALUE = 4;
    static final int ERROR_INT_BYTE_VALUE = 5;
    static final int OFF_INT_BYTE_VALUE = 6;


    static byte levelIntToByte(int levelInt) {
        switch (levelInt) {
            case ALL_INT:
                return ALL_INT_BYTE_VALUE;
            case TRACE_INT:
                return TRACE_INT_BYTE_VALUE;
            case DEBUG_INT:
                return DEBUG_INT_BYTE_VALUE;
            case INFO_INT:
                return INFO_INT_BYTE_VALUE;
            case WARN_INT:
                return WARN_INT_BYTE_VALUE;
            case ERROR_INT:
                return ERROR_INT_BYTE_VALUE;
            case OFF_INT:
                return OFF_INT_BYTE_VALUE;
            default:
                return -1; // Unknown level
        }
    }


    static byte levelToByte(Level level) {
        return levelIntToByte(level.levelInt);
    }

    static int byteToLevelInt(byte levelByte) {
        switch (levelByte) {
            case ALL_INT_BYTE_VALUE:
                return Level.ALL_INT;
            case TRACE_INT_BYTE_VALUE:
                return Level.TRACE_INT;
            case DEBUG_INT_BYTE_VALUE:
                return Level.DEBUG_INT;
            case INFO_INT_BYTE_VALUE:
                return Level.INFO_INT;
            case WARN_INT_BYTE_VALUE:
                return Level.WARN_INT;
            case ERROR_INT_BYTE_VALUE:
                return Level.ERROR_INT;
            case OFF_INT_BYTE_VALUE:
                return Level.OFF_INT;
            default:
                return -1; // Unknown level
        }
    }

    static Level levelFromByte(byte levelByte) {
        switch (levelByte) {
            case ALL_INT_BYTE_VALUE:
                return Level.ALL;
            case TRACE_INT_BYTE_VALUE:
                return Level.TRACE;
            case DEBUG_INT_BYTE_VALUE:
                return Level.DEBUG;
            case INFO_INT_BYTE_VALUE:
                return Level.INFO;
            case WARN_INT_BYTE_VALUE:
                return Level.WARN;
            case ERROR_INT_BYTE_VALUE:
                return Level.ERROR;
            case OFF_INT_BYTE_VALUE:
                return Level.OFF;
            default:
                return null; // Unknown level
        }
    }

    @Override
    public void write(Kryo kryo, Output output, Level level) {
        byte levelByte = (byte) level.toInt();
        output.writeByte(levelByte);
    }

    @Override
    public Level read(Kryo kryo, Input input, Class<? extends Level> type) {

        byte levelByte = input.readByte();
        Level level = levelFromByte(levelByte);
        if (level != null)
            return level;
        else
            return null;
    }
}
