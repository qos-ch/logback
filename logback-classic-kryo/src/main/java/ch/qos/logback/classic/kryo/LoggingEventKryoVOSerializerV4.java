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

import ch.qos.logback.classic.spi.ThrowableProxyVO;
import ch.qos.logback.core.kryo.SerializerBase;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.serializers.CollectionSerializer;
import org.slf4j.event.KeyValuePair;

import java.util.ArrayList;

/**
 * Stream version 4 event serializer.
 *
 * <p>Same field order as {@link LoggingEventKryoVOSerializerV3}, including
 * {@code callerData} via {@code writeObjectOrNull} (no presence mask). Long unique strings
 * (message, string arguments, and KVP string values) share one
 * {@link MultiEventCompressedStringSerializer} so there is a single deflate stream.
 * Short repeating keys (thread name, MDC, KVP keys) use
 * {@link BasicStringSerializer} and the id cache.
 *
 * <p>Selected when the stream header version is
 * {@link KryoClassicConstants#STREAM_VERSION_4}.
 */
public class LoggingEventKryoVOSerializerV4 extends LoggingEventKryoVOSerializerV3 {

    /**
     * Lazy: {@code super()} calls {@link #newMessageSerializer()} before subclass
     * field initializers run.
     */
    private MultiEventCompressedStringSerializer longUniqueStrings;

    protected MultiEventCompressedStringSerializer longUniqueStrings() {
        if (longUniqueStrings == null) {
            longUniqueStrings = new MultiEventCompressedStringSerializer(1024);
        }
        return longUniqueStrings;
    }

    @Override
    protected SerializerBase<String> newMessageSerializer() {
        return longUniqueStrings();
    }

    @Override
    protected ArgumentArraySerializerV3 newArgumentArraySerializer() {
        return new ArgumentArraySerializerV3(longUniqueStrings());
    }

    @Override
    protected MDCPropertyMapSerializer newMdcPropertyMapSerializer() {
        return new MDCPropertyMapSerializerV4(1024);
    }

    @Override
    protected CollectionSerializer<ArrayList<KeyValuePair>> newKeyValuePairCollectionSerializer() {
        return new KeyValuePairCollectionSerializerV4(
                new KeyValuePairSerializerV4(new BasicStringSerializer(1024), longUniqueStrings()));
    }

    @Override
    protected Serializer<String> newThreadNameSerializer() {
        return new BasicStringSerializer(1024);
    }

    @Override
    protected Serializer<ThrowableProxyVO> newThrowableProxyVOSerializer() {
        return new ThrowableProxyVOSerializerV4(1024);
    }
}
