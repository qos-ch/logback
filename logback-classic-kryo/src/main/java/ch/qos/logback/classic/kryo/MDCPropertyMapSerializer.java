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
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

import java.util.HashMap;
import java.util.Map;

/**
 * Serializer for MDC property maps ({@code Map<String, String>}).
 *
 * <p>Each map is written as:
 * <ol>
 *   <li>entry count ({@code writeVarInt}), excluding null keys and null values</li>
 *   <li>for each remaining entry: key string, then value string</li>
 * </ol>
 *
 * <p>The default constructor caches whole maps with {@link BiMapType#DIRECT_MAP}
 * ({@code equals}/{@code hashCode}). {@link MDCPropertyMapSerializerV4} uses
 * {@link BiMapType#IDENTITY}. Decode returns {@link Map#copyOf}. An older stream
 * may still contain a null value; that entry is read and dropped.
 *
 * <p>By default key and value strings share one nested string serializer.
 * {@link MDCPropertyMapSerializerV5} uses {@link BasicStringSerializer} for keys
 * and a caller-supplied value codec (the V5 message/argument long-string serializer).
 *
 * <p>Null maps are not handled here; callers should use {@code writeObjectOrNull} /
 * {@code readObjectOrNull}.
 */
public class MDCPropertyMapSerializer extends SerializerBase<Map<String, String>> {

    private final Serializer<String> keySerializer;
    private final Serializer<String> valueSerializer;

    /**
     * @param capacity capacity of the whole-map cache and of the nested string cache
     */
    public MDCPropertyMapSerializer(int capacity) {
        this(BiMapType.DIRECT_MAP, capacity);
    }

    /**
     * @param biMapType whole-map cache: {@link BiMapType#DIRECT_MAP} (equals) or
     *                  {@link BiMapType#IDENTITY} ({@code ==})
     * @param capacity  capacity of the whole-map cache and of the nested string cache
     */
    public MDCPropertyMapSerializer(BiMapType biMapType, int capacity) {
        this(biMapType, capacity, null, null);
    }

    /**
     * @param biMapType        whole-map cache: {@link BiMapType#DIRECT_MAP} (equals) or
     *                         {@link BiMapType#IDENTITY} ({@code ==})
     * @param capacity         capacity of the whole-map cache; also used when this
     *                         constructor allocates nested string serializers
     * @param keySerializer    key codec; {@code null} means {@link #getNewEntrySerializer(int)}
     * @param valueSerializer  value codec; {@code null} means the same instance as keys
     */
    public MDCPropertyMapSerializer(BiMapType biMapType, int capacity,
                                    Serializer<String> keySerializer,
                                    Serializer<String> valueSerializer) {
        super(biMapType, capacity);
        setAcceptsNull(false);
        // acceptsNull stays false: Kryo writeObjectOrNull handles null values via NULL/NOT_NULL.
        if (keySerializer == null) {
            keySerializer = getNewEntrySerializer(capacity);
        }
        if (valueSerializer == null) {
            valueSerializer = keySerializer;
        }
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }


    @Override
    protected void writeNewEntry(Kryo kryo, Output output, Map<String, String> map) {
        int count = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                count++;
            }
        }
        output.writeVarInt(count, true);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || value == null) {
                continue;
            }
            kryo.writeObject(output, key, keySerializer);
            kryo.writeObjectOrNull(output, value, valueSerializer);
        }
    }

    @Override
    protected Map<String, String> readNewEntry(Kryo kryo, Input input) {
        int size = input.readVarInt(true);
        HashMap<String, String> map = new HashMap<>(Math.max(4, (int) (size / 0.75f) + 1));
        for (int i = 0; i < size; i++) {
            String key = kryo.readObject(input, String.class, keySerializer);
            String value = kryo.readObjectOrNull(input, String.class, valueSerializer);
            // Older streams included null values in the count and wrote them.
            if (key == null || value == null) {
                continue;
            }
            map.put(key, value);
        }
        return Map.copyOf(map);
    }

    protected Serializer<String> getNewEntrySerializer(int capacity) {
        return new BasicStringSerializer(BiMapType.DIRECT_MAP, capacity);
    }
}
