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
import ch.qos.logback.core.kryo.SerializerBase;
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.minlog.Log;

/**
 * Caching serializer for {@link StackTraceElementProxy}.
 *
 * <p>Stack frames are the main source of redundancy in throwable payloads: the same frames
 * reappear across exceptions and log events. This serializer:
 * <ul>
 *   <li>Caches whole frames via {@link BiMapType#DIRECT_MAP} (frame equality is well-defined)</li>
 *   <li>Compresses class names with {@link ClassNameSerializer} by default (V5 passes a
 *       {@link BasicStringSerializer})</li>
 *   <li>Caches method and file names with {@link SingleItemCompressingStringSerializer}</li>
 * </ul>
 */
public class StackTraceElementProxySerializerV5 extends SerializerBase<StackTraceElementProxy> {

    private static final int DEFAULT_CAPACITY = 4096;

    private final SerializerBase<String> classNameSerializer;
    private final SingleItemCompressingStringSerializer methodNameSerializer;
    private final SingleItemCompressingStringSerializer fileNameSerializer;

    public StackTraceElementProxySerializerV5() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Class names use {@link ClassNameSerializer}.
     */
    public StackTraceElementProxySerializerV5(int capacity) {
        this(capacity, new ClassNameSerializer(capacity));
    }

    /**
     * @param classNameSerializer class-name codec; V5 passes a {@link BasicStringSerializer}
     */
    public StackTraceElementProxySerializerV5(int capacity, SerializerBase<String> classNameSerializer) {
        super(BiMapType.DIRECT_MAP, capacity);
        setAcceptsNull(false);
        this.classNameSerializer = classNameSerializer;
        this.methodNameSerializer = new SingleItemCompressingStringSerializer(capacity);
        // acceptsNull stays false: Kryo writeObjectOrNull handles null file names via
        // NULL/NOT_NULL flags. StringSerializer rejects null values.
        this.fileNameSerializer = new SingleItemCompressingStringSerializer(capacity);
    }

    @Override
    protected void writeNewEntry(Kryo kryo, Output output, StackTraceElementProxy step) {
        StackTraceElement ste = step.getStackTraceElement();
        Log.trace("writeNewEntry for StackTraceElementProxy: " + ste);

        // Rarely set; store raw (null-friendly) strings
        output.writeString(ste.getClassLoaderName());
        output.writeString(ste.getModuleName());
        output.writeString(ste.getModuleVersion());

        // High-redundancy string fields
        kryo.writeObject(output, ste.getClassName(), classNameSerializer);
        kryo.writeObject(output, ste.getMethodName(), methodNameSerializer);
        kryo.writeObjectOrNull(output, ste.getFileName(), fileNameSerializer);

        // Line numbers can be negative (-1 unknown, -2 native)
        output.writeVarInt(ste.getLineNumber(), false);
    }

    @Override
    protected StackTraceElementProxy readNewEntry(Kryo kryo, Input input) {
        String classLoaderName = input.readString();
        String moduleName = input.readString();
        String moduleVersion = input.readString();
        String className = kryo.readObject(input, String.class, classNameSerializer);
        String methodName = kryo.readObject(input, String.class, methodNameSerializer);
        String fileName = kryo.readObjectOrNull(input, String.class, fileNameSerializer);
        int lineNumber = input.readVarInt(false);

        StackTraceElement ste = new StackTraceElement(classLoaderName, moduleName, moduleVersion,
                className, methodName, fileName, lineNumber);
        Log.trace("readNewEntry for StackTraceElementProxy: " + ste);
        return new StackTraceElementProxy(ste);
    }
}
