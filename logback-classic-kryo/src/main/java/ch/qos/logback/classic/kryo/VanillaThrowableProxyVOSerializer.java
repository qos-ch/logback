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
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

/**
 * Serializer for {@link ThrowableProxyVO} used by the vanilla stream format.
 *
 * <p>Relies only on the public API of {@link ThrowableProxyVO}, i.e. its getters and
 * {@link ThrowableProxyVO#builder()}. Kryo's reflection based FieldSerializer would require
 * module ch.qos.logback.classic to open package ch.qos.logback.classic.spi.
 */
public class VanillaThrowableProxyVOSerializer extends Serializer<ThrowableProxyVO> {

    @Override
    public void write(Kryo kryo, Output output, ThrowableProxyVO throwableProxyVO) {
        output.writeString(throwableProxyVO.getClassName());
        output.writeString(throwableProxyVO.getOverridingMessage());
        output.writeString(throwableProxyVO.getMessage());
        output.writeVarInt(throwableProxyVO.getCommonFrames(), true);
        kryo.writeObjectOrNull(output, throwableProxyVO.getStackTraceElementProxyArray(), StackTraceElementProxy[].class);
        kryo.writeObjectOrNull(output, asThrowableProxyVO(throwableProxyVO.getCause()), ThrowableProxyVO.class);

        IThrowableProxy[] suppressed = throwableProxyVO.getSuppressed();
        if (suppressed == null) {
            output.writeVarInt(-1, true);
        } else {
            output.writeVarInt(suppressed.length, true);
            for (IThrowableProxy s : suppressed) {
                kryo.writeObjectOrNull(output, asThrowableProxyVO(s), ThrowableProxyVO.class);
            }
        }
        output.writeBoolean(throwableProxyVO.isCyclic());
    }

    @Override
    public ThrowableProxyVO read(Kryo kryo, Input input, Class<? extends ThrowableProxyVO> type) {
        String className = input.readString();
        String overridingMessage = input.readString();
        String message = input.readString();
        int commonFrames = input.readVarInt(true);
        StackTraceElementProxy[] steps = kryo.readObjectOrNull(input, StackTraceElementProxy[].class);
        ThrowableProxyVO cause = kryo.readObjectOrNull(input, ThrowableProxyVO.class);

        IThrowableProxy[] suppressed = null;
        int suppressedLength = input.readVarInt(true);
        if (suppressedLength >= 0) {
            suppressed = new IThrowableProxy[suppressedLength];
            for (int i = 0; i < suppressedLength; i++) {
                suppressed[i] = kryo.readObjectOrNull(input, ThrowableProxyVO.class);
            }
        }
        boolean cyclic = input.readBoolean();

        return ThrowableProxyVO.builder()
                .setClassName(className)
                .setOverridingMessage(overridingMessage)
                .setMessage(message)
                .setCommonFramesCount(commonFrames)
                .setStackTraceElementProxyArray(steps)
                .setCause(cause)
                .setSuppressed(suppressed)
                .setCyclic(cyclic)
                .build();
    }

    private static ThrowableProxyVO asThrowableProxyVO(IThrowableProxy proxy) {
        if (proxy == null || proxy instanceof ThrowableProxyVO) {
            return (ThrowableProxyVO) proxy;
        }
        return ThrowableProxyVO.build(proxy);
    }
}
