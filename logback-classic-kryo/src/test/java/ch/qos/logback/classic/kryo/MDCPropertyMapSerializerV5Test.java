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

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MDCPropertyMapSerializerV5Test {

    private static final int CACHED_HEADER_BYTES = 2;

    private Kryo kryo;
    private MultiEventCompressedStringSerializer shared;
    private MDCPropertyMapSerializerV5 serializer;

    @BeforeEach
    public void setUp() {
        shared = new MultiEventCompressedStringSerializer(1024);
        serializer = new MDCPropertyMapSerializerV5(1024, shared);
        kryo = new Kryo();
    }

    @Test
    public void valueSerializerIsRequired() {
        assertThatThrownBy(() -> new MDCPropertyMapSerializerV5(1024, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valueSerializer");
    }

    @Test
    public void roundTripHashMap() {
        Map<String, String> original = new HashMap<>();
        original.put("user", "ceki");
        original.put("req", "42");
        original.put("empty", null);

        Map<String, String> restored = writeThenRead(original);
        assertThat(restored).isEqualTo(Map.of("user", "ceki", "req", "42"));
        assertThat(Map.copyOf(restored)).isSameAs(restored);
    }

    @Test
    public void sameInstanceSecondWriteIsCached() {
        Map<String, String> map = new HashMap<>();
        map.put("user", "ceki");

        write(serializer, map);
        byte[] second = write(serializer, map);
        assertThat(second).hasSize(CACHED_HEADER_BYTES);
    }

    @Test
    public void equalDistinctInstancesAreNotCached() {
        Map<String, String> a = new HashMap<>();
        a.put("user", "ceki");
        Map<String, String> b = new HashMap<>(a);

        write(serializer, a);
        byte[] second = write(serializer, b);
        assertThat(second.length).isGreaterThan(CACHED_HEADER_BYTES);
    }

    @Test
    public void mdcValueHitsSharedStringCache() {
        String value = "ACCEPT";
        Output prior = new Output(512, -1);
        shared.write(kryo, prior, value);

        Map<String, String> map = new HashMap<>();
        map.put("OP", value);
        int withWarmCache = write(new MDCPropertyMapSerializerV5(1024, shared), map).length;

        MultiEventCompressedStringSerializer cold = new MultiEventCompressedStringSerializer(1024);
        int withColdCache = write(new MDCPropertyMapSerializerV5(1024, cold), map).length;

        assertThat(withWarmCache).isLessThan(withColdCache);
    }

    @Test
    public void mdcKeyDoesNotUseSharedValueCache() {
        String token = "ACCEPT";
        Output prior = new Output(512, -1);
        shared.write(kryo, prior, token);

        Map<String, String> asValue = new HashMap<>();
        asValue.put("OP", token);
        int valueHit = write(new MDCPropertyMapSerializerV5(1024, shared), asValue).length;

        Map<String, String> asKey = new HashMap<>();
        asKey.put(token, "x");
        int keyMiss = write(new MDCPropertyMapSerializerV5(1024, shared), asKey).length;

        assertThat(keyMiss).isGreaterThan(valueHit);
    }

    @Test
    public void cachedMapRoundTrips() {
        Map<String, String> map = new HashMap<>();
        map.put("user", "ceki");

        Output out = new Output(512, -1);
        serializer.write(kryo, out, map);
        serializer.write(kryo, out, map);
        out.flush();

        MultiEventCompressedStringSerializer readerShared = new MultiEventCompressedStringSerializer(1024);
        MDCPropertyMapSerializerV5 reader = new MDCPropertyMapSerializerV5(1024, readerShared);
        Input in = new Input(out.toBytes());
        Map<String, String> first = read(reader, in);
        Map<String, String> second = read(reader, in);
        assertThat(first).isEqualTo(map);
        assertThat(second).isSameAs(first);
    }

    @Test
    public void v5EventSerializerWiresSharedValueCodec() {
        LoggingEventKryoVOSerializerV5 eventSerializer = new LoggingEventKryoVOSerializerV5();
        assertThat(eventSerializer.mdcPropertyMapSerializer).isInstanceOf(MDCPropertyMapSerializerV5.class);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> writeThenRead(Map<String, String> map) {
        MultiEventCompressedStringSerializer readerShared = new MultiEventCompressedStringSerializer(1024);
        return read(new MDCPropertyMapSerializerV5(1024, readerShared), new Input(write(serializer, map)));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> read(MDCPropertyMapSerializer ser, Input in) {
        return ser.read(kryo, in, (Class) Map.class);
    }

    private byte[] write(MDCPropertyMapSerializer ser, Map<String, String> map) {
        Output out = new Output(512, -1);
        ser.write(kryo, out, map);
        out.flush();
        return out.toBytes();
    }
}
