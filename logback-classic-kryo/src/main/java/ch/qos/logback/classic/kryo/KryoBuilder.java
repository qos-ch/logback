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
import com.esotericsoftware.kryo.kryo5.serializers.MapSerializer;
import com.esotericsoftware.kryo.kryo5.util.MapReferenceResolver;
import org.slf4j.event.KeyValuePair;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class KryoBuilder {


    static public Kryo build(EncoderVersionTag encoderVersionTag) {
        return build(encoderVersionTag.serializerType(), encoderVersionTag.streamVersion());
    }

    /**
     * Build a Kryo instance for the given serializer type and stream version.
     */
    static public Kryo build(KryoEncoder.SerializerType serializerType, byte streamVersion) {
        switch (serializerType) {
            case Vanilla:
                return buildLoggerEventKryoVO_Vanilla();
            case Custom:
                return buildCustomForVersion(streamVersion);
            default:
                throw new IllegalArgumentException("Unknown serializer type: " + serializerType);
        }
    }

    static Kryo buildCustomForVersion(byte streamVersion) {
        switch (streamVersion) {
            case KryoClassicConstants.STREAM_VERSION_3:
                return buildLoggerEventKryoVO_CustomV3();
            case KryoClassicConstants.STREAM_VERSION_4:
                return buildLoggerEventKryoVO_CustomV4();
            case KryoClassicConstants.STREAM_VERSION_5:
                return buildLoggerEventKryoVO_CustomV5();
            default:
                throw new IllegalArgumentException("Unsupported stream version for Custom: "
                        + (streamVersion & 0xFF));
        }
    }

    static public Kryo buildLoggerEventKryoVO_Vanilla() {

        MapReferenceResolver mapReferenceResolver = new MapReferenceResolver();
        Kryo kryo = new Kryo(mapReferenceResolver);

        kryo.register(LoggingEventKryoVO.class);
        kryo.register(LoggerContextVO.class, new VanillaLoggerContextVOSerializer());
        //kryo.register(Object.class);
        kryo.register(Object[].class);
        kryo.register(HashMap.class);
        kryo.register(java.util.ArrayList.class);

        //kryo.register(org.slf4j.helpers.BasicMarker.class);
        kryo.register(java.util.concurrent.CopyOnWriteArrayList.class);
        kryo.register(ch.qos.logback.classic.spi.IThrowableProxy[].class);
        kryo.register(ch.qos.logback.classic.spi.ThrowableProxyVO.class, new VanillaThrowableProxyVOSerializer());
        kryo.register(ch.qos.logback.classic.spi.StackTraceElementProxy[].class);
        kryo.register(ch.qos.logback.classic.spi.StackTraceElementProxy.class, new StackTraceElementProxySerializer());
        kryo.register(StackTraceElement[].class);
        kryo.register(java.lang.StackTraceElement.class, new StackTraceElementSerializer());
        kryo.register(org.slf4j.helpers.BasicMarker.class, new BasicMarkerSerializer());
        kryo.register(KeyValuePair.class, new KeyValuePairSerializer());
        // Appended so existing vanilla class ids stay stable. Logback's snapshot
        // is this wrapper; FieldSerializer cannot construct it.
        kryo.register(Collections.unmodifiableMap(new HashMap<String, String>()).getClass(),
                new MapSerializer<Map<String, String>>() {
                    @Override
                    protected Map<String, String> create(Kryo kryo, Input input,
                                                         Class<? extends Map<String, String>> type, int size) {
                        return new HashMap<>(Math.max(4, (int) (size / 0.75f) + 1));
                    }
                });
        return kryo;
    }


    /**
     * Custom codec for stream version 3 ({@link LoggingEventKryoVOSerializerV3}).
     */
    static public Kryo buildLoggerEventKryoVO_CustomV3() {
        return buildCustomKryo(new LoggingEventKryoVOSerializerV3());
    }

    /**
     * Custom codec for stream version 4 ({@link LoggingEventKryoVOSerializerV4}).
     */
    static public Kryo buildLoggerEventKryoVO_CustomV4() {
        return buildCustomKryo(new LoggingEventKryoVOSerializerV4());
    }

    /**
     * Custom codec for stream version 5 ({@link LoggingEventKryoVOSerializerV5}).
     */
    static public Kryo buildLoggerEventKryoVO_CustomV5() {
        return buildCustomKryo(new LoggingEventKryoVOSerializerV5());
    }


    static Kryo buildCustomKryo(Serializer<LoggingEventKryoVO> eventSerializer) {
        Kryo kryo = new Kryo();

        if (eventSerializer instanceof LoggingEventKryoVOSerializerV4) {
            kryo.setReferences(false);
        }

        kryo.register(LoggingEventKryoVO.class, eventSerializer);
        kryo.register(Object[].class);

        kryo.register(StackTraceElement[].class);
        kryo.register(java.util.ArrayList.class);

        // events are written with explicit serializers, the registration only reserves the class id.
        // Avoid the default FieldSerializer which requires reflective access to ch.qos.logback.classic.spi
        kryo.register(ch.qos.logback.classic.spi.ThrowableProxyVO.class, new VanillaThrowableProxyVOSerializer());
        kryo.register(ch.qos.logback.classic.spi.IThrowableProxy[].class);
        kryo.register(org.slf4j.helpers.BasicMarker.class, new BasicMarkerSerializer());
        kryo.register(KeyValuePair.class, new KeyValuePairSerializer());
        kryo.register(java.util.concurrent.CopyOnWriteArrayList.class);
        kryo.register(ch.qos.logback.classic.spi.StackTraceElementProxy[].class);

        kryo.register(java.util.HashMap.class);

        return kryo;
    }
}
