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

import ch.qos.logback.classic.spi.StackTraceElementProxy;
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

public class StackTraceElementProxySerializer extends Serializer<StackTraceElementProxy>  {


    @Override
    public void write(Kryo kryo, Output output, StackTraceElementProxy step) {
        kryo.writeObject(output, step.getStackTraceElement());
    }

    @Override
    public StackTraceElementProxy read(Kryo kryo, Input input, Class<? extends StackTraceElementProxy> type) {
       StackTraceElement ste = kryo.readObject(input, StackTraceElement.class);
       StackTraceElementProxy step = new StackTraceElementProxy(ste);
        return step;
    }
}
