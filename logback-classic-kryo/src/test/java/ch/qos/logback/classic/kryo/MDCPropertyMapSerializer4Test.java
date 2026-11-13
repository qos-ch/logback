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

public class MDCPropertyMapSerializer4Test {

    private static final int CACHED_HEADER_BYTES = 2;

    private Kryo kryo;
    private MDCPropertyMapSerializerV4 serializer;

    @BeforeEach
    public void setUp() {
        serializer = new MDCPropertyMapSerializerV4(1024);
        kryo = new Kryo();
    }

    @Test
    public void roundTripHashMap() {
        Map<String, String> original = new HashMap<>();
        original.put("user", "ceki");
        original.put("req", "42");
        original.put("empty", null);

        Map<String, String> restored = writeThenRead(serializer, original);
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
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotSameAs(b);

        write(serializer, a);
        byte[] second = write(serializer, b);
        assertThat(second.length).isGreaterThan(CACHED_HEADER_BYTES);
    }


    @Test
    public void v3DirectMapCachesEqualDistinctMaps() {
        MDCPropertyMapSerializer v3 = new MDCPropertyMapSerializer(1024);
        Map<String, String> a = new HashMap<>();
        a.put("user", "ceki");
        Map<String, String> b = new HashMap<>(a);

        write(v3, a);
        byte[] second = write(v3, b);
        assertThat(second).hasSize(CACHED_HEADER_BYTES);
    }

    @Test
    public void cachedMapRoundTrips() {
        Map<String, String> map = new HashMap<>();
        map.put("user", "ceki");

        Output out = new Output(512, -1);
        serializer.write(kryo, out, map);
        serializer.write(kryo, out, map);
        out.flush();

        MDCPropertyMapSerializerV4 reader = new MDCPropertyMapSerializerV4(1024);
        Input in = new Input(out.toBytes());
        Map<String, String> first = read(reader, in);
        Map<String, String> second = read(reader, in);
        assertThat(first).isEqualTo(map);
        assertThat(second).isSameAs(first);
    }

    private Map<String, String> writeThenRead(MDCPropertyMapSerializer ser, Map<String, String> map) {
        MDCPropertyMapSerializer reader = (ser instanceof MDCPropertyMapSerializerV4)
                ? new MDCPropertyMapSerializerV4(1024)
                : new MDCPropertyMapSerializer(1024);
        return read(reader, new Input(write(ser, map)));
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
