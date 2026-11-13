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

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyVO;
import ch.qos.logback.core.kryo.SerializerBase;
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.minlog.Log;

/**
 * Serializer for {@link ThrowableProxyVO}.
 *
 * <p>Does not cache whole proxies ({@link BiMapType#NONE}): {@code ThrowableProxyVO}
 * equality/hashCode omit the exception message, so object-level caching would merge
 * distinct throwables. Redundancy is removed at the component level instead:
 * <ul>
 *   <li>{@link ClassNameSerializer} for exception class names</li>
 *   <li>{@link SingleItemCompressingStringSerializer} for messages</li>
 *   <li>{@link StackTraceElementProxySerializer} for stack frames (main win)</li>
 * </ul>
 */
public class ThrowableProxyVOSerializer extends SerializerBase<ThrowableProxyVO> {

    private final ClassNameSerializer classNameSerializer;
    private final SingleItemCompressingStringSerializer messageSerializer;
    private final StackTraceElementProxySerializerV3 stackTraceElementProxySerializerV3;

    public ThrowableProxyVOSerializer(int capacity) {
        super(BiMapType.NONE, capacity);
        setAcceptsNull(false);
        this.classNameSerializer = new ClassNameSerializer(capacity);
        // acceptsNull stays false: Kryo writeObjectOrNull/readObjectOrNull emit NULL/NOT_NULL
        // flags. StringSerializer must not receive null (it rejects them).
        this.messageSerializer = new SingleItemCompressingStringSerializer(BiMapType.DIRECT_MAP, capacity);
        // Stack frames dominate size and repeat heavily across exceptions
        this.stackTraceElementProxySerializerV3 = new StackTraceElementProxySerializerV3(capacity);
    }

    /**
     * Shared stack-frame serializer so a single cache is used for all throwables
     * written by this instance.
     */
    public StackTraceElementProxySerializerV3 getStackTraceElementProxySerializer() {
        return stackTraceElementProxySerializerV3;
    }

    @Override
    protected void writeNewEntry(Kryo kryo, Output output, ThrowableProxyVO throwableProxyVO) {
        Log.trace("writeNewEntry for ThrowableProxyVO: " + throwableProxyVO.getClassName());

        kryo.writeObject(output, throwableProxyVO.getClassName(), classNameSerializer);
        kryo.writeObjectOrNull(output, throwableProxyVO.getOverridingMessage(), messageSerializer);
        kryo.writeObjectOrNull(output, throwableProxyVO.getMessage(), messageSerializer);
        output.writeVarInt(throwableProxyVO.getCommonFrames(), true);

        writeStackTrace(kryo, output, throwableProxyVO.getStackTraceElementProxyArray());

        IThrowableProxy cause = throwableProxyVO.getCause();
        if (cause == null) {
            output.writeBoolean(false);
        } else {
            output.writeBoolean(true);
            kryo.writeObject(output, asThrowableProxyVO(cause), this);
        }

        IThrowableProxy[] suppressed = throwableProxyVO.getSuppressed();
        if (suppressed == null) {
            output.writeVarInt(-1, true);
        } else {
            output.writeVarInt(suppressed.length, true);
            for (IThrowableProxy s : suppressed) {
                kryo.writeObject(output, asThrowableProxyVO(s), this);
            }
        }

        output.writeBoolean(throwableProxyVO.isCyclic());
    }

    @Override
    protected ThrowableProxyVO readNewEntry(Kryo kryo, Input input) {
        Log.trace("readNewEntry for ThrowableProxyVO");

        String className = kryo.readObject(input, String.class, classNameSerializer);
        String overridingMessage = kryo.readObjectOrNull(input, String.class, messageSerializer);
        String message = kryo.readObjectOrNull(input, String.class, messageSerializer);
        int commonFrames = input.readVarInt(true);

        StackTraceElementProxy[] stackTraceElementProxyArray = readStackTrace(kryo, input);

        ThrowableProxyVO cause = null;
        if (input.readBoolean()) {
            cause = kryo.readObject(input, ThrowableProxyVO.class, this);
        }

        int suppressedLength = input.readVarInt(true);
        IThrowableProxy[] suppressed = null;
        if (suppressedLength >= 0) {
            suppressed = new IThrowableProxy[suppressedLength];
            for (int i = 0; i < suppressedLength; i++) {
                suppressed[i] = kryo.readObject(input, ThrowableProxyVO.class, this);
            }
        }

        boolean cyclic = input.readBoolean();


        return ThrowableProxyVO.builder()
                .setClassName(className)
                .setOverridingMessage(overridingMessage)
                .setMessage(message)
                .setCommonFramesCount(commonFrames)
                .setStackTraceElementProxyArray(stackTraceElementProxyArray)
                .setCause(cause)
                .setSuppressed(suppressed)
                .setCyclic(cyclic)
                .build();
    }

    private void writeStackTrace(Kryo kryo, Output output, StackTraceElementProxy[] steps) {
        if (steps == null) {
            output.writeVarInt(-1, true);
            return;
        }
        output.writeVarInt(steps.length, true);
        for (StackTraceElementProxy step : steps) {
            kryo.writeObject(output, step, stackTraceElementProxySerializerV3);
        }
    }

    private StackTraceElementProxy[] readStackTrace(Kryo kryo, Input input) {
        int length = input.readVarInt(true);
        if (length < 0) {
            return null;
        }
        StackTraceElementProxy[] steps = new StackTraceElementProxy[length];
        for (int i = 0; i < length; i++) {
            steps[i] = kryo.readObject(input, StackTraceElementProxy.class, stackTraceElementProxySerializerV3);
        }
        return steps;
    }

    private static ThrowableProxyVO asThrowableProxyVO(IThrowableProxy proxy) {
        if (proxy instanceof ThrowableProxyVO) {
            return (ThrowableProxyVO) proxy;
        }
        return ThrowableProxyVO.build(proxy);
    }
}
