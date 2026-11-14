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

import ch.qos.logback.core.kryo.SerializerBase;
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

/**
 * Caller-data frames. The same {@link StackTraceElement} recurs across events,
 * so the whole frame is cached by equality. A repeat is a 2-byte id. The first
 * time a frame is seen, class, method, and file names go through their own
 * caches, and the line number is a signed varint.
 *
 * <p>This replaced the raw six-string layout. Streams written by that layout
 * no longer decode.
 */
public class StackTraceElementSerializer extends SerializerBase<StackTraceElement> {

    private static final int DEFAULT_CAPACITY = 1024;

    private final BasicStringSerializer moduleNameSerializer = new BasicStringSerializer(DEFAULT_CAPACITY);
    private final BasicStringSerializer moduleVersionSerializer = new BasicStringSerializer(DEFAULT_CAPACITY);
    private final SerializerBase<String> classNameSerializer;
    private final BasicStringSerializer methodNameSerializer;
    private final BasicStringSerializer fileNameSerializer;

    public StackTraceElementSerializer() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Class names use {@link ClassNameSerializer} (Vanilla streams).
     */
    public StackTraceElementSerializer(int capacity) {
        this(capacity, new ClassNameSerializer(capacity));
    }

    /**
     * @param classNameSerializer class-name codec; V5 passes a {@link BasicStringSerializer}
     */
    public StackTraceElementSerializer(int capacity, SerializerBase<String> classNameSerializer) {
        super(BiMapType.DIRECT_MAP, capacity);
        setAcceptsNull(false);
        this.classNameSerializer = classNameSerializer;
        this.methodNameSerializer = new BasicStringSerializer(capacity);
        // acceptsNull stays false: Kryo writeObjectOrNull handles a null file name.
        this.fileNameSerializer = new BasicStringSerializer(capacity);
    }

    @Override
    protected void writeNewEntry(Kryo kryo, Output output, StackTraceElement ste) {
        // Rarely set. writeString accepts null. Module name and version are
        // usually null outside the module path; writeObject would throw.
        output.writeString(ste.getClassLoaderName());
        kryo.writeObjectOrNull(output, ste.getModuleName(), moduleNameSerializer);
        kryo.writeObjectOrNull(output, ste.getModuleVersion(), moduleVersionSerializer);

        kryo.writeObject(output, ste.getClassName(), classNameSerializer);
        kryo.writeObject(output, ste.getMethodName(), methodNameSerializer);
        kryo.writeObjectOrNull(output, ste.getFileName(), fileNameSerializer);

        // Line numbers can be negative (-1 unknown, -2 native).
        output.writeVarInt(ste.getLineNumber(), false);
    }

    @Override
    protected StackTraceElement readNewEntry(Kryo kryo, Input input) {
        String classLoaderName = input.readString();
        String moduleName = kryo.readObjectOrNull(input, String.class, moduleNameSerializer);
        String moduleVersion = kryo.readObjectOrNull(input, String.class, moduleVersionSerializer);
        String className = kryo.readObject(input, String.class, classNameSerializer);
        String methodName = kryo.readObject(input, String.class, methodNameSerializer);
        String fileName = kryo.readObjectOrNull(input, String.class, fileNameSerializer);
        int lineNumber = input.readVarInt(false);
        return new StackTraceElement(classLoaderName, moduleName, moduleVersion,
                className, methodName, fileName, lineNumber);
    }
}
