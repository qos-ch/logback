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
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.minlog.Log;

public class StackTraceElementProxySerializerV4 extends SerializerBase<StackTraceElementProxy>  {

    private static final int DEFAULT_CAPACITY = 4096;

    private final BasicStringSerializer moduleNameSerializer = new BasicStringSerializer(100);
    private final BasicStringSerializer moduleVersionSerializer = new BasicStringSerializer(100);

    private final SerializerBase<String> classNameSerializer;
    private final BasicStringSerializer methodNameSerializer;
    private final BasicStringSerializer fileNameSerializer;

    public StackTraceElementProxySerializerV4() {
        this(DEFAULT_CAPACITY);
    }

    public StackTraceElementProxySerializerV4(int capacity) {
        super(BiMapType.DIRECT_MAP, capacity);
        setAcceptsNull(false);
        this.classNameSerializer = new ClassNameSerializer(capacity);
        this.methodNameSerializer = new BasicStringSerializer(capacity);
        // acceptsNull stays false: Kryo writeObjectOrNull handles null file names via
        // NULL/NOT_NULL flags. StringSerializer rejects null values.
        this.fileNameSerializer = new BasicStringSerializer(capacity);
    }

    @Override
    protected void writeNewEntry(Kryo kryo, Output output, StackTraceElementProxy step) {
        StackTraceElement ste = step.getStackTraceElement();
        Log.trace("writeNewEntry for StackTraceElementProxy: " + ste);

        // Rarely set; store raw (null-friendly) strings. Module name/version are
        // usually null on frames outside the module path — writeObject would throw.
        output.writeString(ste.getClassLoaderName());
        kryo.writeObjectOrNull(output, ste.getModuleName(), moduleNameSerializer);
        kryo.writeObjectOrNull(output, ste.getModuleVersion(), moduleVersionSerializer);

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

        String moduleName = kryo.readObjectOrNull(input, String.class, moduleNameSerializer);
        String moduleVersion = kryo.readObjectOrNull(input, String.class, moduleVersionSerializer);

        if(classLoaderName != null || moduleName != null || moduleVersion != null) {
            System.out.println(String.format("classLoaderName = %s moduleName = %s moduleVersion = %s", classLoaderName, moduleName, moduleVersion));
        }

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
